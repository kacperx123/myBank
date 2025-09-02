package com.app.mybank.infrastructure.audit;

import com.app.mybank.domain.account.AccountId;
import com.app.mybank.domain.account.events.AccountCreated;
import com.app.mybank.domain.account.events.MoneyDeposited;
import com.app.mybank.domain.account.events.MoneyWithdrawn;
import com.app.mybank.domain.common.Money;
import com.app.mybank.domain.fx.FxRate;
import com.app.mybank.domain.fx.events.FxRateCached;
import com.app.mybank.domain.transaction.TransactionId;
import com.app.mybank.domain.transaction.TransactionType;
import com.app.mybank.domain.transaction.events.TransactionCreated;
import com.app.mybank.domain.user.UserId;
import com.app.mybank.domain.user.events.UserRegistered;
import com.app.mybank.persistence.audit.AuditLogEntryJpaEntity;
import com.app.mybank.persistence.audit.SpringDataAuditLogRepository;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.transaction.TestTransaction;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Currency;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({AuditLogListener.class, AuditLogListenerIT.TestCfg.class})
class AuditLogListenerIT {

    @Container
    static PostgreSQLContainer<?> db = new PostgreSQLContainer<>("postgres:17-alpine")
            .withDatabaseName("mybank")
            .withUsername("postgres")
            .withPassword("root");

    @DynamicPropertySource
    static void props(DynamicPropertyRegistry reg) {
        reg.add("spring.datasource.url", db::getJdbcUrl);
        reg.add("spring.datasource.username", db::getUsername);
        reg.add("spring.datasource.password", db::getPassword);
        reg.add("spring.jpa.hibernate.ddl-auto", () -> "validate");
        reg.add("spring.flyway.enabled", () -> "true");
    }

    @TestConfiguration
    static class TestCfg {
        @Bean ObjectMapper objectMapper() {
            return new ObjectMapper().findAndRegisterModules();
        }
    }

    @Resource ApplicationEventPublisher publisher;
    @Resource
    SpringDataAuditLogRepository auditRepo;

    @AfterEach
    void cleanup() {
        auditRepo.deleteAll();
    }


    private void publishAndCommit(Object event) {
        assertThat(TestTransaction.isActive()).as("Spring test transaction should be active").isTrue();
        publisher.publishEvent(event);
        TestTransaction.flagForCommit();
        TestTransaction.end();
    }

    private AuditLogEntryJpaEntity theOnlyEntry() {
        List<AuditLogEntryJpaEntity> all = auditRepo.findAll();
        assertThat(all).hasSize(1);
        return all.get(0);
    }

    @Test
    void shouldPersistAuditEntry_onTransactionCreated_afterCommit() {
        // given
        var event = new TransactionCreated(
                TransactionId.newId(),
                null,
                null,
                new Money(new BigDecimal("100.54"), Currency.getInstance("PLN")),
                TransactionType.DEPOSIT,
                Instant.now()
        );
        publishAndCommit(event);


        var entry = theOnlyEntry();
        assertThat(entry.getAction()).isEqualTo("TRANSACTION_CREATED");
        assertThat(entry.getEntityType()).isEqualTo("TransactionCreated");
        assertThat(entry.getPayload().get("type").asText()).isEqualTo("DEPOSIT");
        assertThat(entry.getPayload().get("amount").get("amount").asText()).isEqualTo("100.54");
        assertThat(entry.getPayload().get("amount").get("currency").asText()).isEqualTo("PLN");
        assertThat(entry.getCreatedAt()).isNotNull();
    }

    @Test
    void onUserRegistered_shouldPersistAudit() {
        // given
        var uid = new UserId(UUID.randomUUID());
        var event = new UserRegistered(uid, "bob@example.com", Instant.now());

        // when
        publishAndCommit(event);

        // then
        var entry = theOnlyEntry();
        assertThat(entry.getAction()).isEqualTo("USER_REGISTERED");
        assertThat(entry.getEntityType()).isEqualTo("UserRegistered");
        assertThat(entry.getUserId()).isEqualTo(uid.value());
        assertThat(entry.getEntityId()).isEqualTo(uid.value());
        assertThat(entry.getCreatedAt()).isNotNull();

        // payload basic smoke check
        assertThat(entry.getPayload().toString())
                .contains("bob@example.com")
                .contains(uid.value().toString());
    }

    @Test
    void onAccountCreated_shouldPersistAudit() {
        // given
        var accId = new AccountId(UUID.randomUUID());
        var uid   = new UserId(UUID.randomUUID());
        var event = new AccountCreated(accId, uid, Instant.now());

        // when
        publishAndCommit(event);

        // then
        var entry = theOnlyEntry();
        assertThat(entry.getAction()).isEqualTo("ACCOUNT_CREATED");
        assertThat(entry.getEntityType()).isEqualTo("AccountCreated");
        assertThat(entry.getUserId()).isEqualTo(uid.value());
        assertThat(entry.getEntityId()).isEqualTo(accId.value());
        assertThat(entry.getCreatedAt()).isNotNull();

        assertThat(entry.getPayload().toString())
                .contains(uid.value().toString())
                .contains(accId.value().toString());
    }

    @Test
    void onMoneyDeposited_shouldPersistAudit() {
        // given
        var accId = new AccountId(UUID.randomUUID());
        var money = new Money(new BigDecimal("123.45"), Currency.getInstance("PLN"));
        var event = new MoneyDeposited(accId, money, Instant.now());

        // when
        publishAndCommit(event);

        // then
        var entry = theOnlyEntry();
        assertThat(entry.getAction()).isEqualTo("MONEY_DEPOSITED");
        assertThat(entry.getEntityType()).isEqualTo("MoneyDeposited");
        assertThat(entry.getUserId()).isNull(); // w listenerze zapisujesz null
        assertThat(entry.getEntityId()).isEqualTo(accId.value());

        // payload – sprawdzamy kwotę i walutę
        assertThat(entry.getPayload().toString())
                .contains("123.45")
                .contains("PLN")
                .contains(accId.value().toString());
    }

    @Test
    void onMoneyWithdrawn_shouldPersistAudit() {
        // given
        var accId = new AccountId(UUID.randomUUID());
        var money = new Money(new BigDecimal("10.00"), Currency.getInstance("PLN"));
        var event = new MoneyWithdrawn(accId, money, Instant.now());

        // when
        publishAndCommit(event);

        // then
        var entry = theOnlyEntry();
        assertThat(entry.getAction()).isEqualTo("MONEY_WITHDRAWN");
        assertThat(entry.getEntityType()).isEqualTo("MoneyWithdrawn");
        assertThat(entry.getUserId()).isNull();
        assertThat(entry.getEntityId()).isEqualTo(accId.value());
        assertThat(entry.getPayload().toString())
                .contains("10")
                .contains("PLN")
                .contains(accId.value().toString());
    }

    @Test
    void onFxRateCached_shouldPersistAudit() {
        // given
        var rate = new FxRate(
                Currency.getInstance("USD"),
                Currency.getInstance("PLN"),
                new BigDecimal("3.999900"),
                LocalDate.now(),
                LocalDateTime.now()
        );
        var event = new FxRateCached(rate, Instant.now()); // jeśli masz inną sygnaturę, dopasuj

        // when
        publishAndCommit(event);

        // then
        var entry = theOnlyEntry();
        assertThat(entry.getAction()).isEqualTo("FXRATE_CACHED");
        assertThat(entry.getEntityType()).isEqualTo("FxRateCached");
        assertThat(entry.getUserId()).isNull();
        assertThat(entry.getEntityId()).isNull();

        // payload – sprawdzamy waluty i kurs
        assertThat(entry.getPayload().toString())
                .contains("USD")
                .contains("PLN")
                .contains("3.9999");
    }
}

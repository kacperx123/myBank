package com.app.mybank.persistence.account;

import com.app.mybank.domain.account.Account;
import com.app.mybank.domain.account.AccountId;
import com.app.mybank.domain.common.Money;
import com.app.mybank.domain.user.User;
import com.app.mybank.domain.user.UserId;
import com.app.mybank.persistence.user.UserJpaAdapter;
import com.app.mybank.testutil.UserTestFactory;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Currency;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Testcontainers
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
// importujemy nasze adaptery (repozytoria Spring Data znajdzie @DataJpaTest)
@Import({AccountJpaAdapter.class, UserJpaAdapter.class})
class AccountJpaAdapterIT {

    @Container
    static PostgreSQLContainer<?> db = new PostgreSQLContainer<>("postgres:17-alpine")
            .withDatabaseName("mybank")
            .withUsername("postgres")
            .withPassword("root");

    @DynamicPropertySource
    static void datasourceProps(DynamicPropertyRegistry reg) {
        reg.add("spring.datasource.url", db::getJdbcUrl);
        reg.add("spring.datasource.username", db::getUsername);
        reg.add("spring.datasource.password", db::getPassword);
        // upewnij się, że Flyway jest włączony na profilu testowym
        reg.add("spring.flyway.enabled", () -> "true");
        // walidacja schematu przez Hibernate, bez prób tworzenia
        reg.add("spring.jpa.hibernate.ddl-auto", () -> "validate");
    }

    @Resource
    private AccountJpaAdapter accountAdapter;

    @Resource
    private UserJpaAdapter userAdapter;

    private UserId ownerId;

    @BeforeEach
    void setupOwner() {
        // migracja V1_2__insert_default_roles.sql musi istnieć, by rola USER była dostępna
        User owner = UserTestFactory.withEmail("owner@example.com");
        ownerId = userAdapter.save(owner); // zakładamy, że UserRepository.save zwraca UserId (tak robiliśmy wcześniej)
        assertThat(ownerId).isNotNull();
    }

    @Test
    void shouldStoreAndRetrieveAccountById() {
        // given
        LocalDateTime now = LocalDateTime.now();
        Account newAccount = Account.openNew(ownerId, now); // domyślnie PLN 0.00, ACTIVE
        accountAdapter.save(newAccount); // save: void

        // when
        var byOwner = accountAdapter.findByOwnerId(ownerId);
        assertThat(byOwner).hasSize(1);

        AccountId persistedId = byOwner.get(0).id();
        var byId = accountAdapter.findById(persistedId);

        // then
        assertThat(byId).isPresent();
        var acc = byId.get();
        assertThat(acc.ownerId()).isEqualTo(ownerId);
        assertThat(acc.status().name()).isEqualTo("ACTIVE");
        assertThat(acc.balance().amount()).isEqualByComparingTo("0.00");
        assertThat(acc.balance().currency()).isEqualTo(Currency.getInstance("PLN"));
        assertThat(acc.dailyLimit().amount()).isEqualByComparingTo("0.00");
        assertThat(acc.dailyLimit().currency()).isEqualTo(Currency.getInstance("PLN"));
        assertThat(acc.locked()).isFalse();
    }

    @Test
    void shouldFindByOwnerId() {
        // given
        LocalDateTime now = LocalDateTime.now();
        accountAdapter.save(Account.openNew(ownerId, now));
        accountAdapter.save(Account.openNew(ownerId, now.plusSeconds(1)));

        // when
        var list = accountAdapter.findByOwnerId(ownerId);

        // then
        assertThat(list).hasSize(2);
        assertThat(list).allSatisfy(acc ->
                assertThat(acc.ownerId()).isEqualTo(ownerId)
        );
    }

    @Test
    void shouldUpdateBalance() {
        // given
        LocalDateTime now = LocalDateTime.now();
        accountAdapter.save(Account.openNew(ownerId, now));

        var list = accountAdapter.findByOwnerId(ownerId);
        AccountId accId = list.get(0).id();

        Money newBalance = new Money(new BigDecimal("123.45"), Currency.getInstance("PLN"));

        // when
        accountAdapter.updateBalance(accId, newBalance);

        // then
        var byId = accountAdapter.findById(accId);
        assertThat(byId).isPresent();
        assertThat(byId.get().balance().amount()).isEqualByComparingTo("123.45");
        assertThat(byId.get().balance().currency()).isEqualTo(Currency.getInstance("PLN"));
    }

    @Test
    void shouldRejectCurrencyMismatchOnUpdateBalance() {
        // given
        LocalDateTime now = LocalDateTime.now();
        accountAdapter.save(Account.openNew(ownerId, now));
        var list = accountAdapter.findByOwnerId(ownerId);
        AccountId accId = list.get(0).id();

        Money usd = new Money(new BigDecimal("10.00"), Currency.getInstance("USD"));

        // when / then
        assertThatThrownBy(() -> accountAdapter.updateBalance(accId, usd))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Currency mismatch");
    }
}

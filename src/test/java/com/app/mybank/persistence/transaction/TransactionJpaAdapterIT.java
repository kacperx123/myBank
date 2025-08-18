package com.app.mybank.persistence.transaction;

import com.app.mybank.domain.account.Account;
import com.app.mybank.domain.account.AccountId;
import com.app.mybank.domain.common.Money;
import com.app.mybank.domain.transaction.Transaction;
import com.app.mybank.domain.transaction.TransactionId;
import com.app.mybank.domain.transaction.TransactionType;
import com.app.mybank.domain.user.User;
import com.app.mybank.domain.user.UserId;
import com.app.mybank.persistence.account.AccountJpaAdapter;
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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Currency;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({TransactionJpaAdapter.class, AccountJpaAdapter.class, UserJpaAdapter.class})
class TransactionJpaAdapterIT {

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

    @Resource TransactionJpaAdapter txAdapter;
    @Resource AccountJpaAdapter accountAdapter;
    @Resource UserJpaAdapter userAdapter;

    private UserId userId;
    private AccountId accA;
    private AccountId accB;

    private static final Currency PLN = Currency.getInstance("PLN");

    @BeforeEach
    void setup() {
        // owner
        User owner = UserTestFactory.withEmail("tx_owner@example.com");
        userId = userAdapter.save(owner);

        // dwa konta
        var now = LocalDateTime.now();
        Account a = Account.openNew(userId, now);
        Account b = Account.openNew(userId, now.plusSeconds(1));

        accountAdapter.save(a);
        accountAdapter.save(b);

        var list = accountAdapter.findByOwnerId(userId);
        assertThat(list).hasSize(2);

        accA = list.get(0).id();
        accB = list.get(1).id();
    }

    @Test
    void shouldSaveAndLoadTransactions_forAccountHistory() {
        // given
        var t1 = deposit(accA, "100.00", LocalDateTime.now().minusMinutes(10));
        var t2 = withdraw(accA, "20.00", LocalDateTime.now().minusMinutes(5));
        var t3 = transfer(accA, accB, "7.50", LocalDateTime.now().minusMinutes(1));

        txAdapter.save(t1);
        txAdapter.save(t2);
        txAdapter.save(t3);

        // when
        var historyA = txAdapter.findByAccountId(accA);
        var historyB = txAdapter.findByAccountId(accB);

        // then – kolejność malejąco po czasie
        assertThat(historyA).hasSize(3);
        assertThat(historyA).isSortedAccordingTo((x, y) -> y.occurredAt().compareTo(x.occurredAt()));

        // obecność transferu po obu stronach
        assertThat(historyB).hasSize(1);
        assertThat(historyB.get(0).type()).isEqualTo(TransactionType.TRANSFER);
        assertThat(historyB.get(0).targetAccountId()).isEqualTo(accB);
        assertThat(historyB.get(0).sourceAccountId()).isEqualTo(accA);
    }

    @Test
    void shouldAggregateDailyWithdrawals_forGivenDate() {
        // given – trzy transakcje, z czego dwie WITHDRAWAL dzisiaj
        LocalDateTime now = LocalDateTime.now();
        txAdapter.save(deposit(accA, "50.00", now.minusHours(3)));
        txAdapter.save(withdraw(accA, "10.00", now.minusHours(2))); // dziś
        txAdapter.save(withdraw(accA, "5.25", now.minusHours(1)));  // dziś
        txAdapter.save(withdraw(accA, "3.00", now.minusDays(1)));   // wczoraj – nie liczymy

        // when – prostą agregację robimy po stronie testu (repo dostarcza historię)
        var today = LocalDate.now();
        var history = txAdapter.findByAccountId(accA);
        BigDecimal sum = history.stream()
                .filter(t -> t.type() == TransactionType.WITHDRAWAL)
                .filter(t -> t.occurredAt().toLocalDate().equals(today))
                .map(t -> t.amount().amount())
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // then
        assertThat(sum).isEqualByComparingTo("15.25");
    }

    /* ------------ helpers to create domain transactions ------------ */

    private Transaction deposit(AccountId target, String amount, LocalDateTime when) {
        return new Transaction(
                new TransactionId(java.util.UUID.randomUUID()),
                null,
                target,
                new Money(new BigDecimal(amount), PLN),
                TransactionType.DEPOSIT,
                when
        );
    }

    private Transaction withdraw(AccountId source, String amount, LocalDateTime when) {
        return new Transaction(
                new TransactionId(java.util.UUID.randomUUID()),
                source,
                null,
                new Money(new BigDecimal(amount), PLN),
                TransactionType.WITHDRAWAL,
                when
        );
    }

    private Transaction transfer(AccountId source, AccountId target, String amount, LocalDateTime when) {
        return new Transaction(
                new TransactionId(java.util.UUID.randomUUID()),
                source,
                target,
                new Money(new BigDecimal(amount), PLN),
                TransactionType.TRANSFER,
                when
        );
    }
}

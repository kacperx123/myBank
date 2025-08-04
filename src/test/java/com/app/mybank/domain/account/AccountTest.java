package com.app.mybank.domain.account;

import com.app.mybank.application.transaction.command.account.Account;
import com.app.mybank.application.transaction.command.account.AccountId;
import com.app.mybank.application.transaction.command.account.AccountStatus;
import com.app.mybank.application.transaction.command.account.events.MoneyDeposited;
import com.app.mybank.application.transaction.command.account.events.MoneyWithdrawn;
import com.app.mybank.domain.common.Money;
import com.app.mybank.domain.exception.account.*;
import com.app.mybank.domain.user.UserId;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import static com.app.mybank.testutil.MoneyTestFactory.pln;
import static org.junit.jupiter.api.Assertions.*;

class AccountTest {

    private static final UserId OWNER = new UserId(UUID.randomUUID());
    private static final LocalDateTime NOW = LocalDateTime.of(2025, 1, 1, 10, 0);

    /* ---------- Fabryki pomocnicze ---------- */

    private Account freshAccount() {
        return Account.openNew(OWNER, NOW);
    }

    private Account customAccount(
            Money balance,
            Money dailyLimit,
            boolean locked,
            AccountStatus status,
            Money withdrawnToday,
            LocalDate withdrawnDate
    ) {
        return new Account(
                AccountId.newId(),
                OWNER,
                balance,
                NOW,
                dailyLimit,
                locked,
                status,
                withdrawnToday,
                withdrawnDate,
                new ArrayList<>()
        );
    }

    /* ---------- Testy tworzenia ---------- */

    @Test
    void shouldCreateNewAccountWithZeroBalance() {
        Account acc = freshAccount();

        assertEquals(pln("0.00"), acc.balance());
        assertEquals(AccountStatus.ACTIVE, acc.status());
        assertFalse(acc.locked());
        assertEquals(pln("0.00"), acc.dailyLimit());
    }

    /* ---------- Testy deposit() ---------- */

    @Test
    void shouldDepositMoneyAndPublishEvent() {
        Account acc = freshAccount();

        Account updated = acc.deposit(pln("50.00"));

        assertEquals(pln("50.00"), updated.balance());
        assertTrue(updated.pullDomainEvents().stream()
                .anyMatch(e -> e instanceof MoneyDeposited));
    }

    @Test
    void shouldFailDepositWhenCurrencyMismatch() {
        Account acc = freshAccount();
        Money eur = new Money(new BigDecimal("10.00"), Currency.getInstance("EUR"));

        assertThrows(CurrencyMismatchException.class, () -> acc.deposit(eur));
    }

    @Test
    void shouldFailDepositWhenAccountLocked() {
        Account locked = customAccount(pln("0.00"), pln("0.00"),
                true, AccountStatus.ACTIVE, pln("0.00"), NOW.toLocalDate());

        assertThrows(AccountLockedException.class, () -> locked.deposit(pln("10.00")));
    }

    @Test
    void shouldFailDepositWhenAccountClosed() {
        Account closed = customAccount(pln("0.00"), pln("0.00"),
                false, AccountStatus.CLOSED, pln("0.00"), NOW.toLocalDate());

        assertThrows(AccountClosedException.class, () -> closed.deposit(pln("10.00")));
    }

    /* ---------- Testy withdraw() ---------- */

    @Test
    void shouldWithdrawMoneyAndPublishEvent() {
        Account acc = freshAccount().deposit(pln("100.00"));

        Account updated = acc.withdraw(pln("30.00"), NOW);

        assertEquals(pln("70.00"), updated.balance());
        assertTrue(updated.pullDomainEvents().stream()
                .anyMatch(e -> e instanceof MoneyWithdrawn));
    }

    @Test
    void shouldFailWithdrawWhenInsufficientFunds() {
        Account acc = freshAccount();

        assertThrows(InsufficientFundsException.class,
                () -> acc.withdraw(pln("1.00"), NOW));
    }

    @Test
    void shouldFailWithdrawWhenCurrencyMismatch() {
        Money eur = new Money(new BigDecimal("1.00"), Currency.getInstance("EUR"));
        Money pln = new Money(new BigDecimal("5.00"), Currency.getInstance("PLN"));
        Account acc = freshAccount().deposit(pln); // najpierw zasil konto w PLN

        assertThrows(CurrencyMismatchException.class,
                () -> acc.withdraw(eur, NOW));
    }

    @Test
    void shouldFailWithdrawWhenAccountLocked() {
        Account locked = customAccount(pln("50.00"), pln("0.00"),
                true, AccountStatus.ACTIVE, pln("0.00"), NOW.toLocalDate());

        assertThrows(AccountLockedException.class,
                () -> locked.withdraw(pln("10.00"), NOW));
    }

    @Test
    void shouldFailWithdrawWhenAccountClosed() {
        Account closed = customAccount(pln("50.00"), pln("0.00"),
                false, AccountStatus.CLOSED, pln("0.00"), NOW.toLocalDate());

        assertThrows(AccountClosedException.class,
                () -> closed.withdraw(pln("10.00"), NOW));
    }

    @Test
    void shouldFailWhenDailyLimitExceededInSingleWithdrawal() {
        Account accWithLimit = customAccount(
                pln("500.00"),         // balance
                pln("100.00"),         // dailyLimit
                false,
                AccountStatus.ACTIVE,
                pln("0.00"),           // withdrawnToday
                NOW.toLocalDate());

        assertThrows(DailyLimitExceededException.class,
                () -> accWithLimit.withdraw(pln("120.00"), NOW));
    }

    @Test
    void shouldFailWhenDailyLimitExceededAcrossMultipleWithdrawals() {
        Account acc = customAccount(
                pln("300.00"),
                pln("100.00"),  // limit
                false,
                AccountStatus.ACTIVE,
                pln("80.00"),   // już wypłacono
                NOW.toLocalDate());

        assertThrows(DailyLimitExceededException.class,
                () -> acc.withdraw(pln("30.00"), NOW)); // 80 + 30 > 100
    }

    @Test
    void shouldResetWithdrawnTodayOnNextDay() {
        Account acc = customAccount(
                pln("200.00"),
                pln("100.00"),
                false,
                AccountStatus.ACTIVE,
                pln("90.00"),
                NOW.toLocalDate());  // 1 I 2025

        LocalDateTime tomorrow = NOW.plusDays(1);      // 2 I 2025

        Account updated = acc.withdraw(pln("50.00"), tomorrow);

        assertEquals(pln("50.00"), updated.withdrawnToday()); // tylko dzisiejsza wypłata
        assertEquals(tomorrow.toLocalDate(), updated.withdrawnTodayDate());
    }
}

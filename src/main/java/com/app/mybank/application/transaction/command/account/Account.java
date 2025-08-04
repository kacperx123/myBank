package com.app.mybank.application.transaction.command.account;

import com.app.mybank.application.transaction.command.account.events.MoneyDeposited;
import com.app.mybank.application.transaction.command.account.events.MoneyWithdrawn;
import com.app.mybank.domain.common.Money;
import com.app.mybank.domain.exception.account.*;
import com.app.mybank.domain.user.UserId;


import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Encja domenowa typu „immutable plus events”.
 * Dodano:
 *  • status          – ACTIVE / CLOSED / SUSPENDED
 *  • dailyLimit      – limit dzienny wypłat
 *  • locked          – blokada np. na czas postępowania
 *  • withdrawnToday  – suma wypłat w bieżącym dniu (do limitu)
 */
public record Account(
        AccountId id,
        UserId ownerId,
        Money balance,
        LocalDateTime openedAt,
        Money dailyLimit,
        boolean locked,
        AccountStatus status,
        Money withdrawnToday,
        LocalDate withdrawnTodayDate,
        List<Object> domainEvents
) {

    /* ---------- Walidacja rekordu ---------- */
    public Account {
        Objects.requireNonNull(id);
        Objects.requireNonNull(ownerId);
        Objects.requireNonNull(balance);
        Objects.requireNonNull(openedAt);
        Objects.requireNonNull(dailyLimit);
        Objects.requireNonNull(status);
        Objects.requireNonNull(withdrawnToday);
        Objects.requireNonNull(withdrawnTodayDate);
        Objects.requireNonNull(domainEvents);
    }

    /* ---------- Fabryka ---------- */
    public static Account openNew(UserId ownerId, LocalDateTime now) {
        Money zeroPln = new Money(BigDecimal.ZERO.setScale(Money.SCALE), Currency.getInstance("PLN"));
        return new Account(
                AccountId.newId(),
                ownerId,
                zeroPln,
                now,
                zeroPln,             // dailyLimit domyślnie 0 = brak limitu
                false,               // locked
                AccountStatus.ACTIVE,
                zeroPln,             // withdrawnToday
                now.toLocalDate(),   // data dzisiejszych wypłat
                new ArrayList<>()
        );
    }

    /* ---------- Operacje biznesowe ---------- */

    public Account deposit(Money amount) {
        verifyActiveAndUnlocked();
        verifyCurrency(amount);

        Money newBalance = balance.add(amount);
        var copy = withBalance(newBalance);
        copy.domainEvents.add(new MoneyDeposited(id, amount, java.time.Instant.now()));
        return copy;
    }

    public Account withdraw(Money amount, LocalDateTime now) {
        verifyActiveAndUnlocked();
        verifyCurrency(amount);
        verifySufficientFunds(amount);
        verifyDailyLimit(amount, now.toLocalDate());

        Money newBalance = balance.subtract(amount);
        var copy = withBalance(newBalance)
                .withWithdrawnToday(amount)
                .withWithdrawnTodayDate(now.toLocalDate());

        copy.domainEvents.add(new MoneyWithdrawn(id, amount, java.time.Instant.now()));
        return copy;
    }

    /* ---------- Walidatory prywatne ---------- */

    private void verifyActiveAndUnlocked() {
        if (locked)             throw new AccountLockedException();
        if (status != AccountStatus.ACTIVE) throw new AccountClosedException();
    }

    private void verifyCurrency(Money amount) {
        if (!balance.currency().equals(amount.currency())) {
            throw new CurrencyMismatchException();
        }
    }

    private void verifySufficientFunds(Money amount) {
        if (balance.amount().compareTo(amount.amount()) < 0) {
            throw new InsufficientFundsException();
        }
    }

    private void verifyDailyLimit(Money amount, LocalDate today) {
        if (dailyLimit.amount().signum() == 0)
            return;

        Money toCompare = today.equals(withdrawnTodayDate) ? withdrawnToday.add(amount) : amount;

        if (toCompare.amount().compareTo(dailyLimit.amount()) > 0) {
            throw new DailyLimitExceededException();
        }
    }

    /* ---------- Event pulling ---------- */
    public List<Object> pullDomainEvents() {
        var events = List.copyOf(domainEvents);
        domainEvents.clear();
        return events;
    }

    /* ---------- "with" helpers ---------- */
    private Account withBalance(Money newBalance) {
        return new Account(id, ownerId, newBalance, openedAt,
                dailyLimit, locked, status,
                withdrawnToday, withdrawnTodayDate, domainEvents);
    }

    private Account withWithdrawnToday(Money newSum) {
        return new Account(id, ownerId, balance, openedAt,
                dailyLimit, locked, status,
                newSum, withdrawnTodayDate, domainEvents);
    }

    private Account withWithdrawnTodayDate(LocalDate newDate) {
        return new Account(id, ownerId, balance, openedAt,
                dailyLimit, locked, status,
                withdrawnToday, newDate, domainEvents);
    }
}

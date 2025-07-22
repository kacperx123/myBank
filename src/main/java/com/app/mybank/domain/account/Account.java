package com.app.mybank.domain.account;

import com.app.mybank.domain.account.events.MoneyDeposited;
import com.app.mybank.domain.account.events.MoneyWithdrawn;
import com.app.mybank.domain.common.Money;
import com.app.mybank.domain.user.UserId;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Encja domenowa Account.
 * <p>Immutable core (record) + mutujące metody zwracające <b>nową</b> instancję,    *
 * by zachować niezmienność w warstwie domenowej.</p>
 */
public record Account(
        AccountId id,
        UserId ownerId,
        Money balance,
        LocalDateTime openedAt,
        List<Object> domainEvents
) {

    public Account {
        Objects.requireNonNull(id);
        Objects.requireNonNull(ownerId);
        Objects.requireNonNull(balance);
        Objects.requireNonNull(openedAt);
        Objects.requireNonNull(domainEvents);
    }

    /* ---------- Fabryka ---------- */

    public static Account openNew(UserId ownerId, LocalDateTime clockNow) {
        return new Account(
                AccountId.newId(),
                ownerId,
                new Money(Money.SCALE == 2 ? // szybkie 0.00
                        java.math.BigDecimal.ZERO.setScale(Money.SCALE) :
                        java.math.BigDecimal.ZERO,
                        java.util.Currency.getInstance("PLN") // domyślna waluta – zmień wg potrzeb
                ),
                clockNow,
                new ArrayList<>()
        );
    }

    /* ---------- Operacje biznesowe ---------- */

    public Account deposit(Money amount) {
        Money newBalance = this.balance.add(amount);
        var copy = withBalance(newBalance);
        copy.domainEvents.add(
                new MoneyDeposited(id, amount, java.time.Instant.now())
        );
        return copy;
    }

    public Account withdraw(Money amount) {
        Money newBalance = this.balance.subtract(amount); // rzuci wyjątek jeśli <0
        var copy = withBalance(newBalance);
        copy.domainEvents.add(
                new MoneyWithdrawn(id, amount, java.time.Instant.now())
        );
        return copy;
    }

    /* ---------- Gettery pomocnicze ---------- */

    public List<Object> pullDomainEvents() {
        var events = List.copyOf(domainEvents);
        domainEvents.clear();
        return events;
    }

    /* ---------- prywatny “clone with” ---------- */

    private Account withBalance(Money newBalance) {
        return new Account(id, ownerId, newBalance, openedAt, domainEvents);
    }
}

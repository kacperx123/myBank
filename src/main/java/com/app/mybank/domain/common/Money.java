package com.app.mybank.domain.common;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Currency;
import java.util.Objects;

/**
 * Value Object reprezentujący kwotę w danej walucie.
 * Immutable, bez adnotacji JPA – mapowanie w adapterze persistence.
 */
public record Money(BigDecimal amount, Currency currency) {

    public static final int SCALE = 2;

    public Money {
        Objects.requireNonNull(amount, "amount cannot be null");
        Objects.requireNonNull(currency, "currency cannot be null");

        if (amount.scale() > SCALE) {                         // np. 2,345 €
            throw new IllegalArgumentException(
                    "Scale too large: " + amount.scale() + " (max " + SCALE + ")");
        }
        if (amount.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Amount must be ≥ 0");
        }
    }

    // ---------- OPERACJE ARYTMETYCZNE ---------- //

    public Money add(Money other) {
        assertSameCurrency(other);
        return new Money(
                amount.add(other.amount).setScale(SCALE, RoundingMode.HALF_UP),
                currency
        );
    }

    public Money subtract(Money other) {
        assertSameCurrency(other);
        BigDecimal result = amount.subtract(other.amount).setScale(SCALE, RoundingMode.HALF_UP);
        if (result.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Resulting amount < 0");
        }
        return new Money(result, currency);
    }

    public Money multiply(BigDecimal factor) {
        Objects.requireNonNull(factor, "factor cannot be null");
        BigDecimal result = amount
                .multiply(factor)
                .setScale(SCALE, RoundingMode.HALF_UP);
        return new Money(result, currency);
    }

    // ---------- POMOCNICZE ---------- //

    private void assertSameCurrency(Money other) {
        if (!currency.equals(other.currency)) {
            throw new IllegalArgumentException(
                    "Currency mismatch: %s vs %s".formatted(currency, other.currency));
        }
    }

    @Override
    public String toString() {
        return "%s %s".formatted(amount.setScale(SCALE, RoundingMode.UNNECESSARY), currency);
    }
}

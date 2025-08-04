package com.app.mybank.domain.fx;

import com.app.mybank.domain.common.Money;
import com.app.mybank.domain.exception.account.CurrencyMismatchException;
import com.app.mybank.domain.exception.fx.InvalidRateException;
import com.app.mybank.domain.exception.fx.SameCurrencyException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Currency;
import java.util.Objects;

public record FxRate(
        Currency base,
        Currency target,
        BigDecimal rate,
        LocalDate rateDate,
        LocalDateTime cachedAt
) {

    public FxRate {
        Objects.requireNonNull(base);
        Objects.requireNonNull(target);
        Objects.requireNonNull(rate);
        Objects.requireNonNull(rateDate);
        Objects.requireNonNull(cachedAt);

        if (base.equals(target)) throw new SameCurrencyException();
        if (rate.signum() <= 0)   throw new InvalidRateException();
    }

    /** Prosta konwersja bez zaokrągleń. */
    public Money convert(Money from) {
        if (!from.currency().equals(base))
            throw new CurrencyMismatchException();
        return new Money(from.amount().multiply(rate), target);
    }
}

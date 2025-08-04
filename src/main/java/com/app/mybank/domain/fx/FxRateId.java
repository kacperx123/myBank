package com.app.mybank.domain.fx;

import com.app.mybank.domain.exception.fx.SameCurrencyException;

import java.time.LocalDate;
import java.util.Currency;

public record FxRateId(Currency base, Currency target, LocalDate rateDate) {

    public FxRateId {
        if (base.equals(target))
            throw new SameCurrencyException();
    }
}
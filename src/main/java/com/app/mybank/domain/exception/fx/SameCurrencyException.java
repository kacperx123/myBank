package com.app.mybank.domain.exception.fx;

import com.app.mybank.domain.exception.FxRateException;

public final class SameCurrencyException extends FxRateException {
    public SameCurrencyException() {
        super("Currency can't be the same!");
    }
}

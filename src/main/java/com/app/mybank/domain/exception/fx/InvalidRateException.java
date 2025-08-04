package com.app.mybank.domain.exception.fx;

import com.app.mybank.domain.exception.FxRateException;

public final class InvalidRateException extends FxRateException {
    public InvalidRateException() {
        super("Exchange Rate is invalid!");
    }
}

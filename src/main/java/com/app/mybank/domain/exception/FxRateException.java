package com.app.mybank.domain.exception;

public abstract non-sealed class FxRateException extends DomainException{
    protected FxRateException(String message) {
        super(message);
    }
}

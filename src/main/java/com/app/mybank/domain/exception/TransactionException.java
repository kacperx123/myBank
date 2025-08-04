package com.app.mybank.domain.exception;

public abstract non-sealed class TransactionException extends DomainException {
    protected TransactionException(String msg) { super(msg); }
}

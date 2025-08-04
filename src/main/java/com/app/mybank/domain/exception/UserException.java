package com.app.mybank.domain.exception;

public abstract non-sealed class UserException extends DomainException {
    protected UserException(String msg) { super(msg); }
}
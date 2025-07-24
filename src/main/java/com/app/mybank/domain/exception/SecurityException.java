package com.app.mybank.domain.exception;

public abstract non-sealed class SecurityException extends DomainException {
    protected SecurityException(String msg) { super(msg); }
}
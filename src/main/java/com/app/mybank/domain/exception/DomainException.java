package com.app.mybank.domain.exception;

public abstract sealed class DomainException extends RuntimeException
        permits AccountException, SecurityException, UserException
{

    protected DomainException(String message) { super(message); }
}

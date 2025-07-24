package com.app.mybank.domain.exception;

import com.app.mybank.domain.exception.DomainException;

public abstract non-sealed class AccountException extends DomainException {
    protected AccountException(String msg) { super(msg); }
}

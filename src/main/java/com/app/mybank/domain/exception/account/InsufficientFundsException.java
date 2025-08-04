package com.app.mybank.domain.exception.account;

import com.app.mybank.domain.exception.AccountException;

public final class InsufficientFundsException extends AccountException {
    public InsufficientFundsException() {
        super("Insufficient funds on account");
    }
}
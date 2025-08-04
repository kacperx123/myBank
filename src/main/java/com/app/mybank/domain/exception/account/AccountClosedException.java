package com.app.mybank.domain.exception.account;

import com.app.mybank.domain.exception.AccountException;

public final class AccountClosedException extends AccountException {
    public AccountClosedException() {
        super("Account is closed");
    }
}

package com.app.mybank.domain.exception.account;

import com.app.mybank.domain.exception.AccountException;

public final class AccountLockedException extends AccountException {
    public AccountLockedException() {
        super("Account is locked");
    }
}

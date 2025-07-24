package com.app.mybank.domain.exception.account;

import com.app.mybank.domain.exception.AccountException;

public class AccountNotFoundException extends AccountException {
    protected AccountNotFoundException() {
        super("Account not found");
    }
}

package com.app.mybank.domain.exception.account;

import com.app.mybank.domain.exception.AccountException;

public class AccountNotFoundException extends AccountException {
    public AccountNotFoundException() {
        super("Account not found");
    }
}

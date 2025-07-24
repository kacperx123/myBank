package com.app.mybank.domain.exception.account;

import com.app.mybank.domain.exception.AccountException;

public class DuplicateAccountNumberException extends AccountException {
    public DuplicateAccountNumberException() {
        super("this account number already exists");
    }
}

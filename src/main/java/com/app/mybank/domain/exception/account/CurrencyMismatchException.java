package com.app.mybank.domain.exception.account;

import com.app.mybank.domain.exception.AccountException;

public class CurrencyMismatchException extends AccountException {
    public CurrencyMismatchException() {
        super("Currencies don't match");
    }
}

package com.app.mybank.domain.exception.transaction;

import com.app.mybank.domain.exception.TransactionException;

public final class InvalidAmountException extends TransactionException {
    public InvalidAmountException(){
        super("Invalid amount of money!");
    }
}

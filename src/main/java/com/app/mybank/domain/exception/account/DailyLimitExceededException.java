package com.app.mybank.domain.exception.account;

import com.app.mybank.domain.exception.AccountException;

public class DailyLimitExceededException extends AccountException {
    public DailyLimitExceededException() {
        super("You've exceeded daily limit");
    }
}

package com.app.mybank.domain.exception.user;

import com.app.mybank.domain.exception.UserException;

public class EmailAlreadyTakenException extends UserException {
    public EmailAlreadyTakenException() {
        super("This email has already been taken");
    }
}

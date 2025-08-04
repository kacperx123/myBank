package com.app.mybank.domain.exception.user;

import com.app.mybank.domain.exception.UserException;

public class InvalidCredentialsException extends UserException {
    public InvalidCredentialsException() {
        super("Invalid credentials");
    }
}

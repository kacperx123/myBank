package com.app.mybank.domain.security;

public interface PasswordHasher {


    String hash(String rawPassword);

    default boolean matches(String rawPassword, String hashed) {
        throw new UnsupportedOperationException("matches() not implemented");
    }
}

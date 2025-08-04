package com.app.mybank.testutil;

import com.app.mybank.domain.security.PasswordHasher;

public class FakePasswordHasher implements PasswordHasher {

    @Override
    public String hash(String rawPassword) {
        return "HASH_" + rawPassword;
    }

    @Override
    public boolean matches(String raw, String hashed) {
        return hashed.equals("HASH_" + raw);
    }
}

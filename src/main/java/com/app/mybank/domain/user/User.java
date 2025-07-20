package com.app.mybank.domain.user;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Pattern;

public record User(
        UserId id,
        String email,
        String passwordHash,
        Set<Role> roles,
        LocalDateTime createdAt
) {

    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");

    public User {
        Objects.requireNonNull(id,            "id cannot be null");
        Objects.requireNonNull(email,         "email cannot be null");
        Objects.requireNonNull(passwordHash,  "passwordHash cannot be null");
        Objects.requireNonNull(roles,         "roles cannot be null");
        Objects.requireNonNull(createdAt,     "createdAt cannot be null");

        if (!EMAIL_PATTERN.matcher(email).matches()) {
            throw new IllegalArgumentException("Invalid email format: " + email);
        }
        if (roles.isEmpty()) {
            throw new IllegalArgumentException("User must have at least one role");
        }
    }

    public boolean hasRole(Role role) {
        return roles.contains(role);
    }
}

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
        LocalDateTime createdAt,

        boolean enabled
) {

    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");

    public User {
        Objects.requireNonNull(id);
        Objects.requireNonNull(email);
        Objects.requireNonNull(passwordHash);
        Objects.requireNonNull(roles);
        Objects.requireNonNull(createdAt);

        if (!EMAIL_PATTERN.matcher(email).matches())
            throw new IllegalArgumentException("Invalid email format: " + email);
        if (roles.isEmpty())
            throw new IllegalArgumentException("User must have at least one role");
    }

    public boolean hasRole(Role role) { return roles.contains(role); }

    public static User createNew(UserId id, String email, String hash,
                                 Set<Role> roles, LocalDateTime now) {
        return new User(id, email, hash, roles, now, true);
    }
}

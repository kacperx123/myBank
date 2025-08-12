package com.app.mybank.testutil;

import com.app.mybank.domain.user.Role;
import com.app.mybank.domain.user.User;
import com.app.mybank.domain.user.UserId;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

public final class UserTestFactory {

    private UserTestFactory() {}

    public static User sample() {
        return withEmail("alice@example.com");
    }

    public static User withEmail(String email) {
        return new User(
                new UserId(UUID.randomUUID()),
                email,
                "$2a$10$N9qo8uLOickgx2ZMRZo4i.uQX7NqgDwv/.uwZQ8ycopq5GDlQqixe",
                Set.of(Role.USER),
                LocalDateTime.now(),
                true
        );
    }
}

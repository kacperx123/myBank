package com.app.mybank.domain.user;


import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class UserTest {

    private static final LocalDateTime NOW = LocalDateTime.now();

    @Test
    @DisplayName("Should create User with valid data")
    void shouldCreateValidUser() {
        // given
        UserId id = UserId.newId();
        String email = "alice@example.com";
        String hash = "$2a$10$abcdef..."; // symulowany BCrypt

        // when
        User user = new User(id, email, hash, Set.of(Role.USER), NOW);

        // then
        assertEquals(id, user.id());
        assertEquals(email, user.email());
        assertTrue(user.hasRole(Role.USER));
        assertFalse(user.hasRole(Role.ADMIN));
    }

    @Nested
    @DisplayName("Field Validation - ")
    class Validation {

        @Test
        @DisplayName("Should throw exception when email did not met requirements")
        void shouldFailOnInvalidEmail() {
            IllegalArgumentException ex = assertThrows(
                    IllegalArgumentException.class,
                    () -> new User(UserId.newId(), "bad-email",
                            "hash", Set.of(Role.USER), NOW)
            );
            assertTrue(ex.getMessage().contains("Invalid email"));
        }

        @Test
        @DisplayName("Should throw exception when set of rules is empty")
        void shouldFailWhenRolesEmpty() {
            IllegalArgumentException ex = assertThrows(
                    IllegalArgumentException.class,
                    () -> new User(UserId.newId(), "bob@example.com",
                            "hash", Set.of(), NOW)
            );
            assertTrue(ex.getMessage().contains("at least one role"));
        }
    }
}

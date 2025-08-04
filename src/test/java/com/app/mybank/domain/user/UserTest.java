package com.app.mybank.domain.user;


import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class UserTest {

    private static final LocalDateTime NOW = LocalDateTime.of(2025, 1, 1, 12, 0);

    /* ---------- HAPPY PATH ---------- */

    @Test
    @DisplayName("Should create valid User: enabled=true, role=USER")
    void shouldCreateValidUser() {
        // given
        UserId id = new UserId(UUID.randomUUID());
        String email = "alice@example.com";
        String hash  = "hashed_pwd";

        // when
        User user = User.createNew(id, email, hash, Set.of(Role.USER), NOW);

        // then
        assertEquals(id, user.id());
        assertEquals(email, user.email());
        assertTrue(user.enabled());
        assertTrue(user.hasRole(Role.USER));
        assertFalse(user.hasRole(Role.ADMIN));
    }

    /* ---------- VALIDATION ---------- */

    @Nested
    @DisplayName("Field Vaildation - ")
    class Validation {

        @Test
        @DisplayName("Should throw exception on invalid email")
        void shouldFailOnInvalidEmail() {
            assertThrows(IllegalArgumentException.class,
                    () -> User.createNew(UserId.newId(), "bad-email", "hash",
                            Set.of(Role.USER), NOW));
        }

        @Test
        @DisplayName("Should throw exception when roles are empty")
        void shouldFailWhenRolesEmpty() {
            assertThrows(IllegalArgumentException.class,
                    () -> User.createNew(UserId.newId(), "bob@example.com", "hash",
                            Set.of(), NOW));
        }
    }
}

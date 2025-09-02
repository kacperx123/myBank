package com.app.mybank.application.user;

import com.app.mybank.domain.exception.user.EmailAlreadyTakenException;
import com.app.mybank.domain.security.PasswordHasher;
import com.app.mybank.domain.user.User;
import com.app.mybank.domain.user.UserId;
import com.app.mybank.application.user.port.UserRepository;
import com.app.mybank.infrastructure.stub.InMemoryUserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationEventPublisher;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.*;

class CreateUserServiceTest {

    UserRepository repo;
    PasswordHasher hasher;
    ApplicationEventPublisher publisher;
    Clock clock;
    CreateUserService service;

    @BeforeEach
    void setUp() {
        repo = new InMemoryUserRepository();
        hasher = raw -> "HASH_" + raw;
        publisher = event -> {};
        clock = Clock.fixed(Instant.parse("2025-01-01T12:00:00Z"), ZoneOffset.UTC);
        service = new CreateUserService(repo, hasher, publisher, clock);
    }

    @Test
    void shouldRegisterUser() {
        UserId id = service.register("alice@example.com", "s3cr3t");
        User saved = repo.findById(id).orElseThrow();
        assertTrue(saved.enabled());
        assertEquals("HASH_s3cr3t", saved.passwordHash());
    }

    @Test
    void shouldFailWhenEmailTaken() {
        service.register("bob@example.com", "pass");
        assertThrows(EmailAlreadyTakenException.class,
                () -> service.register("bob@example.com", "x"));
    }
}

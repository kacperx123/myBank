package com.app.mybank.application.user;

import com.app.mybank.domain.exception.user.EmailAlreadyTakenException;
import com.app.mybank.domain.security.PasswordHasher;
import com.app.mybank.domain.user.Role;
import com.app.mybank.domain.user.User;
import com.app.mybank.domain.user.UserId;
import com.app.mybank.domain.user.port.UserRepository;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Set;

public class CreateUserService {

    private final UserRepository repo;
    private final PasswordHasher passwordHasher; // prosty port na BCrypt/Argon2
    private final Clock clock;

    public CreateUserService(UserRepository repo,
                             PasswordHasher passwordHasher,
                             Clock clock) {
        this.repo = repo;
        this.passwordHasher = passwordHasher;
        this.clock = clock;
    }

    public UserId register(String email, String rawPassword) {
        repo.findByEmail(email.toLowerCase())
                .ifPresent(u -> { throw new EmailAlreadyTakenException(); });

        String hash = passwordHasher.hash(rawPassword);
        User user = User.createNew(
                UserId.newId(),
                email.toLowerCase(),
                hash,
                Set.of(Role.USER),
                LocalDateTime.now(clock)
        );

        repo.save(user);
        return user.id();
    }
}
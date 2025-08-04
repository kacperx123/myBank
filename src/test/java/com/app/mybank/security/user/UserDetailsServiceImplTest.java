package com.app.mybank.security.user;

import com.app.mybank.domain.user.Role;
import com.app.mybank.domain.user.User;
import com.app.mybank.domain.user.UserId;
import com.app.mybank.infastructure.security.UserDetailsServiceImpl;
import com.app.mybank.infastructure.stub.InMemoryUserRepository;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class UserDetailsServiceImplTest {

    InMemoryUserRepository repo = new InMemoryUserRepository();
    UserDetailsServiceImpl uds  = new UserDetailsServiceImpl(repo);

    @Test
    void shouldLoadUserByEmail() {
        User user = User.createNew(
                new UserId(UUID.randomUUID()),
                "bob@example.com", "{noop}pwd", Set.of(Role.USER),
                LocalDateTime.now());
        repo.save(user);

        var ud = uds.loadUserByUsername("bob@example.com");

        assertEquals("bob@example.com", ud.getUsername());
        assertTrue(ud.isEnabled());
        assertTrue(ud.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_USER")));
    }
}

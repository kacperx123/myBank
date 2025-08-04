package com.app.mybank.security;

import com.app.mybank.domain.user.Role;
import com.app.mybank.domain.user.UserId;
import com.app.mybank.infastructure.security.JwtService;
import com.app.mybank.infastructure.stub.InMemoryUserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;


import java.time.LocalDateTime;
import java.util.Set;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
class JwtAuthFilterIT {

    @Autowired MockMvc mvc;
    @Autowired
    JwtService jwt;

    @Autowired
    InMemoryUserRepository repo;

    UserDetails demo = User.withUsername("demo@bank.pl")
            .password("{noop}x")   // {noop} → bez haszowania
            .roles("USER")
            .build();

    @BeforeEach
    void setUp() {
        repo.clear();
        com.app.mybank.domain.user.User domainUser = com.app.mybank.domain.user.User.createNew(
                UserId.newId(),
                "demo@bank.pl",
                "{noop}x",
                Set.of(Role.USER),
                LocalDateTime.now());
        repo.save(domainUser);
    }

    @Test
    void shouldAuthenticateWithValidBearerToken() throws Exception {
        String token = jwt.generateToken(demo);

        mvc.perform(get("/actuator/health")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }

    @Test
    void shouldRejectWhenTokenMissing() throws Exception {
        mvc.perform(get("/actuator/health"))
                .andExpect(status().isForbidden());
    }
}
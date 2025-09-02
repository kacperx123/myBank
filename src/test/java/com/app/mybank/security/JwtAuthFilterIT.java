package com.app.mybank.security;

import com.app.mybank.domain.user.Role;
import com.app.mybank.domain.user.UserId;
import com.app.mybank.infrastructure.security.JwtService;
import com.app.mybank.infrastructure.stub.InMemoryUserRepository;
import com.app.mybank.persistence.user.UserJpaAdapter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;


import java.time.LocalDateTime;
import java.util.Set;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class JwtAuthFilterIT {
    @org.springframework.boot.test.context.TestConfiguration
    static class TestCfg {
        @org.springframework.context.annotation.Bean
        @org.springframework.context.annotation.Primary
        com.app.mybank.infrastructure.stub.InMemoryUserRepository inMemoryUserRepository() {
            return new com.app.mybank.infrastructure.stub.InMemoryUserRepository();
        }
    }
    @MockitoBean
    UserJpaAdapter userJpaAdapter;
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
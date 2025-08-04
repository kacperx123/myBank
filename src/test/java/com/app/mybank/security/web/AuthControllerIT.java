package com.app.mybank.security.web;

import com.app.mybank.domain.user.Role;
import com.app.mybank.domain.user.User;
import com.app.mybank.domain.user.UserId;
import com.app.mybank.infastructure.stub.InMemoryUserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;

import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;


import java.time.LocalDateTime;
import java.util.Set;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ActiveProfiles("test")
@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = "spring.flyway.enabled=false"
       )
@AutoConfigureMockMvc
@EnableAutoConfiguration(exclude = {DataSourceAutoConfiguration.class, HibernateJpaAutoConfiguration.class})
class AuthControllerIT {

    @Autowired
    MockMvc mvc;
    @Autowired
    InMemoryUserRepository repo;
    @Autowired
    PasswordEncoder encoder;

    @BeforeEach
    void setUp() {
        repo.clear();
        User u = User.createNew(
                UserId.newId(),
                "alice@example.com",
                encoder.encode("pass123"),
                Set.of(Role.USER),
                LocalDateTime.now());
        repo.save(u);
    }

    @Test
    void shouldReturnJwtOnCorrectCredentials() throws Exception {
        mvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                        {"email":"alice@example.com","password":"pass123"}
                        """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists());
    }

    @Test
    void shouldRejectOnBadPassword() throws Exception {
        mvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                        {"email":"alice@example.com","password":"wrong"}
                        """))
                .andExpect(status().isForbidden());
    }
}


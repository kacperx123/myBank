package com.app.mybank.security.web;

import com.app.mybank.domain.user.Role;
import com.app.mybank.domain.user.User;
import com.app.mybank.domain.user.UserId;
import com.app.mybank.infrastructure.audit.AuditLogListener;
import com.app.mybank.infrastructure.stub.InMemoryUserRepository;
import com.app.mybank.persistence.account.AccountJpaAdapter;
import com.app.mybank.persistence.account.SpringDataAccountRepository;
import com.app.mybank.persistence.fx.FxRateJpaAdapter;
import com.app.mybank.persistence.role.RoleJpaRepository;
import com.app.mybank.persistence.transaction.TransactionJpaAdapter;
import com.app.mybank.persistence.user.SpringDataUserRepository;
import com.app.mybank.persistence.user.UserJpaAdapter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;


import java.time.LocalDateTime;
import java.util.Set;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = "spring.flyway.enabled=false"
       )
@AutoConfigureMockMvc
@EnableAutoConfiguration(exclude = {DataSourceAutoConfiguration.class, HibernateJpaAutoConfiguration.class})
@Import({AuthControllerIT.TestCfg.class})
class AuthControllerIT {
    @TestConfiguration
    static class TestCfg {
        @Bean
        @Primary
        InMemoryUserRepository inMemoryUserRepository() {
            return new InMemoryUserRepository();
        }
    }
    @MockitoBean
    AccountJpaAdapter accountJpaAdapter;

    @MockitoBean
    TransactionJpaAdapter transactionJpaAdapter;

    @MockitoBean
    FxRateJpaAdapter fxRateJpaAdapter;

    @MockitoBean
    UserJpaAdapter userJpaAdapter;

    @MockitoBean
    AuditLogListener auditLogListener;

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


package com.app.mybank.persistence.user;

import com.app.mybank.domain.user.User;
import com.app.mybank.testutil.UserTestFactory;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

@Testcontainers
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(UserJpaAdapter.class)
class UserJpaAdapterIT {

    @Container
    static PostgreSQLContainer<?> db =
            new PostgreSQLContainer<>("postgres:17-alpine")
                    .withDatabaseName("mybank")
                    .withUsername("postgres")
                    .withPassword("root");

    @DynamicPropertySource
    static void dsProps(DynamicPropertyRegistry reg) {
        reg.add("spring.datasource.url",      db::getJdbcUrl);
        reg.add("spring.datasource.username", db::getUsername);
        reg.add("spring.datasource.password", db::getPassword);
    }

    @Autowired
    UserJpaAdapter adapter;

    @Test
    void shouldStoreAndRetrieveUser() {
        // given
        var id = adapter.save(UserTestFactory.sample());
        // when
        var byId   = adapter.findById(id);
        var byMail = adapter.findByEmail("alice@example.com");
        // then
        assertThat(byId).isPresent();
        assertThat(byMail).isPresent()
                .map(User::id).hasValue(id);
    }

    @Test
    void shouldEnforceUniqueEmail() {
        adapter.save(UserTestFactory.sample());
        var duplicate = UserTestFactory.sample();
        assertThatThrownBy(() -> adapter.save(duplicate))
                .isInstanceOf(DataIntegrityViolationException.class);
    }
}

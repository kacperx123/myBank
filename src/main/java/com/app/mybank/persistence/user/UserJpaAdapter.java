package com.app.mybank.persistence.user;

import com.app.mybank.application.user.port.UserRepository;
import com.app.mybank.domain.user.Role;
import com.app.mybank.domain.user.User;
import com.app.mybank.domain.user.UserId;
import com.app.mybank.persistence.role.RoleJpaEntity;
import com.app.mybank.persistence.role.RoleJpaRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@Transactional
public class UserJpaAdapter implements UserRepository {

    private final SpringDataUserRepository userRepo;
    private final RoleJpaRepository roleRepo;

    public UserJpaAdapter(SpringDataUserRepository userRepo, RoleJpaRepository roleRepo) {
        this.userRepo = userRepo;
        this.roleRepo = roleRepo;
    }

    public UserId save(User user) {
        var entity = toEntity(user);          // id == null  ⇒  INSERT
        var saved = userRepo.saveAndFlush(entity);
        return new UserId(saved.getId());
    }

    @Override
    public Optional<User> findById(UserId id) {
        return userRepo.findById(id.value())
                .map(this::toDomain);
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return userRepo.findByEmail(email)
                .map(this::toDomain);
    }

    private UserJpaEntity toEntity(User user) {
        Set<RoleJpaEntity> roleEntities = user.roles().stream()
                .map(r -> roleRepo.findByName(r.name())
                        .orElseThrow(() -> new IllegalStateException("Missing role " + r)))
                .collect(Collectors.toSet());

        return UserJpaEntity.builder()
                .id(null)
                .email(user.email())
                .password(user.passwordHash())
                .roles(roleEntities)
                .enabled(user.enabled())
                .createdAt(user.createdAt())
                .build();
    }

    private User toDomain(UserJpaEntity entity) {
        var roles = entity.getRoles().stream()
                .map(r -> Role.valueOf(r.getName()))
                .collect(Collectors.toSet());
        return new User(
                new UserId(entity.getId()),
                entity.getEmail(),
                entity.getPassword(),
                roles,
                entity.getCreatedAt(),
                entity.isEnabled()
        );
    }
}


package com.app.mybank.persistence.role;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface RoleJpaRepository extends JpaRepository<RoleJpaEntity, Integer> {
    Optional<RoleJpaEntity> findByName(String name);
}

package com.app.mybank.persistence.account;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface SpringDataAccountRepository extends JpaRepository<AccountJpaEntity, UUID> {
    List<AccountJpaEntity> findByOwner_Id(UUID ownerId);
}



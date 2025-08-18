package com.app.mybank.persistence.transaction;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface SpringDataTransactionRepository extends JpaRepository<TransactionJpaEntity, UUID> {

    @Query("select t from TransactionJpaEntity t " +
            "where t.sourceId = :accountId or t.targetId = :accountId " +
            "order by t.occurredAt desc")
    List<TransactionJpaEntity> findByAccountId(@Param("accountId") UUID accountId);
}


package com.app.mybank.persistence.audit;

import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataAuditLogRepository extends JpaRepository<AuditLogEntryJpaEntity, Long> {
}

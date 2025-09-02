package com.app.mybank.infrastructure.audit;

import com.app.mybank.domain.account.events.AccountCreated;
import com.app.mybank.domain.account.events.MoneyDeposited;
import com.app.mybank.domain.account.events.MoneyWithdrawn;
import com.app.mybank.domain.fx.events.FxRateCached;
import com.app.mybank.domain.transaction.events.TransactionCreated;
import com.app.mybank.domain.user.events.UserRegistered;
import com.app.mybank.persistence.audit.AuditLogEntryJpaEntity;
import com.app.mybank.persistence.audit.SpringDataAuditLogRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class AuditLogListener {
    private final SpringDataAuditLogRepository repo;
    private final ObjectMapper objectMapper;

    private void saveAudit(Object event, String action, UUID userId, UUID entityId) {
        try {
            AuditLogEntryJpaEntity entry = AuditLogEntryJpaEntity.builder()
                    .userId(userId)
                    .action(action)
                    .entityType(event.getClass().getSimpleName())
                    .entityId(entityId)
                    .payload(objectMapper.valueToTree(event))
                    .createdAt(LocalDateTime.now())
                    .build();

            repo.saveAndFlush(entry);
            log.info("AUDIT persisted: {}", entry);
        } catch (Exception e) {
            log.error("Failed to persist audit log for {}", action, e);
        }
    }
    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT, fallbackExecution = true)
    public void onUserRegistered(UserRegistered e) {
        log.info("AUDIT User registered: id={}, email={}", e.userId(), e.email());
        saveAudit(e, "USER_REGISTERED", e.userId().value(), e.userId().value());
    }

    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT, fallbackExecution = true)
    public void onAccountCreated(AccountCreated e) {
        log.info("AUDIT Account created: AccountId={}, UserId={}", e.accountId(), e.userId());
        saveAudit(e, "ACCOUNT_CREATED", e.userId().value(), e.accountId().value());
    }

    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT, fallbackExecution = true)
    public void onTransactionCreated(TransactionCreated e) {
        log.info("AUDIT TransactionCreated id={} type={} amount={}", e.id(), e.type(), e.amount());
        saveAudit(e, "TRANSACTION_CREATED", null, e.id().value());
    }

    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT, fallbackExecution = true)
    public void onMoneyDeposited(MoneyDeposited e) {
        log.info("AUDIT MoneyDeposited acc={} amount={}", e.accountId(), e.amount());
        saveAudit(e, "MONEY_DEPOSITED", null, e.accountId().value());
    }

    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT, fallbackExecution = true)
    public void onMoneyWithdrawn(MoneyWithdrawn e) {
        log.info("AUDIT MoneyWithdrawn acc={} amount={}", e.accountId(), e.amount());
        saveAudit(e, "MONEY_WITHDRAWN", null, e.accountId().value());
    }

    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT, fallbackExecution = true)
    public void onFxRateCached(FxRateCached e) {
        log.info("AUDIT FxRate cached {}→{} rate={}", e.rate().base(), e.rate().target(), e.rate().rate());
        saveAudit(e, "FXRATE_CACHED", null, null);
    }
}

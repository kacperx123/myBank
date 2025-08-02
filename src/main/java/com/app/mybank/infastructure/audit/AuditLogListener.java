package com.app.mybank.infastructure.audit;

import com.app.mybank.domain.account.events.MoneyDeposited;
import com.app.mybank.domain.account.events.MoneyWithdrawn;
import com.app.mybank.domain.transaction.events.TransactionCreated;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
public class AuditLogListener {

    // tu wstrzykniesz repo JPA gdy będzie gotowe:
    // private final AuditLogRepository repo;

    // 1) po commit-cie transakcji
    @TransactionalEventListener
    public void onTransactionCreated(TransactionCreated e) {
        log.info("AUDIT TransactionCreated id={} type={} amount={}",
                e.id(), e.type(), e.amount());

        // repo.save(AuditLogEntry.of(e));  // gdy dodasz adapter JPA
    }

    @TransactionalEventListener
    public void onMoneyDeposited(MoneyDeposited e) {
        log.info("AUDIT MoneyDeposited acc={} amount={}", e.accountId(), e.amount());
    }

    @TransactionalEventListener
    public void onMoneyWithdrawn(MoneyWithdrawn e) {
        log.info("AUDIT MoneyWithdrawn acc={} amount={}", e.accountId(), e.amount());
    }
}

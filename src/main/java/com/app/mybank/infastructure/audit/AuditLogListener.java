package com.app.mybank.infastructure.audit;

import com.app.mybank.application.transaction.command.account.events.AccountCreated;
import com.app.mybank.application.transaction.command.account.events.MoneyDeposited;
import com.app.mybank.application.transaction.command.account.events.MoneyWithdrawn;
import com.app.mybank.domain.fx.events.CurrencyRateCached;
import com.app.mybank.domain.transaction.events.TransactionCreated;
import com.app.mybank.domain.user.events.UserRegistered;
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
    public void onUserRegistered(UserRegistered e) {
        log.info("AUDIT User registered: id={}, email={}", e.userId(), e.email());
    }

    @TransactionalEventListener
    public void onAccountCreated(AccountCreated e) {
        log.info("AUDIT Account created: AccountId={}, UserId={}", e.accountId(), e.userId());
    }

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
    @TransactionalEventListener
    public void onCurrencyRateCached(CurrencyRateCached e) {
        log.info("AUDIT FxRate cached {}→{} rate={}",
                e.rate().base(), e.rate().target(), e.rate().rate());
    }
}

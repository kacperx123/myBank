package com.app.mybank.infrastructure.audit;

import com.app.mybank.domain.common.Money;
import com.app.mybank.domain.transaction.TransactionId;
import com.app.mybank.domain.transaction.TransactionType;
import com.app.mybank.domain.transaction.events.TransactionCreated;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Currency;

@ActiveProfiles("test")
@SpringBootTest
class AuditLogListenerIT {

    @Autowired
    ApplicationEventPublisher pub;

    @Test
    void shouldLogTransactionCreated() {
        pub.publishEvent(new TransactionCreated(
                TransactionId.newId(), null, null,
                new Money(BigDecimal.ONE, Currency.getInstance("PLN")),
                TransactionType.DEPOSIT, Instant.now()));
    }
}

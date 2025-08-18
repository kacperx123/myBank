package com.app.mybank.persistence.transaction;


import com.app.mybank.application.transaction.port.TransactionRepository;
import com.app.mybank.domain.account.AccountId;
import com.app.mybank.domain.common.Money;
import com.app.mybank.domain.transaction.Transaction;
import com.app.mybank.domain.transaction.TransactionId;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
@Transactional
public class TransactionJpaAdapter implements TransactionRepository {

    private final SpringDataTransactionRepository txRepo;

    public TransactionJpaAdapter(SpringDataTransactionRepository txRepo) {
        this.txRepo = txRepo;
    }

    @Override
    public void save(Transaction tx) {
        var entity = toEntityForInsert(tx);
        txRepo.saveAndFlush(entity);
    }

    public Optional<Transaction> findById(TransactionId id) {
        return txRepo.findById(id.value()).map(this::toDomain);
    }

    @Override
    public List<Transaction> findByAccountId(AccountId accountId) {
        return txRepo.findByAccountId(accountId.value()).stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    /* ---------------- mapping helpers ---------------- */
    private TransactionJpaEntity toEntityForInsert(Transaction t) {
        String ccy = t.amount().currency().getCurrencyCode();
        return TransactionJpaEntity.builder()
                .id(null) // INSERT
                .sourceId(t.sourceAccountId() != null ? t.sourceAccountId().value() : null)
                .targetId(t.targetAccountId() != null ? t.targetAccountId().value() : null)
                .amount(t.amount().amount())
                .currency(ccy)
                .type(t.type())
                .occurredAt(t.occurredAt())
                .build();
    }

    private Transaction toDomain(TransactionJpaEntity e) {
        Currency ccy = Currency.getInstance(e.getCurrency());
        Money money = new Money(e.getAmount(), ccy);
        return new Transaction(
                new TransactionId(e.getId()),
                e.getSourceId() != null ? new AccountId(e.getSourceId()) : null,
                e.getTargetId() != null ? new AccountId(e.getTargetId()) : null,
                money,
                e.getType(),
                e.getOccurredAt()
        );
    }
}



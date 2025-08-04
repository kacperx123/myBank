package com.app.mybank.infastructure.stub;

import com.app.mybank.application.transaction.port.TransactionRepository;
import com.app.mybank.application.transaction.command.account.AccountId;
import com.app.mybank.domain.transaction.Transaction;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Repository
@Profile("test")
public class InMemoryTransactionRepository implements TransactionRepository {

    private final Map<UUID, Transaction> store = new ConcurrentHashMap<>();

    @Override
    public void save(Transaction tx) {
        store.put(tx.id().value(), tx);
    }

    @Override
    public BigDecimal dailyWithdrawalSum(AccountId accountId, LocalDate date) {
        return TransactionRepository.super.dailyWithdrawalSum(accountId, date);
    }

    @Override
    public List<Transaction> findByAccountId(AccountId accountId) {
        return store.values().stream()
                .filter(tx -> accountId.equals(tx.sourceAccountId())
                        || accountId.equals(tx.targetAccountId()))
                .sorted(Comparator.comparing(Transaction::occurredAt).reversed())
                .toList();
    }

    public void clear() { store.clear(); }
}

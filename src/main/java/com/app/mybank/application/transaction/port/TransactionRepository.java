package com.app.mybank.application.transaction.port;

import com.app.mybank.domain.account.AccountId;
import com.app.mybank.domain.transaction.Transaction;
import com.app.mybank.domain.transaction.TransactionType;

import java.time.LocalDate;
import java.util.List;


public interface TransactionRepository {

    /** Zapisuje (lub aktualizuje) transakcję. */
    void save(Transaction tx);

    /** Zwraca wszystkie transakcje danego konta w odwrotnej kolejności czasu. */
    List<Transaction> findByAccountId(AccountId accountId);

    /** Suma wypłat danego dnia (dla limitu dziennego). 0 gdy brak wypłat. */
    default java.math.BigDecimal dailyWithdrawalSum(AccountId accountId, LocalDate date) {
        return findByAccountId(accountId).stream()
                .filter(tx -> tx.type().equals(TransactionType.WITHDRAWAL))
                .filter(tx -> tx.occurredAt().toLocalDate().equals(date))
                .map(tx -> tx.amount().amount())
                .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add);
    }
}

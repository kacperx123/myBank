package com.app.mybank.infrastructure.stub;

import com.app.mybank.domain.account.AccountId;
import com.app.mybank.domain.common.Money;
import com.app.mybank.domain.transaction.Transaction;
import com.app.mybank.domain.transaction.TransactionId;
import com.app.mybank.domain.transaction.TransactionType;
import com.app.mybank.infastructure.stub.InMemoryTransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Currency;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class InMemoryTransactionRepositoryTest {

    private final Currency PLN = Currency.getInstance("PLN");
    private final AccountId A1 = new AccountId(UUID.randomUUID());
    private final AccountId A2 = new AccountId(UUID.randomUUID());

    InMemoryTransactionRepository repo;

    @BeforeEach
    void setUp() { repo = new InMemoryTransactionRepository(); }

    private Transaction tx(AccountId src, AccountId dst,
                           BigDecimal amt, TransactionType type, LocalDateTime ts) {
        return new Transaction(TransactionId.newId(),
                src, dst,
                new Money(amt, PLN),
                type, ts);
    }

    @Test
    void shouldSaveAndRetrieveByAccount() {
        Transaction t1 = tx(null, A1, new BigDecimal("10.00"),
                TransactionType.DEPOSIT, LocalDateTime.now().minusMinutes(1));
        Transaction t2 = tx(A1, null, new BigDecimal("5.00"),
                TransactionType.WITHDRAWAL, LocalDateTime.now());

        repo.save(t1);
        repo.save(t2);

        var list = repo.findByAccountId(A1);
        assertEquals(2, list.size());
        assertEquals(t2, list.get(0));
    }

    @Test
    void shouldCalculateDailyWithdrawalSum() {
        LocalDate today = LocalDate.now();

        repo.save(tx(A1, null, new BigDecimal("5.00"),
                TransactionType.WITHDRAWAL, today.atTime(10, 0)));
        repo.save(tx(A1, null, new BigDecimal("7.50"),
                TransactionType.WITHDRAWAL, today.atTime(12, 0)));
        repo.save(tx(A1, null, new BigDecimal("3.00"),
                TransactionType.WITHDRAWAL, today.minusDays(1).atTime(9, 0))); // inny dzień

        var sum = repo.dailyWithdrawalSum(A1, today);
        assertEquals(new BigDecimal("12.50"), sum);
    }
}

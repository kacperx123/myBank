package com.app.mybank.domain.transaction;

import com.app.mybank.domain.account.AccountId;
import com.app.mybank.domain.common.Money;
import com.app.mybank.domain.exception.transaction.InvalidAmountException;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Currency;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class TransactionTest {

    private static final Currency PLN = Currency.getInstance("PLN");
    private static final Money TEN = new Money(new BigDecimal("10.00"), PLN);
    private static final LocalDateTime NOW = LocalDateTime.of(2025, 1, 1, 12, 0);

    private static AccountId aid() { return new AccountId(UUID.randomUUID()); }

    /* ---------- HAPPY PATH ---------- */

    @Test
    void shouldCreateValidDeposit() {
        AccountId target = aid();

        Transaction t = new Transaction(
                TransactionId.newId(),
                null,
                target,
                TEN,
                TransactionType.DEPOSIT,
                NOW
        );

        assertEquals(TransactionType.DEPOSIT, t.type());
        assertEquals(target, t.targetAccountId());
    }

    @Test
    void shouldCreateValidWithdrawal() {
        AccountId source = aid();

        Transaction t = new Transaction(
                TransactionId.newId(),
                source,
                null,
                TEN,
                TransactionType.WITHDRAWAL,
                NOW
        );

        assertEquals(source, t.sourceAccountId());
    }

    @Test
    void shouldCreateValidTransfer() {
        AccountId src = aid();
        AccountId dst = aid();

        Transaction t = new Transaction(
                TransactionId.newId(),
                src,
                dst,
                TEN,
                TransactionType.TRANSFER,
                NOW
        );

        assertEquals(src, t.sourceAccountId());
        assertEquals(dst, t.targetAccountId());
    }

    @Test
    void shouldCreateValidExchange() {
        AccountId acc = aid();

        Transaction t = new Transaction(
                TransactionId.newId(),
                acc,
                acc,
                TEN,
                TransactionType.EXCHANGE,
                NOW
        );

        assertEquals(acc, t.sourceAccountId());
        assertEquals(acc, t.targetAccountId());
    }

    /* ---------- WALIDACJE / BŁĘDNE PRZYPADKI ---------- */

    @Nested
    class Validation {

        @Test
        void shouldRejectZeroAmountOnWithdrawal() {
            Money zero = new Money(BigDecimal.ZERO.setScale(2), PLN);

            assertThrows(InvalidAmountException.class,
                    () -> new Transaction(
                            TransactionId.newId(), aid(), null,
                            zero, TransactionType.WITHDRAWAL, NOW));
        }

        @Test
        void shouldRejectZeroAmountOnDeposit() {
            Money zero = new Money(BigDecimal.ZERO.setScale(2), PLN);

            assertThrows(InvalidAmountException.class,
                    () -> new Transaction(
                            TransactionId.newId(), null, aid(),
                            zero, TransactionType.DEPOSIT, NOW));
        }

        @Test
        void shouldRequireTargetForDeposit() {
            assertThrows(NullPointerException.class,
                    () -> new Transaction(
                            TransactionId.newId(), null, null,
                            TEN, TransactionType.DEPOSIT, NOW));
        }

        @Test
        void shouldRequireSourceForWithdrawal() {
            assertThrows(NullPointerException.class,
                    () -> new Transaction(
                            TransactionId.newId(), null, null,
                            TEN, TransactionType.WITHDRAWAL, NOW));
        }

        @Test
        void shouldRejectSameAccountTransferIds() {
            AccountId acc = aid();

            assertThrows(IllegalArgumentException.class,
                    () -> new Transaction(
                            TransactionId.newId(), acc, acc,
                            TEN, TransactionType.TRANSFER, NOW));
        }
    }
}

package com.app.mybank.domain.transaction;

import com.app.mybank.application.transaction.command.account.AccountId;
import com.app.mybank.domain.common.Money;
import com.app.mybank.domain.exception.transaction.InvalidAmountException;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Encja domenowa reprezentująca jedną operację księgową
 * (wpłatę, wypłatę, przelew, konwersję waluty).
 *
 * <p>Nie zawiera logiki – walidacja odbywa się w serwisach aplikacyjnych oraz
 *  w konstruktorze (np. kwota &gt; 0).</p>
 */
public record Transaction(
        TransactionId id,
        AccountId sourceAccountId,   // null dla DEPOSIT
        AccountId targetAccountId,   // null dla WITHDRAWAL
        Money amount,
        TransactionType type,
        LocalDateTime occurredAt
) {

    public Transaction {
        Objects.requireNonNull(id);
        Objects.requireNonNull(amount);
        Objects.requireNonNull(type);
        Objects.requireNonNull(occurredAt);

        if (amount.amount().signum() <= 0)
            throw new InvalidAmountException();

        // prosta walidacja pól zależnie od typu
        switch (type) {
            case DEPOSIT -> Objects.requireNonNull(targetAccountId,
                    "targetAccountId required for DEPOSIT");
            case WITHDRAWAL -> Objects.requireNonNull(sourceAccountId,
                    "sourceAccountId required for WITHDRAWAL");
            case TRANSFER -> {
                Objects.requireNonNull(sourceAccountId,
                        "sourceAccountId required for TRANSFER");
                Objects.requireNonNull(targetAccountId,
                        "targetAccountId required for TRANSFER");
                if (sourceAccountId.equals(targetAccountId))
                    throw new IllegalArgumentException("source and target cannot be the same");
            }
            case EXCHANGE -> {
                Objects.requireNonNull(sourceAccountId,
                        "sourceAccountId required for EXCHANGE");
                Objects.requireNonNull(targetAccountId,
                        "targetAccountId required for EXCHANGE (same account but diff currency)");
            }
        }
    }
}

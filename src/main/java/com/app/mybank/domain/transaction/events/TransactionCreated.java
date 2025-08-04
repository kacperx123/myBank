package com.app.mybank.domain.transaction.events;

import com.app.mybank.application.transaction.command.account.AccountId;
import com.app.mybank.domain.common.Money;
import com.app.mybank.domain.common.events.DomainEvent;
import com.app.mybank.domain.transaction.TransactionId;
import com.app.mybank.domain.transaction.TransactionType;

import java.time.Instant;

public record TransactionCreated(
        TransactionId id,
        AccountId sourceAccountId,
        AccountId targetAccountId,
        Money amount,
        TransactionType type,
        Instant occurredAt
) implements DomainEvent {}

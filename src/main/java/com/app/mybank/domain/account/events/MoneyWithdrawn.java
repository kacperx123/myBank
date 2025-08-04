package com.app.mybank.application.transaction.command.account.events;

import com.app.mybank.application.transaction.command.account.AccountId;
import com.app.mybank.domain.common.Money;
import com.app.mybank.domain.common.events.DomainEvent;

import java.time.Instant;

/** Emitted after successful withdraw(). */
public record MoneyWithdrawn(AccountId accountId, Money amount, Instant occurredAt)
        implements DomainEvent {}

package com.app.mybank.domain.account.events;


import com.app.mybank.domain.account.AccountId;
import com.app.mybank.domain.common.Money;
import com.app.mybank.domain.common.events.DomainEvent;

import java.time.Instant;

/** Emitted after successful deposit(). */
public record MoneyDeposited(AccountId accountId, Money amount, Instant occurredAt)
        implements DomainEvent {}

package com.app.mybank.domain.account.events;

import com.app.mybank.domain.account.AccountId;
import com.app.mybank.domain.common.events.DomainEvent;
import com.app.mybank.domain.user.UserId;

import java.time.Instant;

public record AccountCreated(AccountId accountId, UserId userId, Instant occurredAt)
        implements DomainEvent {}

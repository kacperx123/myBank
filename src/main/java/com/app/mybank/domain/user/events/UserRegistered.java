package com.app.mybank.domain.user.events;

import com.app.mybank.domain.common.events.DomainEvent;
import com.app.mybank.domain.user.UserId;

import java.time.Instant;

public record UserRegistered(UserId userId, String email, Instant occurredAt)
        implements DomainEvent {}

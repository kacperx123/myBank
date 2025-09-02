package com.app.mybank.domain.fx.events;

import com.app.mybank.domain.common.events.DomainEvent;
import com.app.mybank.domain.fx.FxRate;

import java.time.Instant;

public record FxRateCached(FxRate rate, Instant occurredAt)
        implements DomainEvent {}

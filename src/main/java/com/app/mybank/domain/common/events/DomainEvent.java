package com.app.mybank.domain.common.events;

import java.time.Instant;

/** Marker interface dla zdarzeń domenowych. */
public interface DomainEvent {
    Instant occurredAt();
}

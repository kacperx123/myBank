package com.app.mybank.domain.transaction;

import java.util.Objects;
import java.util.UUID;


public record TransactionId(UUID value) {

    public TransactionId {
        Objects.requireNonNull(value, "value cannot be null");
    }

    public static TransactionId newId() {
        return new TransactionId(UUID.randomUUID());
    }

    @Override
    public String toString() {
        return value.toString();
    }
}

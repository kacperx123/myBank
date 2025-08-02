package com.app.mybank.domain.transaction;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class TransactionIdTest {

    @Test
    void shouldGenerateRandomId() {
        TransactionId id1 = TransactionId.newId();
        TransactionId id2 = TransactionId.newId();

        assertNotNull(id1);
        assertNotNull(id2);
        assertNotEquals(id1, id2);
    }

    @Test
    void shouldWrapUuid() {
        UUID raw = UUID.randomUUID();
        TransactionId id = new TransactionId(raw);

        assertEquals(raw, id.value());
        assertEquals(raw.toString(), id.toString());
    }
}

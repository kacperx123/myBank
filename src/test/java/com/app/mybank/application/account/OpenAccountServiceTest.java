package com.app.mybank.application.account;

import com.app.mybank.domain.account.Account;
import com.app.mybank.domain.account.port.AccountRepository;
import com.app.mybank.domain.user.UserId;
import com.app.mybank.infastructure.stub.InMemoryAccountRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;


import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Currency;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class OpenAccountServiceTest {

    private AccountRepository repo;
    private OpenAccountService service;
    private Clock fixedClock;

    @BeforeEach
    void init() {
        repo = new InMemoryAccountRepository();
        fixedClock = Clock.fixed(Instant.parse("2025-01-01T10:00:00Z"), ZoneOffset.UTC);
        service = new OpenAccountService(repo, fixedClock);
    }

    @Test
    @DisplayName("Should open account for user with balance = 0")
    void shouldOpenAccountForUserWithZeroBalance() {
        // given
        UserId owner = new UserId(UUID.randomUUID());

        // when
        var accountId = service.openAccount(owner);

        // then
        Account saved = repo.findById(accountId).orElseThrow();
        assertEquals(owner, saved.ownerId());
        assertEquals("0.00", saved.balance().amount().toPlainString());
        assertEquals(Currency.getInstance("PLN"), saved.balance().currency());
        assertEquals(LocalDateTime.ofInstant(fixedClock.instant(), fixedClock.getZone()), saved.openedAt());
    }

    @Test
    @DisplayName("Should generate unique id")
    void shouldGenerateUniqueIds() {
        UserId owner = new UserId(UUID.randomUUID());

        var id1 = service.openAccount(owner);
        var id2 = service.openAccount(owner);

        assertNotEquals(id1, id2);
    }
}

package com.app.mybank.infrastructure.stub;

import com.app.mybank.domain.account.Account;
import com.app.mybank.domain.account.AccountId;
import com.app.mybank.domain.user.UserId;
import com.app.mybank.infastructure.stub.InMemoryAccountRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;


import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class InMemoryAccountRepositoryTest {

    private InMemoryAccountRepository repo;

    @BeforeEach
    void setUp() {
        repo = new InMemoryAccountRepository();
    }

    @Test
    @DisplayName("Should save and find by account id")
    void shouldSaveAndFindById() {
        // given
        UserId owner = new UserId(UUID.randomUUID());
        Account acc  = Account.openNew(owner, LocalDateTime.now());

        // when
        repo.save(acc);

        // then
        assertTrue(repo.findById(acc.id()).isPresent());
    }

    @Test
    @DisplayName("Should return user accounts")
    void shouldReturnAccountsByOwner() {
        UserId alice = new UserId(UUID.randomUUID());
        UserId bob   = new UserId(UUID.randomUUID());

        repo.save(Account.openNew(alice, LocalDateTime.now()));
        repo.save(Account.openNew(alice, LocalDateTime.now()));
        repo.save(Account.openNew(bob,   LocalDateTime.now()));

        List<Account> aliceAccounts = repo.findByOwnerId(alice);

        assertEquals(2, aliceAccounts.size());
        assertTrue(aliceAccounts.stream().allMatch(a -> a.ownerId().equals(alice)));
    }

    @Test
    @DisplayName("Should return empty optional when account id is not found")
    void shouldReturnEmptyOptionalWhenNotFound() {
        assertTrue(repo.findById(AccountId.newId()).isEmpty());
    }
}

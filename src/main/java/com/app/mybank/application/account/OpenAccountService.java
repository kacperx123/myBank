package com.app.mybank.application.account;

import com.app.mybank.domain.account.Account;
import com.app.mybank.domain.account.AccountId;
import com.app.mybank.application.account.port.AccountRepository;
import com.app.mybank.domain.user.UserId;

import java.time.Clock;
import java.time.LocalDateTime;


public class OpenAccountService {

    private final AccountRepository repository;
    private final Clock clock;

    public OpenAccountService(AccountRepository repository, Clock clock) {
        this.repository = repository;
        this.clock = clock;
    }


    public AccountId openAccount(UserId ownerId) {
        Account account = Account.openNew(ownerId, LocalDateTime.now(clock));
        repository.save(account);
        return account.id();
    }
}

package com.app.mybank.application.account;

import com.app.mybank.domain.account.Account;
import com.app.mybank.domain.account.AccountId;
import com.app.mybank.application.account.port.AccountRepository;
import com.app.mybank.domain.account.events.AccountCreated;
import com.app.mybank.domain.user.UserId;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;


@RequiredArgsConstructor
public class OpenAccountService {

    private final AccountRepository repository;

    private final ApplicationEventPublisher publisher;
    private final Clock clock;


    public AccountId openAccount(UserId ownerId) {
        Account account = Account.openNew(ownerId, LocalDateTime.now(clock));
        repository.save(account);

        publisher.publishEvent(new AccountCreated(account.id(), ownerId, Instant.now(clock)));

        return account.id();
    }
}

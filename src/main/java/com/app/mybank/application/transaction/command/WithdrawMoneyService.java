package com.app.mybank.application.transaction.command;

import com.app.mybank.application.account.port.AccountRepository;
import com.app.mybank.application.transaction.port.TransactionRepository;
import com.app.mybank.application.transaction.command.account.Account;
import com.app.mybank.application.transaction.command.account.AccountId;
import com.app.mybank.domain.common.Money;
import com.app.mybank.domain.exception.account.AccountNotFoundException;
import com.app.mybank.domain.transaction.Transaction;
import com.app.mybank.domain.transaction.TransactionId;
import com.app.mybank.domain.transaction.TransactionType;
import com.app.mybank.domain.transaction.events.TransactionCreated;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;

@RequiredArgsConstructor
public class WithdrawMoneyService {

    private final AccountRepository accountRepo;
    private final TransactionRepository txRepo;
    private final ApplicationEventPublisher publisher;
    private final Clock clock;


    public TransactionId withdraw(AccountId source, Money amount) {
        Account acc = accountRepo.findById(source)
                .orElseThrow(AccountNotFoundException::new);

        Account updated = acc.withdraw(amount, LocalDateTime.now(clock));
        accountRepo.save(updated);

        Transaction tx = new Transaction(
                TransactionId.newId(),
                source,
                null,
                amount,
                TransactionType.WITHDRAWAL,
                LocalDateTime.now(clock)
        );
        txRepo.save(tx);

        publisher.publishEvent(new TransactionCreated(
                tx.id(), tx.sourceAccountId(), tx.targetAccountId(),
                amount, tx.type(), Instant.now(clock)));

        return tx.id();
    }
}

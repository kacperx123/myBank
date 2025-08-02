package com.app.mybank.application.transaction.command;

import com.app.mybank.application.account.port.AccountRepository;
import com.app.mybank.application.transaction.port.TransactionRepository;
import com.app.mybank.domain.account.Account;
import com.app.mybank.domain.account.AccountId;
import com.app.mybank.domain.common.Money;
import com.app.mybank.domain.exception.account.AccountNotFoundException;
import com.app.mybank.domain.transaction.Transaction;
import com.app.mybank.domain.transaction.TransactionId;
import com.app.mybank.domain.transaction.TransactionType;
import com.app.mybank.domain.transaction.events.TransactionCreated;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;

@RequiredArgsConstructor
public class DepositMoneyService {

    private final AccountRepository accountRepo;
    private final TransactionRepository txRepo;
    private final ApplicationEventPublisher publisher;
    private final Clock clock;


    public TransactionId deposit(AccountId target, Money amount) {
        Account acc = accountRepo.findById(target)
                .orElseThrow(AccountNotFoundException::new);

        Account updated = acc.deposit(amount);
        accountRepo.save(updated);

        Transaction tx = new Transaction(
                TransactionId.newId(),
                null,
                target,
                amount,
                TransactionType.DEPOSIT,
                LocalDateTime.now(clock)
        );
        txRepo.save(tx);

        // --- emitujemy zdarzenie ---
        publisher.publishEvent(new TransactionCreated(
                tx.id(), tx.sourceAccountId(), tx.targetAccountId(),
                amount, tx.type(), Instant.now(clock)));

        return tx.id();
    }
}

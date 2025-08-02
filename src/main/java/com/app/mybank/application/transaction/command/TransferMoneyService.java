package com.app.mybank.application.transaction.command;

import com.app.mybank.application.account.port.AccountRepository;
import com.app.mybank.application.transaction.port.TransactionRepository;
import com.app.mybank.domain.account.Account;
import com.app.mybank.domain.account.AccountId;
import com.app.mybank.domain.common.Money;
import com.app.mybank.domain.exception.account.AccountLockedException;
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
public class TransferMoneyService {

    private final AccountRepository accountRepo;
    private final TransactionRepository txRepo;
    private final ApplicationEventPublisher publisher;
    private final Clock clock;


    public TransactionId transfer(AccountId source, AccountId target, Money amount) {
        if (source.equals(target))
            throw new IllegalArgumentException("source and target cannot be the same");

        Account src = accountRepo.findById(source)
                .orElseThrow(AccountNotFoundException::new);
        Account dst = accountRepo.findById(target)
                .orElseThrow(AccountNotFoundException::new);

        if (!src.balance().currency().equals(dst.balance().currency()))
            throw new AccountLockedException();

        LocalDateTime now = LocalDateTime.now(clock);

        Account srcUpd = src.withdraw(amount, now);
        Account dstUpd = dst.deposit(amount);

        accountRepo.save(srcUpd);
        accountRepo.save(dstUpd);

        Transaction tx = new Transaction(TransactionId.newId(),
                source, target, amount, TransactionType.TRANSFER, now);
        txRepo.save(tx);

        publisher.publishEvent(new TransactionCreated(
                tx.id(), tx.sourceAccountId(), tx.targetAccountId(),
                amount, tx.type(), Instant.now(clock)));

        return tx.id();
    }
}

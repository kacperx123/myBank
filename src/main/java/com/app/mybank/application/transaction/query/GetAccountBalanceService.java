package com.app.mybank.application.transaction.query;


import com.app.mybank.application.account.port.AccountRepository;
import com.app.mybank.application.transaction.command.account.Account;
import com.app.mybank.application.transaction.command.account.AccountId;
import com.app.mybank.domain.common.Money;
import com.app.mybank.domain.exception.account.AccountNotFoundException;

public class GetAccountBalanceService {

    private final AccountRepository accountRepo;

    public GetAccountBalanceService(AccountRepository accountRepo) {
        this.accountRepo = accountRepo;
    }

    public Money balanceOf(AccountId id) {
        Account acc = accountRepo.findById(id)
                .orElseThrow(AccountNotFoundException::new);
        return acc.balance();
    }
}

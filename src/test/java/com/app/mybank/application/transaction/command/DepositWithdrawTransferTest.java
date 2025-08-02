package com.app.mybank.application.transaction.command;

import com.app.mybank.application.account.port.AccountRepository;
import com.app.mybank.application.transaction.port.TransactionRepository;
import com.app.mybank.application.transaction.query.GetAccountBalanceService;
import com.app.mybank.domain.account.Account;
import com.app.mybank.domain.account.AccountId;
import com.app.mybank.domain.common.Money;
import com.app.mybank.domain.user.UserId;
import com.app.mybank.infastructure.stub.InMemoryAccountRepository;
import com.app.mybank.infastructure.stub.InMemoryTransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Currency;
import java.util.Set;
import java.util.UUID;

import static com.app.mybank.testutil.MoneyTestFactory.pln;
import static org.junit.jupiter.api.Assertions.*;

class DepositWithdrawTransferTest {

    private final Currency PLN = Currency.getInstance("PLN");
    private AccountRepository accRepo;
    private TransactionRepository txRepo;
    private ApplicationEventPublisher publisher;
    private Clock clock;
    private AccountId A1;
    private AccountId A2;

    @BeforeEach
    void setUp() {
        accRepo = new InMemoryAccountRepository();
        txRepo  = new InMemoryTransactionRepository();
        clock   = Clock.fixed(Instant.parse("2025-01-01T10:00:00Z"), ZoneOffset.UTC);

        publisher = event -> {};

        UserId owner = new UserId(UUID.randomUUID());
        Account a1 = Account.openNew(owner, LocalDateTime.now(clock));
        Account a2 = Account.openNew(owner, LocalDateTime.now(clock));
        accRepo.save(a1);
        accRepo.save(a2);
        A1 = a1.id();
        A2 = a2.id();
    }

    /* -------- DEPOSIT -------- */

    @Test
    void depositHappyPath() {
        DepositMoneyService svc = new DepositMoneyService(accRepo, txRepo, publisher, clock);

        svc.deposit(A1, pln("100.00"));

        Money bal = accRepo.findById(A1).orElseThrow().balance();
        assertEquals(pln("100.00"), bal);
        assertEquals(1, txRepo.findByAccountId(A1).size());
    }

    /* -------- WITHDRAW -------- */

    @Test
    void withdrawHappyPath() {
        accRepo.save(accRepo.findById(A1).get().deposit(pln("50.00")));
        WithdrawMoneyService svc = new WithdrawMoneyService(accRepo, txRepo,publisher, clock);

        svc.withdraw(A1, pln("30.00"));

        Money bal = accRepo.findById(A1).get().balance();
        assertEquals(pln("20.00"), bal);
        assertEquals(1, txRepo.findByAccountId(A1).size());
    }

    @Test
    void withdrawInsufficientFunds() {
        WithdrawMoneyService svc = new WithdrawMoneyService(accRepo, txRepo,publisher, clock);

        assertThrows(RuntimeException.class,          // konkretny exception z Account
                () -> svc.withdraw(A1, pln("5.00")));
    }

    /* -------- GET BALANCE -------- */

    @Test
    void getBalanceService() {
        accRepo.save(accRepo.findById(A1).get().deposit(pln("77.77")));
        var balSvc = new GetAccountBalanceService(accRepo);

        Money bal = balSvc.balanceOf(A1);
        assertEquals(pln("77.77"), bal);
    }

    /* -------- TRANSFER -------- */

    @Test
    void transferHappyPath() {
        accRepo.save(accRepo.findById(A1).get().deposit(pln("100.00")));
        TransferMoneyService svc = new TransferMoneyService(accRepo, txRepo,publisher, clock);

        svc.transfer(A1, A2, pln("40.00"));

        assertEquals(pln("60.00"), accRepo.findById(A1).get().balance());
        assertEquals(pln("40.00"), accRepo.findById(A2).get().balance());
        assertEquals(1, txRepo.findByAccountId(A1).size());   // transfer zapisany raz
    }

    @Test
    void transferAtomicityOnFailure() {
        accRepo.save(accRepo.findById(A1).get().deposit(pln("20.00")));
        TransferMoneyService svc = new TransferMoneyService(accRepo, txRepo,publisher, clock);

        assertThrows(RuntimeException.class,
                () -> svc.transfer(A1, A2, pln("50.00")));  // za duża kwota

        // salda niezmienione
        assertEquals(pln("20.00"), accRepo.findById(A1).get().balance());
        assertEquals(pln("0.00"),  accRepo.findById(A2).get().balance());
        assertTrue(txRepo.findByAccountId(A1).isEmpty());
    }
}

package com.app.mybank.persistence.account;

import com.app.mybank.application.account.port.AccountRepository;
import com.app.mybank.domain.account.Account;
import com.app.mybank.domain.account.AccountId;
import com.app.mybank.domain.account.AccountStatus;

import com.app.mybank.domain.common.Money;
import com.app.mybank.domain.user.UserId;
import com.app.mybank.persistence.user.SpringDataUserRepository;
import com.app.mybank.persistence.user.UserJpaEntity;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@Transactional
public class AccountJpaAdapter implements AccountRepository {

    private final SpringDataAccountRepository accountRepo;
    private final SpringDataUserRepository userRepo;

    public AccountJpaAdapter(SpringDataAccountRepository accountRepo,
                             SpringDataUserRepository userRepo) {
        this.accountRepo = accountRepo;
        this.userRepo = userRepo;
    }

    /** Zapis nowego konta – encja JPA dostaje id=null, aby wymusić INSERT. */
    @Override
    public void save(Account account) {
        var entity = toEntityForInsert(account);
        accountRepo.saveAndFlush(entity);
    }

    @Override
    public Optional<Account> findById(AccountId id) {
        return accountRepo.findById(id.value()).map(this::toDomain);
    }

    @Override
    public List<Account> findByOwnerId(UserId ownerId) {
        return accountRepo.findByOwner_Id(ownerId.value()).stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    public void updateBalance(AccountId id, Money newBalance) {
        var entity = accountRepo.findById(id.value())
                .orElseThrow(() -> new IllegalArgumentException("Account not found: " + id.value()));
        // Waluta konta jest jedna – wymuszamy zgodność
        if (!entity.getCurrency().equals(newBalance.currency().getCurrencyCode())) {
            throw new IllegalArgumentException(
                    "Currency mismatch: account=" + entity.getCurrency()
                            + ", newBalance=" + newBalance.currency().getCurrencyCode()
            );
        }
        entity.setBalance(newBalance.amount());
        accountRepo.save(entity);
    }

    /* ------------------ mapping helpers ------------------ */

    private Account toDomain(AccountJpaEntity e) {
        Currency ccy = Currency.getInstance(e.getCurrency());
        Money balance = new Money(e.getBalance(), ccy);
        Money dailyLimit = new Money(e.getDailyLimit(), ccy);

        return new Account(
                new AccountId(e.getId()),
                new UserId(e.getOwner().getId()),
                balance,
                e.getCreatedAt(),
                dailyLimit,
                e.isLocked(),
                mapStatusToDomain(e.getStatus()),
                zero(ccy),                      // withdrawnToday – nie trzymamy w DB
                e.getCreatedAt().toLocalDate(), // withdrawnTodayDate – punkt startu
                List.of()                       // domainEvents – pusto
        );
    }

    private AccountJpaEntity toEntityForInsert(Account a) {
        UserJpaEntity ownerRef = userRepo.getReferenceById(a.ownerId().value());
        String currencyCode = a.balance().currency().getCurrencyCode();
        // Waluta konta = waluta salda; dailyLimit musi być w tej samej walucie
        if (!currencyCode.equals(a.dailyLimit().currency().getCurrencyCode())) {
            throw new IllegalArgumentException("Daily limit currency must equal account currency");
        }
        return AccountJpaEntity.builder()
                .id(null) // INSERT
                .owner(ownerRef)
                .balance(a.balance().amount())
                .currency(currencyCode)
                .status(mapStatusToJpa(a.status()))
                .dailyLimit(a.dailyLimit().amount())
                .locked(a.locked())
                .createdAt(a.openedAt())
                .build();
    }

    private static Money zero(Currency ccy) {
        return new Money(BigDecimal.ZERO.setScale(Money.SCALE), ccy);
    }

    private static AccountStatus mapStatusToDomain(AccountStatusJpa jpa) {
        return switch (jpa) {
            case ACTIVE -> AccountStatus.ACTIVE;
            case BLOCKED -> AccountStatus.BLOCKED;
            case CLOSED -> AccountStatus.CLOSED;
        };
    }

    private static AccountStatusJpa mapStatusToJpa(AccountStatus s) {
        return switch (s) {
            case ACTIVE -> AccountStatusJpa.ACTIVE;
            case BLOCKED -> AccountStatusJpa.BLOCKED;
            case CLOSED -> AccountStatusJpa.CLOSED;
        };
    }
}

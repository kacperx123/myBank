package com.app.mybank.infrastructure.stub;

import com.app.mybank.domain.account.Account;
import com.app.mybank.domain.account.AccountId;
import com.app.mybank.application.account.port.AccountRepository;
import com.app.mybank.domain.user.UserId;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Repository
@Profile("test")
public class InMemoryAccountRepository implements AccountRepository {

    private final Map<AccountId, Account> store = new ConcurrentHashMap<>();

    @Override
    public void save(Account account) {
        store.put(account.id(), account);
    }

    @Override
    public Optional<Account> findById(AccountId id) {
        return Optional.ofNullable(store.get(id));
    }

    @Override
    public List<Account> findByOwnerId(UserId ownerId) {
        return store.values().stream()
                .filter(acc -> acc.ownerId().equals(ownerId))
                .toList();
    }

    public void clear() {
        store.clear();
    }
}

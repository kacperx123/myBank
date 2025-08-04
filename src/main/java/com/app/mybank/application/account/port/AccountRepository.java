package com.app.mybank.application.account.port;

import com.app.mybank.application.transaction.command.account.Account;
import com.app.mybank.application.transaction.command.account.AccountId;
import com.app.mybank.domain.user.UserId;

import java.util.List;
import java.util.Optional;

/**
 * Port (abstrakcyjny interfejs) do trwałego przechowywania i pobierania kont.
 * <p>
 * Zależność kierunkowa: <b>domena → port</b>.  Implementacje (np. JPA, in-memory, Mongo) powstaną
 * w warstwie infrastruktury i będą wstrzykiwane do serwisów aplikacyjnych.
 * </p>
 */
public interface AccountRepository {

    void save(Account account);

    Optional<Account> findById(AccountId id);

    List<Account> findByOwnerId(UserId ownerId);
}

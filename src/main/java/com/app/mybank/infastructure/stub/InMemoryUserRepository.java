package com.app.mybank.infastructure.stub;

import com.app.mybank.domain.user.User;
import com.app.mybank.domain.user.UserId;
import com.app.mybank.application.user.port.UserRepository;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Nieskonfigurowana, w 100 % pamięciowa implementacja portu UserRepository.
 * <p>Idealna do testów jednostkowych; brak zależności od Springa.</p>
 */
@Repository
@Profile("test")
public class InMemoryUserRepository implements UserRepository {

    private final Map<UserId, User> byId      = new ConcurrentHashMap<>();
    private final Map<String,  User> byEmailL = new ConcurrentHashMap<>(); // klucz: email lower-case

    @Override
    public UserId save(User user) {
        byId.put(user.id(), user);
        byEmailL.put(user.email().toLowerCase(), user);
        return null;
    }

    @Override
    public Optional<User> findById(UserId id) {
        return Optional.ofNullable(byId.get(id));
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return Optional.ofNullable(byEmailL.get(email.toLowerCase()));
    }

    /** Pomocniczo w testach: czyści repozytorium. */
    public void clear() {
        byId.clear();
        byEmailL.clear();
    }
}

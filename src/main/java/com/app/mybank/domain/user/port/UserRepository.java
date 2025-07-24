package com.app.mybank.domain.user.port;

import com.app.mybank.domain.user.User;
import com.app.mybank.domain.user.UserId;

import java.util.Optional;

public interface UserRepository {
    void save(User user);
    Optional<User> findById(UserId id);
    Optional<User> findByEmail(String email);
}

package com.lz.bank.domain.port;

import com.lz.bank.domain.model.User;

import java.util.Optional;

public interface UserRepositoryPort {
    Optional<User> findById(Long id);
    User save(User user);
}

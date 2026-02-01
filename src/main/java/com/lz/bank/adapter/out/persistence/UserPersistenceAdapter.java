package com.lz.bank.adapter.out.persistence;

import com.lz.bank.adapter.out.persistence.entity.UserEntity;
import com.lz.bank.adapter.out.persistence.repository.UserJpaRepository;
import com.lz.bank.domain.model.User;
import com.lz.bank.domain.port.UserRepositoryPort;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class UserPersistenceAdapter implements UserRepositoryPort {
    private final UserJpaRepository userJpaRepository;

    public UserPersistenceAdapter(UserJpaRepository userJpaRepository) {
        this.userJpaRepository = userJpaRepository;
    }

    @Override
    public Optional<User> findById(Long id) {
        return userJpaRepository.findById(id).map(UserPersistenceAdapter::toDomain);
    }

    @Override
    public User save(User user) {
        UserEntity entity = toEntity(user);
        UserEntity saved = userJpaRepository.save(entity);
        return toDomain(saved);
    }

    private static User toDomain(UserEntity entity) {
        return new User(entity.getId(), entity.getFullName(), entity.getDocument(), entity.getEmail(), entity.getPassword(), entity.getType());
    }

    private static UserEntity toEntity(User user) {
        UserEntity entity = new UserEntity();
        entity.setId(user.id());
        entity.setFullName(user.fullName());
        entity.setDocument(user.document());
        entity.setEmail(user.email());
        entity.setPassword(user.password());
        entity.setType(user.type());
        return entity;
    }
}

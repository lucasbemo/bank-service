package com.lz.bank.adapter.out.persistence;

import com.lz.bank.adapter.out.persistence.entity.UserEntity;
import com.lz.bank.adapter.out.persistence.repository.UserJpaRepository;
import com.lz.bank.domain.model.User;
import com.lz.bank.domain.model.UserType;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class UserPersistenceAdapterTest {

    @Test
    void mapsEntityToDomainOnFind() {
        UserJpaRepository repository = mock(UserJpaRepository.class);
        UserPersistenceAdapter adapter = new UserPersistenceAdapter(repository);

        UserEntity entity = new UserEntity();
        entity.setId(1L);
        entity.setFullName("Ada Lovelace");
        entity.setDocument("123");
        entity.setEmail("ada@example.com");
        entity.setPassword("secret");
        entity.setType(UserType.COMMON);

        when(repository.findById(1L)).thenReturn(Optional.of(entity));

        Optional<User> result = adapter.findById(1L);

        assertThat(result).isPresent();
        User user = result.get();
        assertThat(user.id()).isEqualTo(1L);
        assertThat(user.fullName()).isEqualTo("Ada Lovelace");
        assertThat(user.document()).isEqualTo("123");
        assertThat(user.email()).isEqualTo("ada@example.com");
        assertThat(user.password()).isEqualTo("secret");
        assertThat(user.type()).isEqualTo(UserType.COMMON);
    }

    @Test
    void mapsDomainToEntityOnSave() {
        UserJpaRepository repository = mock(UserJpaRepository.class);
        UserPersistenceAdapter adapter = new UserPersistenceAdapter(repository);

        User toSave = new User(null, "Grace Hopper", "456", "grace@example.com", "pwd", UserType.SHOPKEEPER);
        UserEntity saved = new UserEntity();
        saved.setId(10L);
        saved.setFullName("Grace Hopper");
        saved.setDocument("456");
        saved.setEmail("grace@example.com");
        saved.setPassword("pwd");
        saved.setType(UserType.SHOPKEEPER);

        when(repository.save(any(UserEntity.class))).thenReturn(saved);

        User result = adapter.save(toSave);

        ArgumentCaptor<UserEntity> captor = ArgumentCaptor.forClass(UserEntity.class);
        verify(repository).save(captor.capture());
        UserEntity captured = captor.getValue();
        assertThat(captured.getFullName()).isEqualTo("Grace Hopper");
        assertThat(captured.getDocument()).isEqualTo("456");
        assertThat(captured.getEmail()).isEqualTo("grace@example.com");
        assertThat(captured.getPassword()).isEqualTo("pwd");
        assertThat(captured.getType()).isEqualTo(UserType.SHOPKEEPER);

        assertThat(result.id()).isEqualTo(10L);
        assertThat(result.email()).isEqualTo("grace@example.com");
    }
}

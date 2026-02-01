package com.lz.bank.adapter.out.persistence.entity;

import com.lz.bank.domain.model.UserType;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class UserEntityTest {

    @Test
    void storesUserFields() {
        UserEntity entity = new UserEntity();
        entity.setId(1L);
        entity.setFullName("Marie Curie");
        entity.setDocument("999");
        entity.setEmail("marie@example.com");
        entity.setPassword("secret");
        entity.setType(UserType.COMMON);

        assertThat(entity.getId()).isEqualTo(1L);
        assertThat(entity.getFullName()).isEqualTo("Marie Curie");
        assertThat(entity.getDocument()).isEqualTo("999");
        assertThat(entity.getEmail()).isEqualTo("marie@example.com");
        assertThat(entity.getPassword()).isEqualTo("secret");
        assertThat(entity.getType()).isEqualTo(UserType.COMMON);
    }
}

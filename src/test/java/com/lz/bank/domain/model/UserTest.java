package com.lz.bank.domain.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class UserTest {

    @Test
    void exposesUserFields() {
        User user = new User(12L, "Alan Turing", "doc", "alan@example.com", "pwd", UserType.COMMON);

        assertThat(user.id()).isEqualTo(12L);
        assertThat(user.fullName()).isEqualTo("Alan Turing");
        assertThat(user.document()).isEqualTo("doc");
        assertThat(user.email()).isEqualTo("alan@example.com");
        assertThat(user.password()).isEqualTo("pwd");
        assertThat(user.type()).isEqualTo(UserType.COMMON);
    }
}

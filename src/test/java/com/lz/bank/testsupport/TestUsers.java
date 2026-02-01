package com.lz.bank.testsupport;

import com.lz.bank.domain.model.User;
import com.lz.bank.domain.model.UserType;

public final class TestUsers {
    private TestUsers() {
    }

    public static User common(Long id) {
        return new User(id, "Example User", "00000000000", "user@example.com", "secret", UserType.COMMON);
    }

    public static User shopkeeper(Long id) {
        return new User(id, "Example Shop", "00000000002", "shop@example.com", "secret", UserType.SHOPKEEPER);
    }
}

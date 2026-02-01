package com.lz.bank.domain.model;

public final class User {
    private final Long id;
    private final String fullName;
    private final String document;
    private final String email;
    private final String password;
    private final UserType type;

    public User(Long id, String fullName, String document, String email, String password, UserType type) {
        this.id = id;
        this.fullName = fullName;
        this.document = document;
        this.email = email;
        this.password = password;
        this.type = type;
    }

    public Long id() {
        return id;
    }

    public String fullName() {
        return fullName;
    }

    public String document() {
        return document;
    }

    public String email() {
        return email;
    }

    public String password() {
        return password;
    }

    public UserType type() {
        return type;
    }
}

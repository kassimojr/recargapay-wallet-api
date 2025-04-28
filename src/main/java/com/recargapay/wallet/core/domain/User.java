package com.recargapay.wallet.core.domain;

import java.util.UUID;

public class User {
    private UUID id;
    private String email;
    private String name;

    public User(UUID id, String email, String name) {
        this.id = id;
        this.email = email;
        this.name = name;
    }

    public User () {}

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}


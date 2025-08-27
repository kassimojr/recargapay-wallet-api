package com.digital.wallet.core.domain;

import org.junit.jupiter.api.Test;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

class UserTest {
    @Test
    void testAllArgsConstructorAndGetters() {
        UUID id = UUID.randomUUID();
        String name = "Test User";
        String email = "test@example.com";
        User user = new User(id, email, name);
        assertEquals(id, user.getId());
        assertEquals(name, user.getName());
        assertEquals(email, user.getEmail());
    }

    @Test
    void testSetters() {
        User user = new User();
        UUID id = UUID.randomUUID();
        String name = "Another User";
        String email = "another@example.com";
        user.setId(id);
        user.setName(name);
        user.setEmail(email);
        assertEquals(id, user.getId());
        assertEquals(name, user.getName());
        assertEquals(email, user.getEmail());
    }
}

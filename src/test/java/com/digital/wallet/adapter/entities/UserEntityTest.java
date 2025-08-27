package com.digital.wallet.adapter.entities;

import org.junit.jupiter.api.Test;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

class UserEntityTest {
    @Test
    void testGettersAndSetters() {
        UUID id = UUID.randomUUID();
        String email = "user@email.com";
        String name = "User Name";
        UserEntity entity = new UserEntity();
        entity.setId(id);
        entity.setEmail(email);
        entity.setName(name);
        assertEquals(id, entity.getId());
        assertEquals(email, entity.getEmail());
        assertEquals(name, entity.getName());
    }

    @Test
    void testBuilderAndAllArgsConstructor() {
        UUID id = UUID.randomUUID();
        String email = "builder@email.com";
        String name = "Builder Name";
        UserEntity entity1 = UserEntity.builder()
                .id(id)
                .email(email)
                .name(name)
                .build();
        assertEquals(id, entity1.getId());
        assertEquals(email, entity1.getEmail());
        assertEquals(name, entity1.getName());

        UserEntity entity2 = new UserEntity(id, email, name);
        assertEquals(id, entity2.getId());
        assertEquals(email, entity2.getEmail());
        assertEquals(name, entity2.getName());
    }

    @Test
    void testNoArgsConstructor() {
        UserEntity entity = new UserEntity();
        assertNull(entity.getId());
        assertNull(entity.getEmail());
        assertNull(entity.getName());
    }
}

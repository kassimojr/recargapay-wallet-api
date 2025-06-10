package com.recargapay.wallet.adapter.entities;

import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class WalletEntityTest {
    @Test
    void testGettersAndSetters() {
        UserEntity user = UserEntity.builder()
                .id(UUID.randomUUID())
                .email("user@example.com")
                .name("User Test")
                .build();
        UUID id = UUID.randomUUID();
        BigDecimal balance = new BigDecimal("100.50");
        LocalDateTime createdAt = LocalDateTime.now();
        LocalDateTime updatedAt = LocalDateTime.now();

        WalletEntity entity = new WalletEntity();
        entity.setId(id);
        entity.setUser(user);
        entity.setBalance(balance);
        entity.setCreatedAt(createdAt);
        entity.setUpdatedAt(updatedAt);

        assertEquals(id, entity.getId());
        assertEquals(user, entity.getUser());
        assertEquals(balance, entity.getBalance());
        assertEquals(createdAt, entity.getCreatedAt());
        assertEquals(updatedAt, entity.getUpdatedAt());
    }

    @Test
    void testBuilderAndAllArgsConstructor() {
        UserEntity user = UserEntity.builder()
                .id(UUID.randomUUID())
                .email("builder@example.com")
                .name("Builder User")
                .build();
        UUID id = UUID.randomUUID();
        BigDecimal balance = new BigDecimal("200.00");
        LocalDateTime createdAt = LocalDateTime.now();
        LocalDateTime updatedAt = LocalDateTime.now();

        WalletEntity entity1 = WalletEntity.builder()
                .id(id)
                .user(user)
                .balance(balance)
                .createdAt(createdAt)
                .updatedAt(updatedAt)
                .build();
        assertEquals(id, entity1.getId());
        assertEquals(user, entity1.getUser());
        assertEquals(balance, entity1.getBalance());
        assertEquals(createdAt, entity1.getCreatedAt());
        assertEquals(updatedAt, entity1.getUpdatedAt());

        WalletEntity entity2 = new WalletEntity(id, user, balance, createdAt, updatedAt);
        assertEquals(id, entity2.getId());
        assertEquals(user, entity2.getUser());
        assertEquals(balance, entity2.getBalance());
        assertEquals(createdAt, entity2.getCreatedAt());
        assertEquals(updatedAt, entity2.getUpdatedAt());
    }

    @Test
    void testNoArgsConstructor() {
        WalletEntity entity = new WalletEntity();
        assertNull(entity.getId());
        assertNull(entity.getUser());
        assertNull(entity.getBalance());
        assertNull(entity.getCreatedAt());
        assertNull(entity.getUpdatedAt());
    }
    
    @Test
    void testPrePersistSetsCreatedAtAndUpdatedAt() {
        WalletEntity entity = new WalletEntity();
        assertNull(entity.getCreatedAt());
        assertNull(entity.getUpdatedAt());
        
        entity.onCreate();
        
        assertNotNull(entity.getCreatedAt());
        assertNotNull(entity.getUpdatedAt());
        assertTrue(entity.getUpdatedAt().equals(entity.getCreatedAt()) || 
                   entity.getUpdatedAt().isAfter(entity.getCreatedAt()));
    }
    
    @Test
    void testPreUpdateSetsUpdatedAt() {
        WalletEntity entity = new WalletEntity();
        LocalDateTime initialTime = LocalDateTime.now().minusMinutes(5);
        entity.setUpdatedAt(initialTime);
        assertEquals(initialTime, entity.getUpdatedAt());
        
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        entity.onUpdate();
        
        assertNotNull(entity.getUpdatedAt());
        assertTrue(entity.getUpdatedAt().isAfter(initialTime));
    }
}

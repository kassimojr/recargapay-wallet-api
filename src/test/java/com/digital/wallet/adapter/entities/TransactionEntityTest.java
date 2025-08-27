package com.digital.wallet.adapter.entities;

import com.digital.wallet.core.domain.TransactionType;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

class TransactionEntityTest {
    @Test
    void testGettersAndSetters() {
        UUID id = UUID.randomUUID();
        WalletEntity wallet = WalletEntity.builder().id(UUID.randomUUID()).build();
        BigDecimal amount = new BigDecimal("50.00");
        TransactionType type = TransactionType.DEPOSIT;
        LocalDateTime timestamp = LocalDateTime.now();
        UUID relatedUserId = UUID.randomUUID();
        LocalDateTime createdAt = LocalDateTime.now();
        LocalDateTime updatedAt = LocalDateTime.now();

        TransactionEntity entity = new TransactionEntity();
        entity.setId(id);
        entity.setWallet(wallet);
        entity.setAmount(amount);
        entity.setType(type);
        entity.setTimestamp(timestamp);
        entity.setRelatedUserId(relatedUserId);
        entity.setCreatedAt(createdAt);
        entity.setUpdatedAt(updatedAt);

        assertEquals(id, entity.getId());
        assertEquals(wallet, entity.getWallet());
        assertEquals(amount, entity.getAmount());
        assertEquals(type, entity.getType());
        assertEquals(timestamp, entity.getTimestamp());
        assertEquals(relatedUserId, entity.getRelatedUserId());
        assertEquals(createdAt, entity.getCreatedAt());
        assertEquals(updatedAt, entity.getUpdatedAt());
    }

    @Test
    void testBuilderAndAllArgsConstructor() {
        UUID id = UUID.randomUUID();
        WalletEntity wallet = WalletEntity.builder().id(UUID.randomUUID()).build();
        BigDecimal amount = new BigDecimal("75.00");
        TransactionType type = TransactionType.WITHDRAW;
        LocalDateTime timestamp = LocalDateTime.now();
        UUID relatedUserId = UUID.randomUUID();
        LocalDateTime createdAt = LocalDateTime.now();
        LocalDateTime updatedAt = LocalDateTime.now();

        TransactionEntity entity1 = TransactionEntity.builder()
                .id(id)
                .wallet(wallet)
                .amount(amount)
                .type(type)
                .timestamp(timestamp)
                .relatedUserId(relatedUserId)
                .createdAt(createdAt)
                .updatedAt(updatedAt)
                .build();
        assertEquals(id, entity1.getId());
        assertEquals(wallet, entity1.getWallet());
        assertEquals(amount, entity1.getAmount());
        assertEquals(type, entity1.getType());
        assertEquals(timestamp, entity1.getTimestamp());
        assertEquals(relatedUserId, entity1.getRelatedUserId());
        assertEquals(createdAt, entity1.getCreatedAt());
        assertEquals(updatedAt, entity1.getUpdatedAt());

        TransactionEntity entity2 = new TransactionEntity(id, wallet, amount, type, timestamp, relatedUserId, createdAt, updatedAt);
        assertEquals(id, entity2.getId());
        assertEquals(wallet, entity2.getWallet());
        assertEquals(amount, entity2.getAmount());
        assertEquals(type, entity2.getType());
        assertEquals(timestamp, entity2.getTimestamp());
        assertEquals(relatedUserId, entity2.getRelatedUserId());
        assertEquals(createdAt, entity2.getCreatedAt());
        assertEquals(updatedAt, entity2.getUpdatedAt());
    }

    @Test
    void testNoArgsConstructor() {
        TransactionEntity entity = new TransactionEntity();
        assertNull(entity.getId());
        assertNull(entity.getWallet());
        assertNull(entity.getAmount());
        assertNull(entity.getType());
        assertNull(entity.getTimestamp());
        assertNull(entity.getRelatedUserId());
        assertNull(entity.getCreatedAt());
        assertNull(entity.getUpdatedAt());
    }
    
    @Test
    void testPrePersistAndPreUpdate() {
        TransactionEntity entity = new TransactionEntity();
        entity.setId(UUID.randomUUID());
        entity.setAmount(BigDecimal.TEN);
        entity.setType(TransactionType.DEPOSIT);
        
        entity.onCreate();
        assertNotNull(entity.getCreatedAt());
        assertNotNull(entity.getUpdatedAt());
        
        LocalDateTime initialUpdatedAt = entity.getUpdatedAt();
        LocalDateTime originalCreatedAt = entity.getCreatedAt();
        
        entity.setUpdatedAt(initialUpdatedAt.plusNanos(1000));
        
        entity.onUpdate();
        assertEquals(originalCreatedAt, entity.getCreatedAt()); 
        assertTrue(entity.getUpdatedAt().isAfter(initialUpdatedAt)); 
    }
}

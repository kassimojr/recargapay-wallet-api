package com.recargapay.wallet.adapter.entities;

import com.recargapay.wallet.core.domain.TransactionType;
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

        TransactionEntity entity = new TransactionEntity();
        entity.setId(id);
        entity.setWallet(wallet);
        entity.setAmount(amount);
        entity.setType(type);
        entity.setTimestamp(timestamp);
        entity.setRelatedUserId(relatedUserId);

        assertEquals(id, entity.getId());
        assertEquals(wallet, entity.getWallet());
        assertEquals(amount, entity.getAmount());
        assertEquals(type, entity.getType());
        assertEquals(timestamp, entity.getTimestamp());
        assertEquals(relatedUserId, entity.getRelatedUserId());
    }

    @Test
    void testBuilderAndAllArgsConstructor() {
        UUID id = UUID.randomUUID();
        WalletEntity wallet = WalletEntity.builder().id(UUID.randomUUID()).build();
        BigDecimal amount = new BigDecimal("75.00");
        TransactionType type = TransactionType.WITHDRAW;
        LocalDateTime timestamp = LocalDateTime.now();
        UUID relatedUserId = UUID.randomUUID();

        TransactionEntity entity1 = TransactionEntity.builder()
                .id(id)
                .wallet(wallet)
                .amount(amount)
                .type(type)
                .timestamp(timestamp)
                .relatedUserId(relatedUserId)
                .build();
        assertEquals(id, entity1.getId());
        assertEquals(wallet, entity1.getWallet());
        assertEquals(amount, entity1.getAmount());
        assertEquals(type, entity1.getType());
        assertEquals(timestamp, entity1.getTimestamp());
        assertEquals(relatedUserId, entity1.getRelatedUserId());

        TransactionEntity entity2 = new TransactionEntity(id, wallet, amount, type, timestamp, relatedUserId);
        assertEquals(id, entity2.getId());
        assertEquals(wallet, entity2.getWallet());
        assertEquals(amount, entity2.getAmount());
        assertEquals(type, entity2.getType());
        assertEquals(timestamp, entity2.getTimestamp());
        assertEquals(relatedUserId, entity2.getRelatedUserId());
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
    }
}

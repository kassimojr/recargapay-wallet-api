package com.digital.wallet.core.domain;

import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

class TransactionTest {
    @Test
    void testAllArgsConstructorAndGetters() {
        UUID id = UUID.randomUUID();
        UUID walletId = UUID.randomUUID();
        BigDecimal amount = new BigDecimal("50.00");
        TransactionType type = TransactionType.DEPOSIT;
        LocalDateTime timestamp = LocalDateTime.now();
        UUID relatedUserId = UUID.randomUUID();
        Transaction tx = new Transaction(id, walletId, amount, type, timestamp, relatedUserId);
        assertEquals(id, tx.getId());
        assertEquals(walletId, tx.getWalletId());
        assertEquals(amount, tx.getAmount());
        assertEquals(type, tx.getType());
        assertEquals(timestamp, tx.getTimestamp());
        assertEquals(relatedUserId, tx.getRelatedUserId());
    }

    @Test
    void testSetters() {
        Transaction tx = new Transaction();
        UUID id = UUID.randomUUID();
        UUID walletId = UUID.randomUUID();
        BigDecimal amount = new BigDecimal("75.00");
        TransactionType type = TransactionType.WITHDRAW;
        LocalDateTime timestamp = LocalDateTime.now();
        UUID relatedUserId = UUID.randomUUID();
        tx.setId(id);
        tx.setWalletId(walletId);
        tx.setAmount(amount);
        tx.setType(type);
        tx.setTimestamp(timestamp);
        tx.setRelatedUserId(relatedUserId);
        assertEquals(id, tx.getId());
        assertEquals(walletId, tx.getWalletId());
        assertEquals(amount, tx.getAmount());
        assertEquals(type, tx.getType());
        assertEquals(timestamp, tx.getTimestamp());
        assertEquals(relatedUserId, tx.getRelatedUserId());
    }
}

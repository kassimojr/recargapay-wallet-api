package com.recargapay.wallet.core.domain;

import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

class WalletTest {
    @Test
    void testAllArgsConstructorAndGetters() {
        UUID id = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        BigDecimal balance = new BigDecimal("100.00");
        Wallet wallet = new Wallet(id, userId, balance);
        assertEquals(id, wallet.getId());
        assertEquals(userId, wallet.getUserId());
        assertEquals(balance, wallet.getBalance());
    }

    @Test
    void testSetters() {
        Wallet wallet = new Wallet();
        UUID id = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        BigDecimal balance = new BigDecimal("200.00");
        wallet.setId(id);
        wallet.setUserId(userId);
        wallet.setBalance(balance);
        assertEquals(id, wallet.getId());
        assertEquals(userId, wallet.getUserId());
        assertEquals(balance, wallet.getBalance());
    }
}

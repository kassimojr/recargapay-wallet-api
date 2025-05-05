package com.recargapay.wallet.adapter.dtos;

import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

class WalletDTOTest {
    @Test
    void gettersAndSetters() {
        UUID id = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        BigDecimal balance = new BigDecimal("99.99");

        WalletDTO dto = new WalletDTO(id, userId, balance);
        assertEquals(id, dto.getId());
        assertEquals(userId, dto.getUserId());
        assertEquals(balance, dto.getBalance());
    }
}

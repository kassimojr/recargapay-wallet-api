package com.recargapay.wallet.adapter.dtos;

import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

class TransferRequestDTOTest {
    @Test
    void gettersAndSetters() {
        UUID fromWalletId = UUID.randomUUID();
        UUID toWalletId = UUID.randomUUID();
        BigDecimal amount = new BigDecimal("123.45");

        TransferRequestDTO dto = new TransferRequestDTO(fromWalletId, toWalletId, amount);
        assertEquals(fromWalletId, dto.getFromWalletId());
        assertEquals(toWalletId, dto.getToWalletId());
        assertEquals(amount, dto.getAmount());
    }
}

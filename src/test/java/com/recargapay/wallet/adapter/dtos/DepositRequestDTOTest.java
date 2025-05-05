package com.recargapay.wallet.adapter.dtos;

import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

class DepositRequestDTOTest {
    @Test
    void gettersAndSetters() {
        UUID walletId = UUID.randomUUID();
        BigDecimal amount = BigDecimal.TEN;

        DepositRequestDTO dto = new DepositRequestDTO(walletId, amount);
        assertEquals(walletId, dto.getWalletId());
        assertEquals(amount, dto.getAmount());
    }
}

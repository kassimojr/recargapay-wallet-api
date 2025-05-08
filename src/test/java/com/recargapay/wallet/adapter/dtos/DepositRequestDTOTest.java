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

    @Test
    void builderShouldBuildCorrectly() {
        UUID walletId = UUID.randomUUID();
        BigDecimal amount = new BigDecimal("20.00");
        DepositRequestDTO dto = DepositRequestDTO.builder()
                .walletId(walletId)
                .amount(amount)
                .build();
        assertEquals(walletId, dto.getWalletId());
        assertEquals(amount, dto.getAmount());
    }

    @Test
    void noArgsConstructorAndSetters() {
        UUID walletId = UUID.randomUUID();
        BigDecimal amount = new BigDecimal("30.00");
        DepositRequestDTO dto = new DepositRequestDTO();
        dto.setWalletId(walletId);
        dto.setAmount(amount);
        assertEquals(walletId, dto.getWalletId());
        assertEquals(amount, dto.getAmount());
    }
}

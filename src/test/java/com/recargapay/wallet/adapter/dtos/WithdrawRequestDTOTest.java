package com.recargapay.wallet.adapter.dtos;

import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

class WithdrawRequestDTOTest {
    @Test
    void gettersAndSetters() {
        UUID walletId = UUID.randomUUID();
        BigDecimal amount = BigDecimal.ONE;

        WithdrawRequestDTO dto = new WithdrawRequestDTO(walletId, amount);
        assertEquals(walletId, dto.getWalletId());
        assertEquals(amount, dto.getAmount());
    }

    @Test
    void builderShouldBuildCorrectly() {
        UUID walletId = UUID.randomUUID();
        BigDecimal amount = new BigDecimal("15.00");
        WithdrawRequestDTO dto = WithdrawRequestDTO.builder()
                .walletId(walletId)
                .amount(amount)
                .build();
        assertEquals(walletId, dto.getWalletId());
        assertEquals(amount, dto.getAmount());
    }

    @Test
    void noArgsConstructorAndSetters() {
        UUID walletId = UUID.randomUUID();
        BigDecimal amount = new BigDecimal("25.00");
        WithdrawRequestDTO dto = new WithdrawRequestDTO();
        dto.setWalletId(walletId);
        dto.setAmount(amount);
        assertEquals(walletId, dto.getWalletId());
        assertEquals(amount, dto.getAmount());
    }
}

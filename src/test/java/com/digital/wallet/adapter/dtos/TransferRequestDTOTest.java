package com.digital.wallet.adapter.dtos;

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

    @Test
    void builderShouldBuildCorrectly() {
        UUID fromWalletId = UUID.randomUUID();
        UUID toWalletId = UUID.randomUUID();
        BigDecimal amount = new BigDecimal("200.00");
        TransferRequestDTO dto = TransferRequestDTO.builder()
                .fromWalletId(fromWalletId)
                .toWalletId(toWalletId)
                .amount(amount)
                .build();
        assertEquals(fromWalletId, dto.getFromWalletId());
        assertEquals(toWalletId, dto.getToWalletId());
        assertEquals(amount, dto.getAmount());
    }

    @Test
    void noArgsConstructorAndSetters() {
        UUID fromWalletId = UUID.randomUUID();
        UUID toWalletId = UUID.randomUUID();
        BigDecimal amount = new BigDecimal("300.00");
        TransferRequestDTO dto = new TransferRequestDTO();
        dto.setFromWalletId(fromWalletId);
        dto.setToWalletId(toWalletId);
        dto.setAmount(amount);
        assertEquals(fromWalletId, dto.getFromWalletId());
        assertEquals(toWalletId, dto.getToWalletId());
        assertEquals(amount, dto.getAmount());
    }
}

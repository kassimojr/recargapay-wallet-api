package com.recargapay.wallet.adapter.dtos;

import com.recargapay.wallet.core.domain.TransactionType;
import java.math.BigDecimal;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import static org.junit.jupiter.api.Assertions.*;

class TransactionDTOTest {
    @Test
    void gettersAndSetters() {
        UUID id = UUID.randomUUID();
        UUID walletId = UUID.randomUUID();
        BigDecimal amount = BigDecimal.TEN;
        String type = TransactionType.DEPOSIT.name();
        LocalDateTime timestamp = LocalDateTime.now();
        UUID relatedUserId = UUID.randomUUID();

        TransactionDTO dto = new TransactionDTO(id, walletId, amount, type, timestamp, relatedUserId);
        assertEquals(id, dto.getId());
        assertEquals(walletId, dto.getWalletId());
        assertEquals(relatedUserId, dto.getRelatedUserId());
        assertEquals(amount, dto.getAmount());
    }

    @Test
    void builderShouldBuildCorrectly() {
        UUID id = UUID.randomUUID();
        UUID walletId = UUID.randomUUID();
        BigDecimal amount = BigDecimal.ONE;
        String type = TransactionType.WITHDRAW.name();
        LocalDateTime timestamp = LocalDateTime.now();
        UUID relatedUserId = UUID.randomUUID();
        TransactionDTO dto = TransactionDTO.builder()
                .id(id)
                .walletId(walletId)
                .amount(amount)
                .type(type)
                .timestamp(timestamp)
                .relatedUserId(relatedUserId)
                .build();
        assertEquals(id, dto.getId());
        assertEquals(walletId, dto.getWalletId());
        assertEquals(amount, dto.getAmount());
        assertEquals(type, dto.getType());
        assertEquals(timestamp, dto.getTimestamp());
        assertEquals(relatedUserId, dto.getRelatedUserId());
    }

    @Test
    void noArgsConstructorAndSetters() {
        UUID id = UUID.randomUUID();
        UUID walletId = UUID.randomUUID();
        BigDecimal amount = BigDecimal.valueOf(99);
        String type = TransactionType.DEPOSIT.name();
        LocalDateTime timestamp = LocalDateTime.now();
        UUID relatedUserId = UUID.randomUUID();
        TransactionDTO dto = new TransactionDTO();
        dto.setId(id);
        dto.setWalletId(walletId);
        dto.setAmount(amount);
        dto.setType(type);
        dto.setTimestamp(timestamp);
        dto.setRelatedUserId(relatedUserId);
        assertEquals(id, dto.getId());
        assertEquals(walletId, dto.getWalletId());
        assertEquals(amount, dto.getAmount());
        assertEquals(type, dto.getType());
        assertEquals(timestamp, dto.getTimestamp());
        assertEquals(relatedUserId, dto.getRelatedUserId());
    }
}

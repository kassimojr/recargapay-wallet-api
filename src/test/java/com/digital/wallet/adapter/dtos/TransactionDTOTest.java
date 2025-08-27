package com.digital.wallet.adapter.dtos;

import com.digital.wallet.core.domain.TransactionType;
import java.math.BigDecimal;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import static org.junit.jupiter.api.Assertions.*;

class TransactionDTOTest {
    @Test
    void constructorAndGettersShouldWorkCorrectly() {
        UUID id = UUID.randomUUID();
        UUID walletId = UUID.randomUUID();
        BigDecimal amount = new BigDecimal("50.00");
        String type = TransactionType.DEPOSIT.name();
        LocalDateTime timestamp = LocalDateTime.now();
        UUID relatedUserId = UUID.randomUUID();
        String relatedUserName = "John Doe";

        TransactionDTO dto = new TransactionDTO(id, walletId, amount, type, timestamp, relatedUserId, relatedUserName);
        assertEquals(id, dto.getId());
        assertEquals(walletId, dto.getWalletId());
        assertEquals(amount, dto.getAmount());
        assertEquals(type, dto.getType());
        assertEquals(timestamp, dto.getTimestamp());
        assertEquals(relatedUserId, dto.getRelatedUserId());
        assertEquals(relatedUserName, dto.getRelatedUserName());
    }

    @Test
    void builderShouldBuildCorrectly() {
        UUID id = UUID.randomUUID();
        UUID walletId = UUID.randomUUID();
        BigDecimal amount = new BigDecimal("50.00");
        String type = TransactionType.WITHDRAW.name();
        LocalDateTime timestamp = LocalDateTime.now();
        UUID relatedUserId = UUID.randomUUID();
        String relatedUserName = "Jane Doe";

        TransactionDTO dto = TransactionDTO.builder()
                .id(id)
                .walletId(walletId)
                .amount(amount)
                .type(type)
                .timestamp(timestamp)
                .relatedUserId(relatedUserId)
                .relatedUserName(relatedUserName)
                .build();
        assertEquals(id, dto.getId());
        assertEquals(walletId, dto.getWalletId());
        assertEquals(amount, dto.getAmount());
        assertEquals(type, dto.getType());
        assertEquals(timestamp, dto.getTimestamp());
        assertEquals(relatedUserId, dto.getRelatedUserId());
        assertEquals(relatedUserName, dto.getRelatedUserName());
    }

    @Test
    void noArgsConstructorAndSettersShouldWorkCorrectly() {
        UUID id = UUID.randomUUID();
        UUID walletId = UUID.randomUUID();
        BigDecimal amount = new BigDecimal("25.00");
        String type = TransactionType.DEPOSIT.name();
        LocalDateTime timestamp = LocalDateTime.now();
        UUID relatedUserId = UUID.randomUUID();
        String relatedUserName = "Alice Smith";

        TransactionDTO dto = new TransactionDTO();
        dto.setId(id);
        dto.setWalletId(walletId);
        dto.setAmount(amount);
        dto.setType(type);
        dto.setTimestamp(timestamp);
        dto.setRelatedUserId(relatedUserId);
        dto.setRelatedUserName(relatedUserName);
        assertEquals(id, dto.getId());
        assertEquals(walletId, dto.getWalletId());
        assertEquals(amount, dto.getAmount());
        assertEquals(type, dto.getType());
        assertEquals(timestamp, dto.getTimestamp());
        assertEquals(relatedUserId, dto.getRelatedUserId());
        assertEquals(relatedUserName, dto.getRelatedUserName());
    }
}

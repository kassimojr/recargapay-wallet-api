package com.recargapay.wallet.adapter.converters;

import com.recargapay.wallet.adapter.dtos.TransactionDTO;
import com.recargapay.wallet.adapter.entities.TransactionEntity;
import com.recargapay.wallet.adapter.entities.WalletEntity;
import com.recargapay.wallet.core.domain.Transaction;
import com.recargapay.wallet.core.domain.TransactionType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

class TransactionMapperTest {
    private TransactionMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new TransactionMapper();
    }

    @Test
    void toDomain_shouldMapFields() {
        TransactionEntity entity = new TransactionEntity();
        UUID id = UUID.randomUUID();
        UUID walletId = UUID.randomUUID();
        BigDecimal amount = new BigDecimal("100.00");
        TransactionType type = TransactionType.DEPOSIT;
        LocalDateTime timestamp = LocalDateTime.now();
        UUID relatedUserId = UUID.randomUUID();
        WalletEntity walletEntity = new WalletEntity();
        walletEntity.setId(walletId);
        entity.setId(id);
        entity.setWallet(walletEntity);
        entity.setAmount(amount);
        entity.setType(type);
        entity.setTimestamp(timestamp);
        entity.setRelatedUserId(relatedUserId);
        Transaction tx = mapper.toDomain(entity);
        assertNotNull(tx);
        assertEquals(id, tx.getId());
        assertEquals(walletId, tx.getWalletId());
        assertEquals(amount, tx.getAmount());
        assertEquals(type, tx.getType());
        assertEquals(timestamp, tx.getTimestamp());
        assertEquals(relatedUserId, tx.getRelatedUserId());
    }

    @Test
    void toDTO_shouldMapFields() {
        UUID id = UUID.randomUUID();
        UUID walletId = UUID.randomUUID();
        BigDecimal amount = new BigDecimal("100.00");
        TransactionType type = TransactionType.DEPOSIT;
        LocalDateTime timestamp = LocalDateTime.now();
        UUID relatedUserId = UUID.randomUUID();
        Transaction tx = new Transaction(id, walletId, amount, type, timestamp, relatedUserId);
        TransactionDTO dto = mapper.toDTO(tx);
        assertNotNull(dto);
        assertEquals(id, dto.getId());
        assertEquals(walletId, dto.getWalletId());
        assertEquals(amount, dto.getAmount());
        assertEquals(type.toString(), dto.getType());
        assertEquals(timestamp, dto.getTimestamp());
        assertEquals(relatedUserId, dto.getRelatedUserId());
    }

    @Test
    void toDTOList_shouldMapList() {
        UUID id1 = UUID.randomUUID();
        UUID id2 = UUID.randomUUID();
        UUID walletId = UUID.randomUUID();
        Transaction tx1 = new Transaction(id1, walletId, BigDecimal.ONE, TransactionType.DEPOSIT, LocalDateTime.now(), UUID.randomUUID());
        Transaction tx2 = new Transaction(id2, walletId, BigDecimal.TEN, TransactionType.WITHDRAW, LocalDateTime.now(), UUID.randomUUID());
        List<Transaction> transactions = List.of(tx1, tx2);
        List<TransactionDTO> dtos = mapper.toDTOList(transactions);
        assertEquals(2, dtos.size());
        assertEquals(id1, dtos.get(0).getId());
        assertEquals(id2, dtos.get(1).getId());
    }

    @Test
    void toDTOList_withNull_shouldReturnEmptyList() {
        List<TransactionDTO> dtos = mapper.toDTOList(null);
        assertNotNull(dtos);
        assertTrue(dtos.isEmpty());
    }

    @Test
    void toDomain_withNull_shouldReturnNull() {
        Transaction result = mapper.toDomain(null);
        assertNull(result);
    }

    @Test
    void toDTO_withNull_shouldReturnNull() {
        TransactionDTO result = mapper.toDTO(null);
        assertNull(result);
    }
}

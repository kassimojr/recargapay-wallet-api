package com.recargapay.wallet.adapter.converters;

import com.recargapay.wallet.adapter.dtos.TransactionDTO;
import com.recargapay.wallet.adapter.entities.TransactionEntity;
import com.recargapay.wallet.adapter.entities.WalletEntity;
import com.recargapay.wallet.core.domain.Transaction;
import com.recargapay.wallet.core.domain.TransactionType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.modelmapper.ModelMapper;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

class TransactionMapperTest {
    private TransactionMapper mapper;
    private ModelMapper modelMapper;

    @BeforeEach
    void setUp() {
        modelMapper = new ModelMapper();
        mapper = new TransactionMapper(modelMapper);
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
    void toEntity_shouldMapFields() {
        UUID id = UUID.randomUUID();
        UUID walletId = UUID.randomUUID();
        BigDecimal amount = new BigDecimal("100.00");
        TransactionType type = TransactionType.DEPOSIT;
        LocalDateTime timestamp = LocalDateTime.now();
        UUID relatedUserId = UUID.randomUUID();
        Transaction tx = new Transaction(id, walletId, amount, type, timestamp, relatedUserId);
        TransactionEntity entity = mapper.toEntity(tx);
        assertNotNull(entity);
        assertEquals(id, entity.getId());
        assertNotNull(entity.getWallet());
        assertEquals(walletId, entity.getWallet().getId());
        assertEquals(amount, entity.getAmount());
        assertEquals(type, entity.getType());
        assertEquals(timestamp, entity.getTimestamp());
        assertEquals(relatedUserId, entity.getRelatedUserId());
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
        assertEquals(type.name(), dto.getType());
        assertEquals(timestamp, dto.getTimestamp());
        assertEquals(relatedUserId, dto.getRelatedUserId());
    }

    @Test
    void toDTOList_shouldMapList() {
        UUID id = UUID.randomUUID();
        UUID walletId = UUID.randomUUID();
        BigDecimal amount = new BigDecimal("100.00");
        TransactionType type = TransactionType.DEPOSIT;
        LocalDateTime timestamp = LocalDateTime.now();
        UUID relatedUserId = UUID.randomUUID();
        Transaction tx = new Transaction(id, walletId, amount, type, timestamp, relatedUserId);
        List<TransactionDTO> dtos = mapper.toDTOList(List.of(tx));
        assertNotNull(dtos);
        assertEquals(1, dtos.size());
        assertEquals(id, dtos.get(0).getId());
        assertEquals(walletId, dtos.get(0).getWalletId());
        assertEquals(amount, dtos.get(0).getAmount());
        assertEquals(type.name(), dtos.get(0).getType());
        assertEquals(timestamp, dtos.get(0).getTimestamp());
        assertEquals(relatedUserId, dtos.get(0).getRelatedUserId());
    }

    @Test
    void toDTOList_shouldReturnEmptyListForNull() {
        List<TransactionDTO> dtos = mapper.toDTOList(null);
        assertNotNull(dtos);
        assertTrue(dtos.isEmpty());
    }
}

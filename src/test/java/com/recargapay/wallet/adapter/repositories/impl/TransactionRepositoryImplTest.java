package com.recargapay.wallet.adapter.repositories.impl;

import com.recargapay.wallet.adapter.converters.TransactionMapper;
import com.recargapay.wallet.adapter.entities.TransactionEntity;
import com.recargapay.wallet.adapter.repositories.TransactionJpaRepository;
import com.recargapay.wallet.core.domain.Transaction;
import com.recargapay.wallet.core.domain.TransactionType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TransactionRepositoryImplTest {
    @Mock
    private TransactionJpaRepository jpaRepository;
    @Mock
    private TransactionMapper transactionMapper;

    @InjectMocks
    private TransactionRepositoryImpl transactionRepository;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void findById_shouldReturnTransaction() {
        UUID id = UUID.randomUUID();
        TransactionEntity entity = new TransactionEntity();
        Transaction tx = new Transaction(id, UUID.randomUUID(), BigDecimal.TEN, TransactionType.DEPOSIT, LocalDateTime.now(), UUID.randomUUID());
        when(jpaRepository.findById(id)).thenReturn(Optional.of(entity));
        when(transactionMapper.toDomain(entity)).thenReturn(tx);
        Optional<Transaction> result = transactionRepository.findById(id);
        assertTrue(result.isPresent());
        assertEquals(tx, result.get());
    }

    @Test
    void findById_shouldReturnEmpty() {
        UUID id = UUID.randomUUID();
        when(jpaRepository.findById(id)).thenReturn(Optional.empty());
        Optional<Transaction> result = transactionRepository.findById(id);
        assertFalse(result.isPresent());
    }

    @Test
    void save_shouldCallSave() {
        Transaction tx = new Transaction(UUID.randomUUID(), UUID.randomUUID(), BigDecimal.ONE, TransactionType.WITHDRAW, LocalDateTime.now(), UUID.randomUUID());
        TransactionEntity entity = new TransactionEntity();
        when(transactionMapper.toEntity(tx)).thenReturn(entity);
        transactionRepository.save(tx);
        verify(jpaRepository, times(1)).save(entity);
    }

    @Test
    void update_shouldCallSave() {
        Transaction tx = new Transaction(UUID.randomUUID(), UUID.randomUUID(), BigDecimal.ONE, TransactionType.DEPOSIT, LocalDateTime.now(), UUID.randomUUID());
        TransactionEntity entity = new TransactionEntity();
        when(transactionMapper.toEntity(tx)).thenReturn(entity);
        transactionRepository.save(tx); 
        verify(jpaRepository, times(1)).save(entity);
    }

    @Test
    void delete_shouldCallDeleteById() {
        UUID id = UUID.randomUUID();
        transactionRepository.delete(id);
        verify(jpaRepository, times(1)).deleteById(id);
    }

    @Test
    void findAll_shouldReturnTransactionList() {
        TransactionEntity entity = new TransactionEntity();
        Transaction tx = new Transaction(UUID.randomUUID(), UUID.randomUUID(), BigDecimal.ONE, TransactionType.WITHDRAW, LocalDateTime.now(), UUID.randomUUID());
        when(jpaRepository.findAll()).thenReturn(List.of(entity));
        when(transactionMapper.toDomain(entity)).thenReturn(tx);
        List<Transaction> result = transactionRepository.findAll();
        assertEquals(1, result.size());
        assertEquals(tx, result.get(0));
    }
}

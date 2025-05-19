package com.recargapay.wallet.adapter.repositories.impl;

import com.recargapay.wallet.adapter.converters.TransactionMapper;
import com.recargapay.wallet.adapter.entities.TransactionEntity;
import com.recargapay.wallet.adapter.entities.WalletEntity;
import com.recargapay.wallet.adapter.repositories.TransactionJpaRepository;
import com.recargapay.wallet.adapter.repositories.WalletJpaRepository;
import com.recargapay.wallet.core.domain.Transaction;
import com.recargapay.wallet.core.domain.TransactionType;
import com.recargapay.wallet.core.exceptions.WalletNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

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
    private WalletJpaRepository walletJpaRepository;
    
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
        // Arrange
        UUID walletId = UUID.randomUUID();
        Transaction tx = new Transaction(UUID.randomUUID(), walletId, BigDecimal.ONE, TransactionType.WITHDRAW, LocalDateTime.now(), UUID.randomUUID());
        WalletEntity walletEntity = new WalletEntity();
        
        // Mock wallet repository
        when(walletJpaRepository.findById(walletId)).thenReturn(Optional.of(walletEntity));
        
        // Act
        transactionRepository.save(tx);
        
        // Assert
        verify(jpaRepository, times(1)).save(any(TransactionEntity.class));
    }

    @Test
    void save_shouldThrowExceptionWhenWalletNotFound() {
        // Arrange
        UUID walletId = UUID.randomUUID();
        Transaction tx = new Transaction(UUID.randomUUID(), walletId, BigDecimal.ONE, TransactionType.WITHDRAW, LocalDateTime.now(), UUID.randomUUID());
        
        // Mock wallet repository to return empty
        when(walletJpaRepository.findById(walletId)).thenReturn(Optional.empty());
        
        // Act & Assert
        assertThrows(WalletNotFoundException.class, () -> transactionRepository.save(tx));
        verify(jpaRepository, never()).save(any(TransactionEntity.class));
    }

    @Test
    void saveAndReturn_shouldReturnTransaction() {
        // Arrange
        UUID walletId = UUID.randomUUID();
        Transaction tx = new Transaction(UUID.randomUUID(), walletId, BigDecimal.ONE, TransactionType.DEPOSIT, LocalDateTime.now(), UUID.randomUUID());
        WalletEntity walletEntity = new WalletEntity();
        TransactionEntity savedEntity = new TransactionEntity();
        
        // Mock wallet repository
        when(walletJpaRepository.findById(walletId)).thenReturn(Optional.of(walletEntity));
        when(jpaRepository.save(any(TransactionEntity.class))).thenReturn(savedEntity);
        when(transactionMapper.toDomain(savedEntity)).thenReturn(tx);
        
        // Act
        Transaction result = transactionRepository.saveAndReturn(tx);
        
        // Assert
        assertEquals(tx, result);
        verify(jpaRepository, times(1)).save(any(TransactionEntity.class));
    }

    @Test
    void delete_shouldCallDelete() {
        UUID id = UUID.randomUUID();
        transactionRepository.delete(id);
        verify(jpaRepository, times(1)).deleteById(id);
    }

    @Test
    void findAll_shouldReturnAllTransactions() {
        TransactionEntity entity1 = new TransactionEntity();
        TransactionEntity entity2 = new TransactionEntity();
        Transaction tx1 = new Transaction();
        Transaction tx2 = new Transaction();
        when(jpaRepository.findAll()).thenReturn(List.of(entity1, entity2));
        when(transactionMapper.toDomain(entity1)).thenReturn(tx1);
        when(transactionMapper.toDomain(entity2)).thenReturn(tx2);
        List<Transaction> result = transactionRepository.findAll();
        assertEquals(2, result.size());
        assertEquals(tx1, result.get(0));
        assertEquals(tx2, result.get(1));
    }
}

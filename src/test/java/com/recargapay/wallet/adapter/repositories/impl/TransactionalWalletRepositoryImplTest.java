package com.recargapay.wallet.adapter.repositories.impl;

import com.recargapay.wallet.adapter.converters.TransactionMapper;
import com.recargapay.wallet.adapter.converters.WalletMapper;
import com.recargapay.wallet.adapter.entities.TransactionEntity;
import com.recargapay.wallet.adapter.entities.UserEntity;
import com.recargapay.wallet.adapter.entities.WalletEntity;
import com.recargapay.wallet.core.domain.Transaction;
import com.recargapay.wallet.core.domain.TransactionType;
import com.recargapay.wallet.core.domain.Wallet;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.Query;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionalWalletRepositoryImplTest {

    @Mock
    private EntityManager entityManager;
    
    @Mock
    private WalletRepositoryImpl delegateWalletRepository;
    
    @Mock
    private WalletMapper walletMapper;
    
    @Mock
    private TransactionMapper transactionMapper;
    
    @Mock
    private Query query;

    @InjectMocks
    private TransactionalWalletRepositoryImpl transactionalWalletRepository;

    private UUID walletId;
    private UUID userId;
    private Wallet wallet;
    private WalletEntity walletEntity;
    private TransactionEntity transactionEntity;
    private Transaction transaction;
    private BigDecimal amount;
    private LocalDateTime timestamp;
    private UserEntity userEntity;

    @BeforeEach
    void setUp() {
        walletId = UUID.randomUUID();
        userId = UUID.randomUUID();
        amount = new BigDecimal("100.00");
        timestamp = LocalDateTime.now();
        
        userEntity = new UserEntity();
        userEntity.setId(userId);
        
        wallet = new Wallet(walletId, userId, new BigDecimal("500.00"));
        
        walletEntity = new WalletEntity();
        walletEntity.setId(walletId);
        walletEntity.setUser(userEntity);
        walletEntity.setBalance(new BigDecimal("500.00"));
        
        transactionEntity = new TransactionEntity();
        transactionEntity.setId(UUID.randomUUID());
        transactionEntity.setWallet(walletEntity);
        transactionEntity.setAmount(amount);
        transactionEntity.setType(TransactionType.DEPOSIT);
        transactionEntity.setRelatedUserId(userId);
        transactionEntity.setTimestamp(timestamp);
        
        transaction = new Transaction(
            transactionEntity.getId(),
            walletId,
            amount,
            TransactionType.DEPOSIT,
            timestamp,
            userId
        );
        
        // Inject the mocked EntityManager into the @PersistenceContext field
        ReflectionTestUtils.setField(transactionalWalletRepository, "entityManager", entityManager);
    }

    @Test
    void findById_ShouldDelegateToWalletRepository() {
        // Given
        when(delegateWalletRepository.findById(walletId)).thenReturn(Optional.of(wallet));

        // When
        Optional<Wallet> result = transactionalWalletRepository.findById(walletId);

        // Then
        assertTrue(result.isPresent());
        assertEquals(wallet, result.get());
        verify(delegateWalletRepository).findById(walletId);
    }

    @Test
    void findByUserId_ShouldDelegateToWalletRepository() {
        // Given
        when(delegateWalletRepository.findByUserId(userId)).thenReturn(Optional.of(wallet));

        // When
        Optional<Wallet> result = transactionalWalletRepository.findByUserId(userId);

        // Then
        assertTrue(result.isPresent());
        assertEquals(wallet, result.get());
        verify(delegateWalletRepository).findByUserId(userId);
    }

    @Test
    void update_ShouldDelegateToWalletRepository() {
        // When
        transactionalWalletRepository.update(wallet);

        // Then
        verify(delegateWalletRepository).update(wallet);
    }

    @Test
    void save_ShouldDelegateToWalletRepository() {
        // Given
        when(delegateWalletRepository.save(wallet)).thenReturn(wallet);

        // When
        Wallet result = transactionalWalletRepository.save(wallet);

        // Then
        assertEquals(wallet, result);
        verify(delegateWalletRepository).save(wallet);
    }

    @Test
    void delete_ShouldDelegateToWalletRepository() {
        // When
        transactionalWalletRepository.delete(walletId);

        // Then
        verify(delegateWalletRepository).delete(walletId);
    }

    @Test
    void findAll_ShouldDelegateToWalletRepository() {
        // Given
        List<Wallet> wallets = List.of(wallet);
        when(delegateWalletRepository.findAll()).thenReturn(wallets);

        // When
        List<Wallet> result = transactionalWalletRepository.findAll();

        // Then
        assertEquals(wallets, result);
        verify(delegateWalletRepository).findAll();
    }

    @Test
    void updateWalletBalance_WithCreditOperation_ShouldReturnTrue() {
        // Given
        when(entityManager.createQuery(anyString())).thenReturn(query);
        when(query.setParameter("amount", amount)).thenReturn(query);
        when(query.setParameter("walletId", walletId)).thenReturn(query);
        when(query.executeUpdate()).thenReturn(1);

        // When
        boolean result = transactionalWalletRepository.updateWalletBalance(walletId, amount, false);

        // Then
        assertTrue(result);
        verify(entityManager).createQuery("UPDATE WalletEntity w SET w.balance = w.balance + :amount WHERE w.id = :walletId");
        verify(query).setParameter("amount", amount);
        verify(query).setParameter("walletId", walletId);
        verify(query).executeUpdate();
    }

    @Test
    void updateWalletBalance_WithDebitOperation_ShouldReturnTrue() {
        // Given
        when(entityManager.createQuery(anyString())).thenReturn(query);
        when(query.setParameter("amount", amount)).thenReturn(query);
        when(query.setParameter("walletId", walletId)).thenReturn(query);
        when(query.executeUpdate()).thenReturn(1);

        // When
        boolean result = transactionalWalletRepository.updateWalletBalance(walletId, amount, true);

        // Then
        assertTrue(result);
        verify(entityManager).createQuery("UPDATE WalletEntity w SET w.balance = w.balance - :amount WHERE w.id = :walletId");
        verify(query).setParameter("amount", amount);
        verify(query).setParameter("walletId", walletId);
        verify(query).executeUpdate();
    }

    @Test
    void updateWalletBalance_WhenNoRowsUpdated_ShouldReturnFalse() {
        // Given
        when(entityManager.createQuery(anyString())).thenReturn(query);
        when(query.setParameter("amount", amount)).thenReturn(query);
        when(query.setParameter("walletId", walletId)).thenReturn(query);
        when(query.executeUpdate()).thenReturn(0);

        // When
        boolean result = transactionalWalletRepository.updateWalletBalance(walletId, amount, false);

        // Then
        assertFalse(result);
        verify(query).executeUpdate();
    }

    @Test
    void createTransaction_WithValidWallet_ShouldCreateAndReturnTransaction() {
        // Given
        when(entityManager.find(WalletEntity.class, walletId)).thenReturn(walletEntity);
        when(transactionMapper.toDomain(any(TransactionEntity.class))).thenReturn(transaction);

        // When
        Transaction result = transactionalWalletRepository.createTransaction(
            walletId, amount, TransactionType.DEPOSIT, userId, timestamp
        );

        // Then
        assertNotNull(result);
        assertEquals(transaction, result);
        
        verify(entityManager).find(WalletEntity.class, walletId);
        
        ArgumentCaptor<TransactionEntity> entityCaptor = ArgumentCaptor.forClass(TransactionEntity.class);
        verify(entityManager).persist(entityCaptor.capture());
        
        TransactionEntity capturedEntity = entityCaptor.getValue();
        assertEquals(walletEntity, capturedEntity.getWallet());
        assertEquals(amount, capturedEntity.getAmount());
        assertEquals(TransactionType.DEPOSIT, capturedEntity.getType());
        assertEquals(userId, capturedEntity.getRelatedUserId());
        assertEquals(timestamp, capturedEntity.getTimestamp());
        
        verify(transactionMapper).toDomain(capturedEntity);
    }

    @Test
    void createTransaction_WithNonExistentWallet_ShouldThrowEntityNotFoundException() {
        // Given
        when(entityManager.find(WalletEntity.class, walletId)).thenReturn(null);

        // When & Then
        EntityNotFoundException exception = assertThrows(
            EntityNotFoundException.class,
            () -> transactionalWalletRepository.createTransaction(
                walletId, amount, TransactionType.DEPOSIT, userId, timestamp
            )
        );

        assertEquals("Wallet not found: " + walletId, exception.getMessage());
        verify(entityManager).find(WalletEntity.class, walletId);
        verify(entityManager, never()).persist(any());
        verify(transactionMapper, never()).toDomain(any());
    }

    @Test
    void createTransaction_WithWithdrawType_ShouldCreateCorrectTransaction() {
        // Given
        when(entityManager.find(WalletEntity.class, walletId)).thenReturn(walletEntity);
        when(transactionMapper.toDomain(any(TransactionEntity.class))).thenReturn(transaction);

        // When
        transactionalWalletRepository.createTransaction(
            walletId, amount, TransactionType.WITHDRAW, userId, timestamp
        );

        // Then
        ArgumentCaptor<TransactionEntity> entityCaptor = ArgumentCaptor.forClass(TransactionEntity.class);
        verify(entityManager).persist(entityCaptor.capture());
        
        TransactionEntity capturedEntity = entityCaptor.getValue();
        assertEquals(TransactionType.WITHDRAW, capturedEntity.getType());
    }

    @Test
    void createTransaction_WithTransferInType_ShouldCreateCorrectTransaction() {
        // Given
        UUID relatedUserId = UUID.randomUUID();
        when(entityManager.find(WalletEntity.class, walletId)).thenReturn(walletEntity);
        when(transactionMapper.toDomain(any(TransactionEntity.class))).thenReturn(transaction);

        // When
        transactionalWalletRepository.createTransaction(
            walletId, amount, TransactionType.TRANSFER_IN, relatedUserId, timestamp
        );

        // Then
        ArgumentCaptor<TransactionEntity> entityCaptor = ArgumentCaptor.forClass(TransactionEntity.class);
        verify(entityManager).persist(entityCaptor.capture());
        
        TransactionEntity capturedEntity = entityCaptor.getValue();
        assertEquals(TransactionType.TRANSFER_IN, capturedEntity.getType());
        assertEquals(relatedUserId, capturedEntity.getRelatedUserId());
    }

    @Test
    void createTransaction_WithTransferOutType_ShouldCreateCorrectTransaction() {
        // Given
        UUID relatedUserId = UUID.randomUUID();
        when(entityManager.find(WalletEntity.class, walletId)).thenReturn(walletEntity);
        when(transactionMapper.toDomain(any(TransactionEntity.class))).thenReturn(transaction);

        // When
        transactionalWalletRepository.createTransaction(
            walletId, amount, TransactionType.TRANSFER_OUT, relatedUserId, timestamp
        );

        // Then
        ArgumentCaptor<TransactionEntity> entityCaptor = ArgumentCaptor.forClass(TransactionEntity.class);
        verify(entityManager).persist(entityCaptor.capture());
        
        TransactionEntity capturedEntity = entityCaptor.getValue();
        assertEquals(TransactionType.TRANSFER_OUT, capturedEntity.getType());
        assertEquals(relatedUserId, capturedEntity.getRelatedUserId());
    }

    @Test
    void createTransaction_WhenExceptionOccurs_ShouldRethrowException() {
        // Given
        when(entityManager.find(WalletEntity.class, walletId)).thenReturn(walletEntity);
        doThrow(new RuntimeException("Database error")).when(entityManager).persist(any());

        // When & Then
        RuntimeException exception = assertThrows(
            RuntimeException.class,
            () -> transactionalWalletRepository.createTransaction(
                walletId, amount, TransactionType.DEPOSIT, userId, timestamp
            )
        );

        assertEquals("Database error", exception.getMessage());
        verify(entityManager).find(WalletEntity.class, walletId);
        verify(entityManager).persist(any());
        verify(transactionMapper, never()).toDomain(any());
    }
}

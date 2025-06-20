package com.recargapay.wallet.core.services;

import com.recargapay.wallet.core.domain.Transaction;
import com.recargapay.wallet.core.domain.TransactionType;
import com.recargapay.wallet.core.domain.Wallet;
import com.recargapay.wallet.core.exceptions.WalletNotFoundException;
import com.recargapay.wallet.core.ports.out.TransactionalWalletRepository;
import com.recargapay.wallet.infra.metrics.MetricsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class DepositServiceTest {

    @Mock
    private TransactionalWalletRepository walletRepository;
    
    @Mock
    private MetricsService metricsService;
    
    @Captor
    private ArgumentCaptor<Transaction> transactionCaptor;

    private DepositService depositService;

    private UUID walletId;
    private UUID userId;
    private BigDecimal initialBalance;
    private BigDecimal depositAmount;

    private Wallet wallet;

    @BeforeEach
    void setUp() {
        walletId = UUID.randomUUID();
        userId = UUID.randomUUID();
        initialBalance = new BigDecimal("100.00");
        depositAmount = new BigDecimal("50.00");

        wallet = new Wallet();
        wallet.setId(walletId);
        wallet.setUserId(userId);
        wallet.setBalance(initialBalance);
        
        // Creating the service with the new constructor signature
        depositService = new DepositService(walletRepository, metricsService);
        
        // Configure wallet repository to return our test wallet
        when(walletRepository.findById(walletId)).thenReturn(Optional.of(wallet));
        when(walletRepository.updateWalletBalance(walletId, depositAmount, false)).thenReturn(true);
        
        Transaction mockTransaction = new Transaction();
        mockTransaction.setId(UUID.randomUUID());
        mockTransaction.setAmount(depositAmount);
        mockTransaction.setType(TransactionType.DEPOSIT);
        mockTransaction.setWalletId(walletId);
        
        when(walletRepository.createTransaction(eq(walletId), eq(depositAmount), eq(TransactionType.DEPOSIT), any(UUID.class), any()))
            .thenReturn(mockTransaction);
    }

    @Test
    void deposit_shouldUpdateWalletBalance_andCreateTransaction() {
        // Act
        Transaction result = depositService.deposit(walletId, depositAmount);

        // Assert
        assertNotNull(result);
        assertEquals(depositAmount, result.getAmount());
        assertEquals(TransactionType.DEPOSIT, result.getType());
        
        // Verify repository calls
        verify(walletRepository).findById(walletId);
        verify(walletRepository).updateWalletBalance(walletId, depositAmount, false);
        verify(walletRepository).createTransaction(eq(walletId), eq(depositAmount), eq(TransactionType.DEPOSIT), any(UUID.class), any());
        verify(metricsService).recordWalletBalance(walletId.toString(), wallet.getBalance().add(depositAmount));
    }

    @Test
    void deposit_shouldThrowWalletNotFoundException_whenWalletNotFound() {
        // Arrange
        when(walletRepository.findById(walletId)).thenReturn(Optional.empty());
        
        // Act & Assert
        assertThrows(WalletNotFoundException.class, () -> {
            depositService.deposit(walletId, depositAmount);
        });
        
        // Verify repository call
        verify(walletRepository).findById(walletId);
        verify(walletRepository, never()).updateWalletBalance(any(), any(), anyBoolean());
        verify(walletRepository, never()).createTransaction(any(), any(), any(), any(), any());
        verify(metricsService, never()).recordWalletBalance(any(), any());
    }

    @Test
    void deposit_shouldThrowWalletNotFoundException_whenUpdateFails() {
        // Arrange
        when(walletRepository.findById(walletId)).thenReturn(Optional.of(wallet));
        when(walletRepository.updateWalletBalance(walletId, depositAmount, false)).thenReturn(false);
        
        // Act & Assert
        assertThrows(WalletNotFoundException.class, () -> {
            depositService.deposit(walletId, depositAmount);
        });
        
        // Verify repository calls
        verify(walletRepository).findById(walletId);
        verify(walletRepository).updateWalletBalance(walletId, depositAmount, false);
        verify(walletRepository, never()).createTransaction(any(), any(), any(), any(), any());
        verify(metricsService, never()).recordWalletBalance(any(), any());
    }

    @Test
    void deposit_shouldThrowException_whenWalletIdIsNull() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            depositService.deposit(null, depositAmount);
        });
        
        // Verify no repository calls
        verify(walletRepository, never()).findById(any());
        verify(walletRepository, never()).updateWalletBalance(any(), any(), anyBoolean());
        verify(walletRepository, never()).createTransaction(any(), any(), any(), any(), any());
    }
    
    @Test
    void deposit_shouldThrowException_whenDepositAmountIsNull() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            depositService.deposit(walletId, null);
        });
        
        // Verify no repository calls
        verify(walletRepository, never()).findById(any());
        verify(walletRepository, never()).updateWalletBalance(any(), any(), anyBoolean());
        verify(walletRepository, never()).createTransaction(any(), any(), any(), any(), any());
    }

    @Test
    void deposit_shouldThrowException_whenWalletIdIsZero() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            depositService.deposit(UUID.fromString("00000000-0000-0000-0000-000000000000"), depositAmount);
        });
        
        // Verify no repository calls
        verify(walletRepository, never()).findById(any());
        verify(walletRepository, never()).updateWalletBalance(any(), any(), anyBoolean());
        verify(walletRepository, never()).createTransaction(any(), any(), any(), any(), any());
    }

    @Test
    void deposit_shouldThrowException_whenDepositAmountIsZero() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            depositService.deposit(walletId, BigDecimal.ZERO);
        });
        
        // Verify no repository calls
        verify(walletRepository, never()).findById(any());
        verify(walletRepository, never()).updateWalletBalance(any(), any(), anyBoolean());
        verify(walletRepository, never()).createTransaction(any(), any(), any(), any(), any());
    }

    @Test
    void deposit_shouldThrowException_whenDepositAmountIsNegative() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            depositService.deposit(walletId, BigDecimal.valueOf(-10));
        });
        
        // Verify no repository calls
        verify(walletRepository, never()).findById(any());
        verify(walletRepository, never()).updateWalletBalance(any(), any(), anyBoolean());
        verify(walletRepository, never()).createTransaction(any(), any(), any(), any(), any());
    }
}

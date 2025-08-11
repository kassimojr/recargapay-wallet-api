package com.recargapay.wallet.core.services;

import com.recargapay.wallet.core.domain.Transaction;
import com.recargapay.wallet.core.domain.TransactionType;
import com.recargapay.wallet.core.domain.Wallet;
import com.recargapay.wallet.core.exceptions.WalletNotFoundException;
import com.recargapay.wallet.core.ports.out.DomainLogger;
import com.recargapay.wallet.core.ports.out.TransactionalWalletRepository;
import com.recargapay.wallet.infra.metrics.MetricsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class DepositServiceTest {

    @Mock
    private TransactionalWalletRepository walletRepository;

    @Mock
    private MetricsService metricsService;
    
    @Mock
    private DomainLogger logger;

    private DepositService depositService;
    private UUID walletId;
    private BigDecimal depositAmount;

    @BeforeEach
    void setUp() {
        walletId = UUID.randomUUID();
        depositAmount = new BigDecimal("100.00");
        depositService = new DepositService(walletRepository, metricsService, logger);
    }

    @Test
    void deposit_shouldAddFundsToWalletAndCreateTransaction() {
        // Arrange
        Wallet wallet = new Wallet();
        wallet.setId(walletId);
        wallet.setBalance(new BigDecimal("50.00"));
        
        UUID transactionId = UUID.randomUUID();
        Transaction expectedTransaction = new Transaction();
        expectedTransaction.setId(transactionId);
        expectedTransaction.setWalletId(walletId);
        expectedTransaction.setAmount(depositAmount);
        expectedTransaction.setType(TransactionType.DEPOSIT);
        
        when(walletRepository.findById(walletId)).thenReturn(Optional.of(wallet));
        when(walletRepository.updateWalletBalance(walletId, depositAmount, false)).thenReturn(true);
        when(walletRepository.createTransaction(eq(walletId), eq(depositAmount), eq(TransactionType.DEPOSIT), any(), any(LocalDateTime.class)))
                .thenReturn(expectedTransaction);
        
        // Act
        Transaction result = depositService.deposit(walletId, depositAmount);
        
        // Assert
        assertNotNull(result);
        assertEquals(expectedTransaction.getId(), result.getId());
        assertEquals(depositAmount, result.getAmount());
        assertEquals(TransactionType.DEPOSIT, result.getType());
        
        // Verify repository calls
        verify(walletRepository).findById(walletId);
        verify(walletRepository).updateWalletBalance(walletId, depositAmount, false);
        verify(walletRepository).createTransaction(eq(walletId), eq(depositAmount), eq(TransactionType.DEPOSIT), any(), any(LocalDateTime.class));
        
        // Verify metrics recording
        verify(metricsService).recordWalletBalance(eq(walletId.toString()), eq(new BigDecimal("150.00")));
        
        // Verify logging
        verify(logger).logOperationStart("DEPOSIT", walletId.toString(), depositAmount.toString());
        verify(logger).logOperationSuccess("DEPOSIT", walletId.toString(), depositAmount.toString(), result.getId().toString());
    }
    
    @Test
    void deposit_shouldThrowWalletNotFoundException_whenWalletNotFound() {
        // Arrange
        when(walletRepository.findById(walletId)).thenReturn(Optional.empty());
        
        // Act & Assert
        assertThrows(WalletNotFoundException.class, () -> depositService.deposit(walletId, depositAmount));
        
        // Verify repository calls
        verify(walletRepository).findById(walletId);
        verify(walletRepository, never()).updateWalletBalance(any(), any(), anyBoolean());
        verify(walletRepository, never()).createTransaction(any(), any(), any(), any(), any());
        
        // Verify logging
        verify(logger).logOperationStart("DEPOSIT", walletId.toString(), depositAmount.toString());
        verify(logger).logOperationError(eq("DEPOSIT"), eq(walletId.toString()), eq("WALLET_NOT_FOUND"), anyString());
    }
    
    @Test
    void deposit_shouldThrowWalletNotFoundException_whenWalletUpdateFails() {
        // Arrange
        Wallet wallet = new Wallet();
        wallet.setId(walletId);
        wallet.setBalance(new BigDecimal("50.00"));
        
        when(walletRepository.findById(walletId)).thenReturn(Optional.of(wallet));
        when(walletRepository.updateWalletBalance(walletId, depositAmount, false)).thenReturn(false);
        
        // Act & Assert
        assertThrows(WalletNotFoundException.class, () -> depositService.deposit(walletId, depositAmount));
        
        // Verify repository calls
        verify(walletRepository).findById(walletId);
        verify(walletRepository).updateWalletBalance(walletId, depositAmount, false);
        verify(walletRepository, never()).createTransaction(any(), any(), any(), any(), any());
        
        // Verify logging
        verify(logger).logOperationStart("DEPOSIT", walletId.toString(), depositAmount.toString());
        verify(logger).logOperationError(eq("DEPOSIT"), eq(walletId.toString()), eq("UPDATE_FAILED"), anyString());
    }
}

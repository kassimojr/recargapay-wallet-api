package com.recargapay.wallet.core.services;

import com.recargapay.wallet.core.domain.Transaction;
import com.recargapay.wallet.core.domain.TransactionType;
import com.recargapay.wallet.core.domain.Wallet;
import com.recargapay.wallet.core.exceptions.InsufficientFundsException;
import com.recargapay.wallet.core.exceptions.WalletNotFoundException;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class WithdrawServiceTest {

    @Mock
    private TransactionalWalletRepository walletRepository;

    @Mock
    private MetricsService metricsService;

    private WithdrawService withdrawService;
    private UUID walletId;
    private Wallet wallet;
    private BigDecimal withdrawAmount;

    @BeforeEach
    void setUp() {
        withdrawService = new WithdrawService(walletRepository, metricsService);
        walletId = UUID.randomUUID();
        wallet = new Wallet();
        wallet.setId(walletId);
        wallet.setBalance(BigDecimal.valueOf(100.0));
        withdrawAmount = BigDecimal.valueOf(50.0);
    }

    @Test
    void withdraw_shouldDeductBalanceAndCreateTransaction() {
        // Arrange
        when(walletRepository.findById(walletId)).thenReturn(Optional.of(wallet));
        
        Transaction mockTransaction = new Transaction();
        mockTransaction.setId(UUID.randomUUID());
        mockTransaction.setWalletId(walletId);
        mockTransaction.setAmount(withdrawAmount);
        mockTransaction.setType(TransactionType.WITHDRAW);
        mockTransaction.setTimestamp(LocalDateTime.now());
        
        when(walletRepository.updateWalletBalance(eq(walletId), eq(withdrawAmount), eq(true))).thenReturn(true);
        doReturn(mockTransaction).when(walletRepository).createTransaction(any(), any(), any(), any(), any());

        // Act
        Transaction result = withdrawService.withdraw(walletId, withdrawAmount);

        // Assert
        assertNotNull(result);
        assertEquals(withdrawAmount, result.getAmount());
        assertEquals(TransactionType.WITHDRAW, result.getType());
        
        // Verify repository calls
        verify(walletRepository).findById(walletId);
        verify(walletRepository).updateWalletBalance(eq(walletId), eq(withdrawAmount), eq(true));
        verify(walletRepository).createTransaction(any(), any(), any(), any(), any());
        verify(metricsService).recordWalletBalance(eq(walletId.toString()), eq(BigDecimal.valueOf(50.0)));
    }

    @Test
    void withdraw_shouldThrowWalletNotFoundException_whenWalletNotFound() {
        // Arrange
        when(walletRepository.findById(walletId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(WalletNotFoundException.class, () -> 
            withdrawService.withdraw(walletId, withdrawAmount)
        );
        
        // Verify repository calls
        verify(walletRepository).findById(walletId);
        verify(walletRepository, never()).updateWalletBalance(any(), any(), anyBoolean());
        verify(walletRepository, never()).createTransaction(any(), any(), any(), any(), any());
        verify(metricsService, never()).recordWalletBalance(any(), any());
    }

    @Test
    void withdraw_shouldThrowInsufficientFundsException_whenBalanceInsufficient() {
        // Arrange
        Wallet lowBalanceWallet = new Wallet();
        lowBalanceWallet.setId(walletId);
        lowBalanceWallet.setBalance(BigDecimal.valueOf(10.0));
        
        when(walletRepository.findById(walletId)).thenReturn(Optional.of(lowBalanceWallet));

        // Act & Assert
        assertThrows(InsufficientFundsException.class, () ->
            withdrawService.withdraw(walletId, withdrawAmount)
        );
        
        // Verify repository calls
        verify(walletRepository).findById(walletId);
        verify(walletRepository, never()).updateWalletBalance(any(), any(), anyBoolean());
        verify(walletRepository, never()).createTransaction(any(), any(), any(), any(), any());
        verify(metricsService, never()).recordWalletBalance(any(), any());
    }

    @Test
    void withdraw_shouldThrowWalletNotFoundException_whenUpdateFails() {
        // Arrange
        when(walletRepository.findById(walletId)).thenReturn(Optional.of(wallet));
        when(walletRepository.updateWalletBalance(eq(walletId), eq(withdrawAmount), eq(true))).thenReturn(false);

        // Act & Assert
        assertThrows(WalletNotFoundException.class, () -> 
            withdrawService.withdraw(walletId, withdrawAmount)
        );
        
        // Verify repository calls
        verify(walletRepository).findById(walletId);
        verify(walletRepository).updateWalletBalance(eq(walletId), eq(withdrawAmount), eq(true));
        verify(walletRepository, never()).createTransaction(any(), any(), any(), any(), any());
        verify(metricsService, never()).recordWalletBalance(any(), any());
    }

    @Test
    void withdraw_shouldThrowException_whenWalletIdIsNull() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> 
            withdrawService.withdraw(null, withdrawAmount)
        );
        
        // Verify no repository calls
        verify(walletRepository, never()).findById(any());
        verify(walletRepository, never()).updateWalletBalance(any(), any(), anyBoolean());
        verify(walletRepository, never()).createTransaction(any(), any(), any(), any(), any());
    }

    @Test
    void withdraw_shouldThrowException_whenWithdrawAmountIsNull() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> 
            withdrawService.withdraw(walletId, null)
        );
        
        // Verify no repository calls
        verify(walletRepository, never()).findById(any());
        verify(walletRepository, never()).updateWalletBalance(any(), any(), anyBoolean());
        verify(walletRepository, never()).createTransaction(any(), any(), any(), any(), any());
    }

    @Test
    void withdraw_shouldThrowException_whenWithdrawAmountIsZero() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> 
            withdrawService.withdraw(walletId, BigDecimal.ZERO)
        );
        
        // Verify no repository calls
        verify(walletRepository, never()).findById(any());
        verify(walletRepository, never()).updateWalletBalance(any(), any(), anyBoolean());
        verify(walletRepository, never()).createTransaction(any(), any(), any(), any(), any());
    }

    @Test
    void withdraw_shouldThrowException_whenWithdrawAmountIsNegative() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> 
            withdrawService.withdraw(walletId, BigDecimal.valueOf(-10.0))
        );
        
        // Verify no repository calls
        verify(walletRepository, never()).findById(any());
        verify(walletRepository, never()).updateWalletBalance(any(), any(), anyBoolean());
        verify(walletRepository, never()).createTransaction(any(), any(), any(), any(), any());
    }
}

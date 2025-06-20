package com.recargapay.wallet.core.services;

import com.recargapay.wallet.core.domain.Transaction;
import com.recargapay.wallet.core.domain.TransactionType;
import com.recargapay.wallet.core.domain.Wallet;
import com.recargapay.wallet.core.exceptions.InsufficientFundsException;
import com.recargapay.wallet.core.exceptions.SameWalletTransferException;
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
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class TransferFundsServiceTest {

    @Mock
    private TransactionalWalletRepository walletRepository;

    @Mock
    private MetricsService metricsService;

    private TransferFundsService transferFundsService;
    private UUID fromWalletId;
    private UUID toWalletId;
    private BigDecimal transferAmount;
    private Wallet fromWallet;
    private Wallet toWallet;

    @BeforeEach
    void setUp() {
        transferFundsService = new TransferFundsService(walletRepository, metricsService);
        
        // Initialize test data
        fromWalletId = UUID.randomUUID();
        toWalletId = UUID.randomUUID();
        transferAmount = new BigDecimal("50.00");
        
        fromWallet = new Wallet();
        fromWallet.setId(fromWalletId);
        fromWallet.setUserId(UUID.randomUUID());
        fromWallet.setBalance(new BigDecimal("100.00"));
        
        toWallet = new Wallet();
        toWallet.setId(toWalletId);
        toWallet.setUserId(UUID.randomUUID());
        toWallet.setBalance(new BigDecimal("20.00"));
    }

    @Test
    void transferFunds_shouldTransferFundsAndCreateTransactions() {
        // Arrange
        when(walletRepository.findById(fromWalletId)).thenReturn(Optional.of(fromWallet));
        when(walletRepository.findById(toWalletId)).thenReturn(Optional.of(toWallet));
        when(walletRepository.updateWalletBalance(fromWalletId, transferAmount, true)).thenReturn(true);
        when(walletRepository.updateWalletBalance(toWalletId, transferAmount, false)).thenReturn(true);
        
        Transaction outTransaction = new Transaction();
        outTransaction.setId(UUID.randomUUID());
        outTransaction.setWalletId(fromWalletId);
        outTransaction.setAmount(transferAmount);
        outTransaction.setType(TransactionType.TRANSFER_OUT);
        
        Transaction inTransaction = new Transaction();
        inTransaction.setId(UUID.randomUUID());
        inTransaction.setWalletId(toWalletId);
        inTransaction.setAmount(transferAmount);
        inTransaction.setType(TransactionType.TRANSFER_IN);
        
        when(walletRepository.createTransaction(eq(fromWalletId), eq(transferAmount), eq(TransactionType.TRANSFER_OUT), eq(toWallet.getUserId()), any(LocalDateTime.class)))
            .thenReturn(outTransaction);
        
        when(walletRepository.createTransaction(eq(toWalletId), eq(transferAmount), eq(TransactionType.TRANSFER_IN), eq(fromWallet.getUserId()), any(LocalDateTime.class)))
            .thenReturn(inTransaction);

        // Act
        List<Transaction> result = transferFundsService.transfer(fromWalletId, toWalletId, transferAmount);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(TransactionType.TRANSFER_OUT, result.get(0).getType());
        assertEquals(TransactionType.TRANSFER_IN, result.get(1).getType());
        
        // Verify repository calls
        verify(walletRepository).findById(fromWalletId);
        verify(walletRepository).findById(toWalletId);
        verify(walletRepository).updateWalletBalance(fromWalletId, transferAmount, true);
        verify(walletRepository).updateWalletBalance(toWalletId, transferAmount, false);
        verify(walletRepository).createTransaction(eq(fromWalletId), eq(transferAmount), eq(TransactionType.TRANSFER_OUT), eq(toWallet.getUserId()), any(LocalDateTime.class));
        verify(walletRepository).createTransaction(eq(toWalletId), eq(transferAmount), eq(TransactionType.TRANSFER_IN), eq(fromWallet.getUserId()), any(LocalDateTime.class));
        
        // Verify metrics recording
        verify(metricsService).recordWalletBalance(eq(fromWalletId.toString()), eq(new BigDecimal("50.00")));
        verify(metricsService).recordWalletBalance(eq(toWalletId.toString()), eq(new BigDecimal("70.00")));
    }

    @Test
    void transferFunds_shouldThrowWalletNotFoundException_whenSourceWalletNotFound() {
        // Arrange
        when(walletRepository.findById(fromWalletId)).thenReturn(Optional.empty());
        
        // Act & Assert
        assertThrows(WalletNotFoundException.class, () -> 
            transferFundsService.transfer(fromWalletId, toWalletId, transferAmount)
        );
        
        // Verify repository calls
        verify(walletRepository).findById(fromWalletId);
        verify(walletRepository, never()).findById(toWalletId);
        verify(walletRepository, never()).updateWalletBalance(any(), any(), anyBoolean());
        verify(walletRepository, never()).createTransaction(any(), any(), any(), any(), any());
    }
    
    @Test
    void transferFunds_shouldThrowWalletNotFoundException_whenDestinationWalletNotFound() {
        // Arrange
        when(walletRepository.findById(fromWalletId)).thenReturn(Optional.of(fromWallet));
        when(walletRepository.findById(toWalletId)).thenReturn(Optional.empty());
        
        // Act & Assert
        assertThrows(WalletNotFoundException.class, () -> 
            transferFundsService.transfer(fromWalletId, toWalletId, transferAmount)
        );
        
        // Verify repository calls
        verify(walletRepository).findById(fromWalletId);
        verify(walletRepository).findById(toWalletId);
        verify(walletRepository, never()).updateWalletBalance(any(), any(), anyBoolean());
        verify(walletRepository, never()).createTransaction(any(), any(), any(), any(), any());
    }
    
    @Test
    void transferFunds_shouldThrowInsufficientFundsException_whenBalanceInsufficient() {
        // Arrange
        Wallet lowBalanceWallet = new Wallet();
        lowBalanceWallet.setId(fromWalletId);
        lowBalanceWallet.setBalance(new BigDecimal("10.00"));
        
        when(walletRepository.findById(fromWalletId)).thenReturn(Optional.of(lowBalanceWallet));
        when(walletRepository.findById(toWalletId)).thenReturn(Optional.of(toWallet));
        
        // Act & Assert
        assertThrows(InsufficientFundsException.class, () -> 
            transferFundsService.transfer(fromWalletId, toWalletId, transferAmount)
        );
        
        // Verify repository calls
        verify(walletRepository).findById(fromWalletId);
        verify(walletRepository).findById(toWalletId);
        verify(walletRepository, never()).updateWalletBalance(any(), any(), anyBoolean());
        verify(walletRepository, never()).createTransaction(any(), any(), any(), any(), any());
    }
    
    @Test
    void transferFunds_shouldThrowWalletNotFoundException_whenSourceWalletUpdateFails() {
        // Arrange
        when(walletRepository.findById(fromWalletId)).thenReturn(Optional.of(fromWallet));
        when(walletRepository.findById(toWalletId)).thenReturn(Optional.of(toWallet));
        when(walletRepository.updateWalletBalance(fromWalletId, transferAmount, true)).thenReturn(false);
        
        // Act & Assert
        assertThrows(WalletNotFoundException.class, () -> 
            transferFundsService.transfer(fromWalletId, toWalletId, transferAmount)
        );
        
        // Verify repository calls
        verify(walletRepository).findById(fromWalletId);
        verify(walletRepository).findById(toWalletId);
        verify(walletRepository).updateWalletBalance(fromWalletId, transferAmount, true);
        verify(walletRepository, never()).updateWalletBalance(toWalletId, transferAmount, false);
        verify(walletRepository, never()).createTransaction(any(), any(), any(), any(), any());
    }
    
    @Test
    void transferFunds_shouldThrowWalletNotFoundException_whenDestinationWalletUpdateFails() {
        // Arrange
        when(walletRepository.findById(fromWalletId)).thenReturn(Optional.of(fromWallet));
        when(walletRepository.findById(toWalletId)).thenReturn(Optional.of(toWallet));
        when(walletRepository.updateWalletBalance(fromWalletId, transferAmount, true)).thenReturn(true);
        when(walletRepository.updateWalletBalance(toWalletId, transferAmount, false)).thenReturn(false);
        
        // Act & Assert
        assertThrows(WalletNotFoundException.class, () -> 
            transferFundsService.transfer(fromWalletId, toWalletId, transferAmount)
        );
        
        // Verify repository calls
        verify(walletRepository).findById(fromWalletId);
        verify(walletRepository).findById(toWalletId);
        verify(walletRepository).updateWalletBalance(fromWalletId, transferAmount, true);
        verify(walletRepository).updateWalletBalance(toWalletId, transferAmount, false);
        // Verify the rollback operation occurred
        verify(walletRepository).updateWalletBalance(fromWalletId, transferAmount, false);
        verify(walletRepository, never()).createTransaction(any(), any(), any(), any(), any());
    }
    
    @Test
    void transferFunds_shouldThrowSameWalletTransferException_whenWalletIdsAreEqual() {
        // Act & Assert
        assertThrows(SameWalletTransferException.class, () -> 
            transferFundsService.transfer(fromWalletId, fromWalletId, transferAmount)
        );
        
        // Verify no repository calls
        verify(walletRepository, never()).findById(any());
        verify(walletRepository, never()).updateWalletBalance(any(), any(), anyBoolean());
        verify(walletRepository, never()).createTransaction(any(), any(), any(), any(), any());
    }
    
    @Test
    void transferFunds_shouldThrowException_whenSourceWalletIdIsNull() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> 
            transferFundsService.transfer(null, toWalletId, transferAmount)
        );
        
        // Verify no repository calls
        verify(walletRepository, never()).findById(any());
        verify(walletRepository, never()).updateWalletBalance(any(), any(), anyBoolean());
        verify(walletRepository, never()).createTransaction(any(), any(), any(), any(), any());
    }
    
    @Test
    void transferFunds_shouldThrowException_whenDestinationWalletIdIsNull() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> 
            transferFundsService.transfer(fromWalletId, null, transferAmount)
        );
        
        // Verify no repository calls
        verify(walletRepository, never()).findById(any());
        verify(walletRepository, never()).updateWalletBalance(any(), any(), anyBoolean());
        verify(walletRepository, never()).createTransaction(any(), any(), any(), any(), any());
    }
    
    @Test
    void transferFunds_shouldThrowException_whenAmountIsNull() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> 
            transferFundsService.transfer(fromWalletId, toWalletId, null)
        );
        
        // Verify no repository calls
        verify(walletRepository, never()).findById(any());
        verify(walletRepository, never()).updateWalletBalance(any(), any(), anyBoolean());
        verify(walletRepository, never()).createTransaction(any(), any(), any(), any(), any());
    }
    
    @Test
    void transferFunds_shouldThrowException_whenAmountIsZero() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> 
            transferFundsService.transfer(fromWalletId, toWalletId, BigDecimal.ZERO)
        );
        
        // Verify no repository calls
        verify(walletRepository, never()).findById(any());
        verify(walletRepository, never()).updateWalletBalance(any(), any(), anyBoolean());
        verify(walletRepository, never()).createTransaction(any(), any(), any(), any(), any());
    }
    
    @Test
    void transferFunds_shouldThrowException_whenAmountIsNegative() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> 
            transferFundsService.transfer(fromWalletId, toWalletId, BigDecimal.valueOf(-10))
        );
        
        // Verify no repository calls
        verify(walletRepository, never()).findById(any());
        verify(walletRepository, never()).updateWalletBalance(any(), any(), anyBoolean());
        verify(walletRepository, never()).createTransaction(any(), any(), any(), any(), any());
    }
}

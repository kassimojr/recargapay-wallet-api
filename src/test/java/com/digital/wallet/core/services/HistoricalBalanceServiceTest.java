package com.digital.wallet.core.services;

import com.digital.wallet.core.domain.Transaction;
import com.digital.wallet.core.domain.TransactionType;
import com.digital.wallet.core.domain.Wallet;
import com.digital.wallet.core.exceptions.InvalidDateFormatException;
import com.digital.wallet.core.exceptions.WalletNotFoundException;
import com.digital.wallet.core.ports.out.TransactionRepository;
import com.digital.wallet.core.ports.out.UserRepository;
import com.digital.wallet.core.ports.out.WalletRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HistoricalBalanceServiceTest {
    
    @Mock
    private WalletRepository walletRepository;
    
    @Mock
    private UserRepository userRepository;
    
    @Mock
    private TransactionRepository transactionRepository;
    
    private CreateWalletService service;

    @BeforeEach
    void setUp() {
        service = new CreateWalletService(walletRepository, userRepository, transactionRepository);
    }

    @Test
    void shouldCalculateCorrectHistoricalBalance() {
        // Arrange
        UUID walletId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        Wallet wallet = new Wallet(walletId, userId, BigDecimal.ZERO);
        wallet.setCreatedAt(LocalDateTime.of(2023, 1, 1, 0, 0, 0));
        
        String targetDate = "2023-01-10T12:00:00";
        LocalDateTime targetDateTime = LocalDateTime.of(2023, 1, 10, 12, 0, 0);
        
        // Create test transactions
        List<Transaction> transactions = new ArrayList<>();
        
        // Deposit of 100
        Transaction deposit = new Transaction();
        deposit.setId(UUID.randomUUID());
        deposit.setWalletId(walletId);
        deposit.setAmount(new BigDecimal("100.00"));
        deposit.setType(TransactionType.DEPOSIT);
        deposit.setTimestamp(LocalDateTime.of(2023, 1, 5, 10, 0, 0));
        transactions.add(deposit);
        
        // Withdrawal of 30
        Transaction withdrawal = new Transaction();
        withdrawal.setId(UUID.randomUUID());
        withdrawal.setWalletId(walletId);
        withdrawal.setAmount(new BigDecimal("30.00"));
        withdrawal.setType(TransactionType.WITHDRAW);
        withdrawal.setTimestamp(LocalDateTime.of(2023, 1, 7, 14, 0, 0));
        transactions.add(withdrawal);
        
        // Incoming transfer of 50
        Transaction transferIn = new Transaction();
        transferIn.setId(UUID.randomUUID());
        transferIn.setWalletId(walletId);
        transferIn.setAmount(new BigDecimal("50.00"));
        transferIn.setType(TransactionType.TRANSFER_IN);
        transferIn.setTimestamp(LocalDateTime.of(2023, 1, 8, 9, 0, 0));
        transactions.add(transferIn);
        
        // Outgoing transfer of 20
        Transaction transferOut = new Transaction();
        transferOut.setId(UUID.randomUUID());
        transferOut.setWalletId(walletId);
        transferOut.setAmount(new BigDecimal("20.00"));
        transferOut.setType(TransactionType.TRANSFER_OUT);
        transferOut.setTimestamp(LocalDateTime.of(2023, 1, 9, 16, 0, 0));
        transactions.add(transferOut);
        
        when(walletRepository.findById(walletId)).thenReturn(Optional.of(wallet));
        when(transactionRepository.findByWalletIdAndTimestampLessThanEqual(eq(walletId), any(LocalDateTime.class)))
            .thenReturn(transactions);
        
        // Act
        Wallet historicalWallet = service.findBalanceAt(walletId, targetDate);
        
        // Assert
        // Expected calculation: 100 (deposit) - 30 (withdrawal) + 50 (incoming transfer) - 20 (outgoing transfer) = 100
        assertEquals(new BigDecimal("100.00"), historicalWallet.getBalance());
        assertEquals(walletId, historicalWallet.getId());
        assertEquals(userId, historicalWallet.getUserId());
        
        verify(walletRepository).findById(walletId);
        verify(transactionRepository).findByWalletIdAndTimestampLessThanEqual(eq(walletId), any(LocalDateTime.class));
    }
    
    @Test
    void shouldSupportSimpleDateFormat() {
        // Arrange
        UUID walletId = UUID.randomUUID();
        Wallet wallet = new Wallet(walletId, UUID.randomUUID(), BigDecimal.ZERO);
        
        String simpleDate = "2023-01-10 12:00:00";
        LocalDateTime expectedDateTime = LocalDateTime.of(2023, 1, 10, 12, 0, 0);
        
        when(walletRepository.findById(walletId)).thenReturn(Optional.of(wallet));
        when(transactionRepository.findByWalletIdAndTimestampLessThanEqual(eq(walletId), eq(expectedDateTime)))
            .thenReturn(new ArrayList<>());
        
        // Act
        service.findBalanceAt(walletId, simpleDate);
        
        // Assert
        verify(transactionRepository).findByWalletIdAndTimestampLessThanEqual(eq(walletId), eq(expectedDateTime));
    }
    
    @Test
    void shouldThrowExceptionForInvalidDateFormat() {
        // Arrange
        UUID walletId = UUID.randomUUID();
        Wallet wallet = new Wallet(walletId, UUID.randomUUID(), BigDecimal.ZERO);
        String invalidDate = "10/01/2023";
        
        when(walletRepository.findById(walletId)).thenReturn(Optional.of(wallet));
        
        // Act & Assert
        assertThrows(
            InvalidDateFormatException.class,
            () -> service.findBalanceAt(walletId, invalidDate)
        );
        
        verify(walletRepository).findById(walletId);
        verify(transactionRepository, never()).findByWalletIdAndTimestampLessThanEqual(any(UUID.class), any(LocalDateTime.class));
    }
    
    @Test
    void shouldThrowExceptionWhenWalletNotFound() {
        // Arrange
        UUID walletId = UUID.randomUUID();
        String timestamp = "2023-01-01T12:00:00";
        
        when(walletRepository.findById(walletId)).thenReturn(Optional.empty());
        
        // Act & Assert
        assertThrows(
            WalletNotFoundException.class,
            () -> service.findBalanceAt(walletId, timestamp)
        );
        
        verify(walletRepository).findById(walletId);
    }
}

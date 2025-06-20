package com.recargapay.wallet.core.services;

import com.recargapay.wallet.core.domain.Transaction;
import com.recargapay.wallet.core.domain.TransactionType;
import com.recargapay.wallet.core.domain.User;
import com.recargapay.wallet.core.domain.Wallet;
import com.recargapay.wallet.core.exceptions.InvalidDateFormatException;
import com.recargapay.wallet.core.exceptions.UserNotFoundException;
import com.recargapay.wallet.core.exceptions.WalletAlreadyExistsException;
import com.recargapay.wallet.core.exceptions.WalletNotFoundException;
import com.recargapay.wallet.core.ports.out.TransactionRepository;
import com.recargapay.wallet.core.ports.out.UserRepository;
import com.recargapay.wallet.core.ports.out.WalletRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CreateWalletServiceTest {
    private WalletRepository walletRepository;
    private UserRepository userRepository;
    private TransactionRepository transactionRepository;
    private CreateWalletService service;

    @BeforeEach
    void setUp() {
        walletRepository = mock(WalletRepository.class);
        userRepository = mock(UserRepository.class);
        transactionRepository = mock(TransactionRepository.class);
        service = new CreateWalletService(walletRepository, userRepository, transactionRepository);
    }

    @Test
    void shouldCreateWalletSuccessfully() {
        // Arrange
        UUID userId = UUID.randomUUID();
        Wallet wallet = new Wallet(UUID.randomUUID(), userId, null);
        User user = new User();
        user.setId(userId);
        
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(walletRepository.findByUserId(userId)).thenReturn(Optional.empty());
        when(walletRepository.save(wallet)).thenReturn(wallet);
        
        // Act
        Wallet created = service.create(wallet);
        
        // Assert
        assertEquals(wallet, created);
        verify(userRepository).findById(userId);
        verify(walletRepository).findByUserId(userId);
        verify(walletRepository).save(wallet);
    }
    
    @Test
    void shouldThrowExceptionWhenUserNotFound() {
        // Arrange
        UUID userId = UUID.randomUUID();
        Wallet wallet = new Wallet(UUID.randomUUID(), userId, null);
        
        when(userRepository.findById(userId)).thenReturn(Optional.empty());
        
        // Act & Assert
        UserNotFoundException exception = assertThrows(
            UserNotFoundException.class,
            () -> service.create(wallet)
        );
        
        assertTrue(exception.getMessage().contains(userId.toString()));
        verify(userRepository).findById(userId);
        verify(walletRepository, never()).save(any(Wallet.class));
    }
    
    @Test
    void shouldThrowExceptionWhenWalletAlreadyExists() {
        // Arrange
        UUID userId = UUID.randomUUID();
        Wallet existingWallet = new Wallet(UUID.randomUUID(), userId, null);
        Wallet newWallet = new Wallet(null, userId, null);
        User user = new User();
        user.setId(userId);
        
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(walletRepository.findByUserId(userId)).thenReturn(Optional.of(existingWallet));
        
        // Act & Assert
        WalletAlreadyExistsException exception = assertThrows(
            WalletAlreadyExistsException.class,
            () -> service.create(newWallet)
        );
        
        assertTrue(exception.getMessage().contains(userId.toString()));
        verify(userRepository).findById(userId);
        verify(walletRepository).findByUserId(userId);
        verify(walletRepository, never()).save(any(Wallet.class));
    }
    
    @Test
    void findById_shouldReturnWalletWhenExists() {
        // Arrange
        UUID walletId = UUID.randomUUID();
        Wallet wallet = new Wallet(walletId, UUID.randomUUID(), null);
        
        when(walletRepository.findById(walletId)).thenReturn(Optional.of(wallet));
        
        // Act
        Wallet found = service.findById(walletId);
        
        // Assert
        assertEquals(wallet, found);
        verify(walletRepository).findById(walletId);
    }
    
    @Test
    void findById_shouldThrowExceptionWhenWalletNotFound() {
        // Arrange
        UUID walletId = UUID.randomUUID();
        
        when(walletRepository.findById(walletId)).thenReturn(Optional.empty());
        
        // Act & Assert
        assertThrows(
            WalletNotFoundException.class,
            () -> service.findById(walletId)
        );
        
        verify(walletRepository).findById(walletId);
    }
    
    @Test
    void findBalanceAt_shouldReturnWalletWhenExists() {
        // Arrange
        UUID walletId = UUID.randomUUID();
        Wallet wallet = new Wallet(walletId, UUID.randomUUID(), null);
        String timestamp = "2023-01-01T12:00:00";
        
        when(walletRepository.findById(walletId)).thenReturn(Optional.of(wallet));
        
        // Act
        Wallet found = service.findBalanceAt(walletId, timestamp);
        
        // Assert
        assertEquals(wallet, found);
        verify(walletRepository).findById(walletId);
    }
    
    @Test
    void findBalanceAt_shouldThrowExceptionWhenWalletNotFound() {
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
    
    @Test
    void findBalanceAt_shouldCalculateCorrectHistoricalBalance() {
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
        
        // This transaction is after the target date and should not be considered
        Transaction laterDeposit = new Transaction();
        laterDeposit.setId(UUID.randomUUID());
        laterDeposit.setWalletId(walletId);
        laterDeposit.setAmount(new BigDecimal("200.00"));
        laterDeposit.setType(TransactionType.DEPOSIT);
        laterDeposit.setTimestamp(LocalDateTime.of(2023, 1, 11, 10, 0, 0));
        
        when(walletRepository.findById(walletId)).thenReturn(Optional.of(wallet));
        when(transactionRepository.findByWalletIdAndTimestampLessThanEqual(walletId, targetDateTime))
            .thenReturn(transactions);
        
        // Act
        Wallet historicalWallet = service.findBalanceAt(walletId, targetDate);
        
        // Assert
        // Expected calculation: 100 (deposit) - 30 (withdrawal) + 50 (incoming transfer) - 20 (outgoing transfer) = 100
        assertEquals(new BigDecimal("100.00"), historicalWallet.getBalance());
        assertEquals(walletId, historicalWallet.getId());
        assertEquals(userId, historicalWallet.getUserId());
        
        verify(walletRepository).findById(walletId);
        verify(transactionRepository).findByWalletIdAndTimestampLessThanEqual(walletId, targetDateTime);
    }
    
    @Test
    void findBalanceAt_shouldSupportSimpleDateFormat() {
        // Arrange
        UUID walletId = UUID.randomUUID();
        Wallet wallet = new Wallet(walletId, UUID.randomUUID(), BigDecimal.ZERO);
        
        String simpleDate = "2023-01-10 12:00:00";
        LocalDateTime targetDateTime = LocalDateTime.of(2023, 1, 10, 12, 0, 0);
        
        when(walletRepository.findById(walletId)).thenReturn(Optional.of(wallet));
        when(transactionRepository.findByWalletIdAndTimestampLessThanEqual(eq(walletId), any(LocalDateTime.class)))
            .thenReturn(new ArrayList<>());
        
        // Act
        service.findBalanceAt(walletId, simpleDate);
        
        // Assert
        verify(walletRepository).findById(walletId);
        verify(transactionRepository).findByWalletIdAndTimestampLessThanEqual(eq(walletId), any(LocalDateTime.class));
    }
    
    @Test
    void findBalanceAt_shouldThrowExceptionForInvalidDateFormat() {
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
}

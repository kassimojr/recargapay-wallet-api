package com.recargapay.wallet.core.services;

import com.recargapay.wallet.core.domain.Transaction;
import com.recargapay.wallet.core.domain.TransactionType;
import com.recargapay.wallet.core.domain.Wallet;
import com.recargapay.wallet.core.exceptions.WalletNotFoundException;
import com.recargapay.wallet.core.ports.out.TransactionRepository;
import com.recargapay.wallet.core.ports.out.WalletRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DepositServiceTest {

    @Mock
    private WalletRepository walletRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @InjectMocks
    private DepositService depositService;

    @Captor
    private ArgumentCaptor<Wallet> walletCaptor;

    @Captor
    private ArgumentCaptor<Transaction> transactionCaptor;

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
    }

    @Test
    void deposit_shouldIncreaseBalanceAndCreateTransaction() {
        // Arrange
        when(walletRepository.findById(walletId)).thenReturn(Optional.of(wallet));
        when(walletRepository.save(any(Wallet.class))).thenReturn(wallet);
        
        Transaction savedTransaction = new Transaction();
        savedTransaction.setId(UUID.randomUUID());
        savedTransaction.setWalletId(walletId);
        savedTransaction.setAmount(depositAmount);
        savedTransaction.setType(TransactionType.DEPOSIT);
        savedTransaction.setRelatedUserId(userId);
        
        when(transactionRepository.saveAndReturn(any(Transaction.class))).thenReturn(savedTransaction);

        // Act
        Transaction result = depositService.deposit(walletId, depositAmount);

        // Assert
        verify(walletRepository).findById(walletId);
        verify(walletRepository).save(walletCaptor.capture());
        verify(transactionRepository).saveAndReturn(transactionCaptor.capture());

        Wallet capturedWallet = walletCaptor.getValue();
        Transaction capturedTransaction = transactionCaptor.getValue();

        assertEquals(initialBalance.add(depositAmount), capturedWallet.getBalance());
        assertEquals(walletId, capturedTransaction.getWalletId());
        assertEquals(depositAmount, capturedTransaction.getAmount());
        assertEquals(TransactionType.DEPOSIT, capturedTransaction.getType());
        assertEquals(userId, capturedTransaction.getRelatedUserId());
        assertNotNull(result);
    }

    @Test
    void deposit_whenWalletNotFound_shouldThrowException() {
        // Arrange
        when(walletRepository.findById(walletId)).thenReturn(Optional.empty());

        // Act & Assert
        WalletNotFoundException exception = assertThrows(
                WalletNotFoundException.class,
                () -> depositService.deposit(walletId, depositAmount)
        );

        // Verify
        verify(walletRepository).findById(walletId);
        verify(walletRepository, never()).save(any());
        verify(transactionRepository, never()).saveAndReturn(any());
        assertTrue(exception.getMessage().contains(walletId.toString()));
    }

    @Test
    void deposit_shouldHandleNullWalletId() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> depositService.deposit(null, depositAmount)
        );

        // Verify
        verify(walletRepository, never()).findById(any());
        verify(walletRepository, never()).save(any());
        verify(transactionRepository, never()).saveAndReturn(any());
    }
}

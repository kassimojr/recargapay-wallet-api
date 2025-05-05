package com.recargapay.wallet.core.services;

import com.recargapay.wallet.core.domain.Transaction;
import com.recargapay.wallet.core.domain.Wallet;
import com.recargapay.wallet.core.exceptions.InsufficientBalanceException;
import com.recargapay.wallet.core.exceptions.WalletNotFoundException;
import com.recargapay.wallet.core.ports.out.TransactionRepository;
import com.recargapay.wallet.core.ports.out.WalletRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class WithdrawServiceTest {
    private WalletRepository walletRepository;
    private TransactionRepository transactionRepository;
    private WithdrawService service;

    @BeforeEach
    void setUp() {
        walletRepository = mock(WalletRepository.class);
        transactionRepository = mock(TransactionRepository.class);
        service = new WithdrawService(walletRepository, transactionRepository);
    }

    @Test
    void shouldWithdrawSuccessfully() {
        UUID walletId = UUID.randomUUID();
        Wallet wallet = new Wallet(walletId, UUID.randomUUID(), new BigDecimal("100.00"));
        when(walletRepository.findById(walletId)).thenReturn(Optional.of(wallet));

        Transaction tx = service.withdraw(walletId, new BigDecimal("40.00"));

        assertEquals(new BigDecimal("60.00"), wallet.getBalance());
        verify(walletRepository).save(wallet);
        verify(transactionRepository).save(any(Transaction.class));
        assertEquals(walletId, tx.getWalletId());
        assertEquals(new BigDecimal("-40.00"), tx.getAmount());
    }

    @Test
    void shouldThrowWhenWalletNotFound() {
        UUID walletId = UUID.randomUUID();
        when(walletRepository.findById(walletId)).thenReturn(Optional.empty());
        assertThrows(WalletNotFoundException.class, () ->
            service.withdraw(walletId, new BigDecimal("10.00"))
        );
    }

    @Test
    void shouldThrowWhenInsufficientBalance() {
        UUID walletId = UUID.randomUUID();
        Wallet wallet = new Wallet(walletId, UUID.randomUUID(), new BigDecimal("20.00"));
        when(walletRepository.findById(walletId)).thenReturn(Optional.of(wallet));
        assertThrows(InsufficientBalanceException.class, () ->
            service.withdraw(walletId, new BigDecimal("30.00"))
        );
    }
}

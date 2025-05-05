package com.recargapay.wallet.core.services;

import com.recargapay.wallet.core.domain.Transaction;
import com.recargapay.wallet.core.domain.Wallet;
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

class DepositServiceTest {
    private WalletRepository walletRepository;
    private TransactionRepository transactionRepository;
    private DepositService service;

    @BeforeEach
    void setUp() {
        walletRepository = mock(WalletRepository.class);
        transactionRepository = mock(TransactionRepository.class);
        service = new DepositService(walletRepository, transactionRepository);
    }

    @Test
    void shouldDepositSuccessfully() {
        UUID walletId = UUID.randomUUID();
        Wallet wallet = new Wallet(walletId, UUID.randomUUID(), new BigDecimal("100.00"));
        when(walletRepository.findById(walletId)).thenReturn(Optional.of(wallet));

        Transaction tx = service.deposit(walletId, new BigDecimal("50.00"));

        assertEquals(new BigDecimal("150.00"), wallet.getBalance());
        verify(walletRepository).save(wallet);
        verify(transactionRepository).save(any(Transaction.class));
        assertEquals(walletId, tx.getWalletId());
        assertEquals(new BigDecimal("50.00"), tx.getAmount());
    }

    @Test
    void shouldThrowWhenWalletNotFound() {
        UUID walletId = UUID.randomUUID();
        when(walletRepository.findById(walletId)).thenReturn(Optional.empty());
        assertThrows(WalletNotFoundException.class, () ->
            service.deposit(walletId, new BigDecimal("10.00"))
        );
    }
}

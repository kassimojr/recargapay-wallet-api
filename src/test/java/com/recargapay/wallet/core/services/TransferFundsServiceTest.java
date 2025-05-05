package com.recargapay.wallet.core.services;

import com.recargapay.wallet.core.domain.TransactionType;
import com.recargapay.wallet.core.domain.Wallet;
import com.recargapay.wallet.core.exceptions.InsufficientBalanceException;
import com.recargapay.wallet.core.exceptions.WalletNotFoundException;
import com.recargapay.wallet.core.ports.out.TransactionRepository;
import com.recargapay.wallet.core.ports.out.WalletRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TransferFundsServiceTest {
    private WalletRepository walletRepository;
    private TransactionRepository transactionRepository;
    private TransferFundsService service;

    private UUID fromWalletId;
    private UUID toWalletId;
    private Wallet fromWallet;
    private Wallet toWallet;

    @BeforeEach
    void setUp() {
        walletRepository = mock(WalletRepository.class);
        transactionRepository = mock(TransactionRepository.class);
        service = new TransferFundsService(walletRepository, transactionRepository);
        fromWalletId = UUID.randomUUID();
        toWalletId = UUID.randomUUID();
        fromWallet = new Wallet(fromWalletId, UUID.randomUUID(), new BigDecimal("100.00"));
        toWallet = new Wallet(toWalletId, UUID.randomUUID(), new BigDecimal("50.00"));
    }

    @Test
    void shouldTransferFundsSuccessfully() {
        when(walletRepository.findById(fromWalletId)).thenReturn(Optional.of(fromWallet));
        when(walletRepository.findById(toWalletId)).thenReturn(Optional.of(toWallet));

        service.transfer(fromWalletId, toWalletId, new BigDecimal("30.00"));

        assertEquals(new BigDecimal("70.00"), fromWallet.getBalance());
        assertEquals(new BigDecimal("80.00"), toWallet.getBalance());
        verify(walletRepository).update(fromWallet);
        verify(walletRepository).update(toWallet);
        verify(transactionRepository, times(2)).save(any());
    }

    @Test
    void shouldThrowWhenFromWalletNotFound() {
        when(walletRepository.findById(fromWalletId)).thenReturn(Optional.empty());
        when(walletRepository.findById(toWalletId)).thenReturn(Optional.of(toWallet));
        WalletNotFoundException ex = assertThrows(WalletNotFoundException.class, () ->
            service.transfer(fromWalletId, toWalletId, new BigDecimal("10.00"))
        );
        assertTrue(ex.getMessage().contains("Carteira de origem"));
    }

    @Test
    void shouldThrowWhenToWalletNotFound() {
        when(walletRepository.findById(fromWalletId)).thenReturn(Optional.of(fromWallet));
        when(walletRepository.findById(toWalletId)).thenReturn(Optional.empty());
        WalletNotFoundException ex = assertThrows(WalletNotFoundException.class, () ->
            service.transfer(fromWalletId, toWalletId, new BigDecimal("10.00"))
        );
        assertTrue(ex.getMessage().contains("Carteira de destino"));
    }

    @Test
    void shouldThrowWhenAmountIsNull() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
            service.transfer(fromWalletId, toWalletId, null)
        );
        assertTrue(ex.getMessage().contains("nulo"));
    }

    @Test
    void shouldThrowWhenAmountIsZeroOrNegative() {
        IllegalArgumentException ex1 = assertThrows(IllegalArgumentException.class, () ->
            service.transfer(fromWalletId, toWalletId, BigDecimal.ZERO)
        );
        IllegalArgumentException ex2 = assertThrows(IllegalArgumentException.class, () ->
            service.transfer(fromWalletId, toWalletId, new BigDecimal("-5.00"))
        );
        assertTrue(ex1.getMessage().contains("positivo"));
        assertTrue(ex2.getMessage().contains("positivo"));
    }

    @Test
    void shouldThrowWhenSameWallet() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
            service.transfer(fromWalletId, fromWalletId, new BigDecimal("10.00"))
        );
        assertTrue(ex.getMessage().contains("mesma carteira"));
    }

    @Test
    void shouldThrowWhenInsufficientBalance() {
        when(walletRepository.findById(fromWalletId)).thenReturn(Optional.of(fromWallet));
        when(walletRepository.findById(toWalletId)).thenReturn(Optional.of(toWallet));
        InsufficientBalanceException ex = assertThrows(InsufficientBalanceException.class, () ->
            service.transfer(fromWalletId, toWalletId, new BigDecimal("150.00"))
        );
        assertTrue(ex.getMessage().contains("Saldo insuficiente"));
    }
}

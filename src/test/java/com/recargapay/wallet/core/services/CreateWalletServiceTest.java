package com.recargapay.wallet.core.services;

import com.recargapay.wallet.core.domain.Wallet;
import com.recargapay.wallet.core.ports.out.WalletRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CreateWalletServiceTest {
    private WalletRepository walletRepository;
    private CreateWalletService service;

    @BeforeEach
    void setUp() {
        walletRepository = mock(WalletRepository.class);
        service = new CreateWalletService(walletRepository);
    }

    @Test
    void shouldCreateWalletSuccessfully() {
        Wallet wallet = new Wallet(UUID.randomUUID(), UUID.randomUUID(), null);
        when(walletRepository.save(wallet)).thenReturn(wallet);
        Wallet created = service.create(wallet);
        assertEquals(wallet, created);
        verify(walletRepository).save(wallet);
    }
}

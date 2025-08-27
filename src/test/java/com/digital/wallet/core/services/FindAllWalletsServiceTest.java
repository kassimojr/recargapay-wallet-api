package com.digital.wallet.core.services;

import com.digital.wallet.core.domain.Wallet;
import com.digital.wallet.core.ports.out.WalletRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class FindAllWalletsServiceTest {
    private WalletRepository walletRepository;
    private FindAllWalletsService service;

    @BeforeEach
    void setUp() {
        walletRepository = mock(WalletRepository.class);
        service = new FindAllWalletsService(walletRepository);
    }

    @Test
    void shouldReturnAllWallets() {
        // Arrange
        UUID walletId1 = UUID.randomUUID();
        UUID userId1 = UUID.randomUUID();
        Wallet wallet1 = new Wallet(walletId1, userId1, BigDecimal.valueOf(100));

        UUID walletId2 = UUID.randomUUID();
        UUID userId2 = UUID.randomUUID();
        Wallet wallet2 = new Wallet(walletId2, userId2, BigDecimal.valueOf(200));

        List<Wallet> expectedWallets = Arrays.asList(wallet1, wallet2);
        
        when(walletRepository.findAll()).thenReturn(expectedWallets);
        
        // Act
        List<Wallet> actualWallets = service.findAll();
        
        // Assert
        assertEquals(expectedWallets.size(), actualWallets.size());
        assertEquals(expectedWallets, actualWallets);
        verify(walletRepository).findAll();
    }

    @Test
    void shouldReturnEmptyListWhenNoWallets() {
        // Arrange
        when(walletRepository.findAll()).thenReturn(List.of());
        
        // Act
        List<Wallet> actualWallets = service.findAll();
        
        // Assert
        assertTrue(actualWallets.isEmpty());
        verify(walletRepository).findAll();
    }
}

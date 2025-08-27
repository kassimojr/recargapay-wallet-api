package com.digital.wallet.infra.metrics;

import com.digital.wallet.core.domain.Wallet;
import com.digital.wallet.core.ports.out.WalletRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("WalletMetricsScheduler Tests")
class WalletMetricsSchedulerTest {

    @Mock
    private WalletRepository walletRepository;

    @Mock
    private WalletGaugeRegistry walletGaugeRegistry;

    private WalletMetricsScheduler walletMetricsScheduler;

    @BeforeEach
    void setUp() {
        walletMetricsScheduler = new WalletMetricsScheduler(walletRepository, walletGaugeRegistry);
    }

    @Test
    @DisplayName("Should initialize scheduler with dependencies successfully")
    void shouldInitializeSchedulerWithDependenciesSuccessfully() {
        // Given, When & Then
        assertNotNull(walletMetricsScheduler, "Scheduler should be initialized");
        // Constructor should complete without exceptions
    }

    @Test
    @DisplayName("Should initialize metrics on startup")
    void shouldInitializeMetricsOnStartup() {
        // Given
        List<Wallet> wallets = Arrays.asList(
            createWallet("wallet-1", "100.00"),
            createWallet("wallet-2", "200.00")
        );
        when(walletRepository.findAll()).thenReturn(wallets);

        // When
        walletMetricsScheduler.initializeMetrics();

        // Then
        verify(walletRepository, times(1)).findAll();
        verify(walletGaugeRegistry, times(1)).updateWalletBalance(
            wallets.get(0).getId().toString(), 
            wallets.get(0).getBalance(), 
            "BRL"
        );
        verify(walletGaugeRegistry, times(1)).updateWalletBalance(
            wallets.get(1).getId().toString(), 
            wallets.get(1).getBalance(), 
            "BRL"
        );
    }

    @Test
    @DisplayName("Should update wallet balance metrics successfully")
    void shouldUpdateWalletBalanceMetricsSuccessfully() {
        // Given
        List<Wallet> wallets = Arrays.asList(
            createWallet("wallet-1", "150.75"),
            createWallet("wallet-2", "300.25"),
            createWallet("wallet-3", "50.00")
        );
        when(walletRepository.findAll()).thenReturn(wallets);

        // When
        walletMetricsScheduler.updateWalletBalanceMetrics();

        // Then
        verify(walletRepository, times(1)).findAll();
        
        for (Wallet wallet : wallets) {
            verify(walletGaugeRegistry, times(1)).updateWalletBalance(
                wallet.getId().toString(),
                wallet.getBalance(),
                "BRL"
            );
        }
    }

    @Test
    @DisplayName("Should handle empty wallet list gracefully")
    void shouldHandleEmptyWalletListGracefully() {
        // Given
        when(walletRepository.findAll()).thenReturn(Collections.emptyList());

        // When & Then - Should not throw exception
        assertDoesNotThrow(() -> walletMetricsScheduler.updateWalletBalanceMetrics());

        // Verify repository was called but no gauge updates occurred
        verify(walletRepository, times(1)).findAll();
        verify(walletGaugeRegistry, never()).updateWalletBalance(anyString(), any(BigDecimal.class), anyString());
    }

    @Test
    @DisplayName("Should handle single wallet update")
    void shouldHandleSingleWalletUpdate() {
        // Given
        Wallet singleWallet = createWallet("single-wallet", "999.99");
        when(walletRepository.findAll()).thenReturn(Collections.singletonList(singleWallet));

        // When
        walletMetricsScheduler.updateWalletBalanceMetrics();

        // Then
        verify(walletRepository, times(1)).findAll();
        verify(walletGaugeRegistry, times(1)).updateWalletBalance(
            singleWallet.getId().toString(),
            singleWallet.getBalance(),
            "BRL"
        );
    }

    @Test
    @DisplayName("Should handle wallets with zero balance")
    void shouldHandleWalletsWithZeroBalance() {
        // Given
        List<Wallet> wallets = Arrays.asList(
            createWallet("wallet-zero", "0.00"),
            createWallet("wallet-positive", "100.00")
        );
        when(walletRepository.findAll()).thenReturn(wallets);

        // When
        walletMetricsScheduler.updateWalletBalanceMetrics();

        // Then
        verify(walletRepository, times(1)).findAll();
        verify(walletGaugeRegistry, times(1)).updateWalletBalance(
            wallets.get(0).getId().toString(),
            new BigDecimal("0.00"),
            "BRL"
        );
        verify(walletGaugeRegistry, times(1)).updateWalletBalance(
            wallets.get(1).getId().toString(),
            new BigDecimal("100.00"),
            "BRL"
        );
    }

    @Test
    @DisplayName("Should handle wallets with negative balance")
    void shouldHandleWalletsWithNegativeBalance() {
        // Given
        List<Wallet> wallets = Arrays.asList(
            createWallet("wallet-negative", "-50.25"),
            createWallet("wallet-positive", "75.50")
        );
        when(walletRepository.findAll()).thenReturn(wallets);

        // When
        walletMetricsScheduler.updateWalletBalanceMetrics();

        // Then
        verify(walletRepository, times(1)).findAll();
        verify(walletGaugeRegistry, times(1)).updateWalletBalance(
            wallets.get(0).getId().toString(),
            new BigDecimal("-50.25"),
            "BRL"
        );
        verify(walletGaugeRegistry, times(1)).updateWalletBalance(
            wallets.get(1).getId().toString(),
            new BigDecimal("75.50"),
            "BRL"
        );
    }

    @Test
    @DisplayName("Should handle large number of wallets")
    void shouldHandleLargeNumberOfWallets() {
        // Given
        List<Wallet> wallets = Arrays.asList(
            createWallet("wallet-1", "100.00"),
            createWallet("wallet-2", "200.00"),
            createWallet("wallet-3", "300.00"),
            createWallet("wallet-4", "400.00"),
            createWallet("wallet-5", "500.00"),
            createWallet("wallet-6", "600.00"),
            createWallet("wallet-7", "700.00"),
            createWallet("wallet-8", "800.00"),
            createWallet("wallet-9", "900.00"),
            createWallet("wallet-10", "1000.00")
        );
        when(walletRepository.findAll()).thenReturn(wallets);

        // When
        walletMetricsScheduler.updateWalletBalanceMetrics();

        // Then
        verify(walletRepository, times(1)).findAll();
        
        // Verify all wallets were processed
        for (Wallet wallet : wallets) {
            verify(walletGaugeRegistry, times(1)).updateWalletBalance(
                wallet.getId().toString(),
                wallet.getBalance(),
                "BRL"
            );
        }
    }

    @Test
    @DisplayName("Should handle repository exception gracefully")
    void shouldHandleRepositoryExceptionGracefully() {
        // Given
        when(walletRepository.findAll()).thenThrow(new RuntimeException("Database connection failed"));

        // When & Then - Should not propagate exception
        assertDoesNotThrow(() -> walletMetricsScheduler.updateWalletBalanceMetrics());

        // Verify repository was called but no gauge updates occurred
        verify(walletRepository, times(1)).findAll();
        verify(walletGaugeRegistry, never()).updateWalletBalance(anyString(), any(BigDecimal.class), anyString());
    }

    @Test
    @DisplayName("Should handle gauge registry exception gracefully")
    void shouldHandleGaugeRegistryExceptionGracefully() {
        // Given
        Wallet wallet = createWallet("wallet-exception", "100.00");
        when(walletRepository.findAll()).thenReturn(Collections.singletonList(wallet));
        doThrow(new RuntimeException("Metrics registry error"))
            .when(walletGaugeRegistry).updateWalletBalance(anyString(), any(BigDecimal.class), anyString());

        // When & Then - Should not propagate exception
        assertDoesNotThrow(() -> walletMetricsScheduler.updateWalletBalanceMetrics());

        // Verify both repository and gauge registry were called
        verify(walletRepository, times(1)).findAll();
        verify(walletGaugeRegistry, times(1)).updateWalletBalance(
            wallet.getId().toString(),
            wallet.getBalance(),
            "BRL"
        );
    }

    @Test
    @DisplayName("Should handle wallets with high precision balances")
    void shouldHandleWalletsWithHighPrecisionBalances() {
        // Given
        List<Wallet> wallets = Arrays.asList(
            createWallet("wallet-precision-1", "123.456789"),
            createWallet("wallet-precision-2", "987.654321")
        );
        when(walletRepository.findAll()).thenReturn(wallets);

        // When
        walletMetricsScheduler.updateWalletBalanceMetrics();

        // Then
        verify(walletRepository, times(1)).findAll();
        verify(walletGaugeRegistry, times(1)).updateWalletBalance(
            wallets.get(0).getId().toString(),
            new BigDecimal("123.456789"),
            "BRL"
        );
        verify(walletGaugeRegistry, times(1)).updateWalletBalance(
            wallets.get(1).getId().toString(),
            new BigDecimal("987.654321"),
            "BRL"
        );
    }

    @Test
    @DisplayName("Should handle wallets with very large balances")
    void shouldHandleWalletsWithVeryLargeBalances() {
        // Given
        List<Wallet> wallets = Arrays.asList(
            createWallet("wallet-large-1", "999999999.99"),
            createWallet("wallet-large-2", "1000000000.00")
        );
        when(walletRepository.findAll()).thenReturn(wallets);

        // When
        walletMetricsScheduler.updateWalletBalanceMetrics();

        // Then
        verify(walletRepository, times(1)).findAll();
        verify(walletGaugeRegistry, times(1)).updateWalletBalance(
            wallets.get(0).getId().toString(),
            new BigDecimal("999999999.99"),
            "BRL"
        );
        verify(walletGaugeRegistry, times(1)).updateWalletBalance(
            wallets.get(1).getId().toString(),
            new BigDecimal("1000000000.00"),
            "BRL"
        );
    }

    @Test
    @DisplayName("Should use BRL as default currency for all wallets")
    void shouldUseBRLAsDefaultCurrencyForAllWallets() {
        // Given
        List<Wallet> wallets = Arrays.asList(
            createWallet("wallet-1", "100.00"),
            createWallet("wallet-2", "200.00"),
            createWallet("wallet-3", "300.00")
        );
        when(walletRepository.findAll()).thenReturn(wallets);

        // When
        walletMetricsScheduler.updateWalletBalanceMetrics();

        // Then
        verify(walletRepository, times(1)).findAll();
        
        // Verify all calls use "BRL" as currency
        for (Wallet wallet : wallets) {
            verify(walletGaugeRegistry, times(1)).updateWalletBalance(
                wallet.getId().toString(),
                wallet.getBalance(),
                "BRL"  // Verify default currency is always BRL
            );
        }
    }

    @Test
    @DisplayName("Should handle multiple consecutive updates")
    void shouldHandleMultipleConsecutiveUpdates() {
        // Given
        Wallet wallet = createWallet("wallet-consecutive", "100.00");
        when(walletRepository.findAll()).thenReturn(Collections.singletonList(wallet));

        // When - Multiple consecutive calls
        walletMetricsScheduler.updateWalletBalanceMetrics();
        walletMetricsScheduler.updateWalletBalanceMetrics();
        walletMetricsScheduler.updateWalletBalanceMetrics();

        // Then
        verify(walletRepository, times(3)).findAll();
        verify(walletGaugeRegistry, times(3)).updateWalletBalance(
            wallet.getId().toString(),
            wallet.getBalance(),
            "BRL"
        );
    }

    /**
     * Helper method to create a wallet for testing
     */
    private Wallet createWallet(String walletIdSuffix, String balance) {
        UUID walletId = UUID.nameUUIDFromBytes(walletIdSuffix.getBytes());
        UUID userId = UUID.randomUUID();
        return new Wallet(walletId, userId, new BigDecimal(balance));
    }
}

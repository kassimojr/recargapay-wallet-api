package com.recargapay.wallet.infra.metrics;

import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("WalletGaugeRegistry Tests")
class WalletGaugeRegistryTest {

    private WalletGaugeRegistry walletGaugeRegistry;
    private MeterRegistry meterRegistry;

    @BeforeEach
    void setUp() {
        walletGaugeRegistry = new WalletGaugeRegistry();
        meterRegistry = new SimpleMeterRegistry();
        walletGaugeRegistry.bindTo(meterRegistry);
    }

    @Test
    @DisplayName("Should bind to meter registry successfully")
    void shouldBindToMeterRegistrySuccessfully() {
        // Given
        WalletGaugeRegistry newRegistry = new WalletGaugeRegistry();
        MeterRegistry newMeterRegistry = new SimpleMeterRegistry();

        // When
        assertDoesNotThrow(() -> newRegistry.bindTo(newMeterRegistry));

        // Then - No exception should be thrown
        // The binding should be successful (tested implicitly by other tests)
    }

    @Test
    @DisplayName("Should register new wallet balance gauge for first time wallet")
    void shouldRegisterNewWalletBalanceGaugeForFirstTimeWallet() {
        // Given
        String walletId = "wallet-123";
        BigDecimal balance = new BigDecimal("100.50");
        String currency = "BRL";

        // When
        walletGaugeRegistry.updateWalletBalance(walletId, balance, currency);

        // Then
        Gauge gauge = meterRegistry.find("wallet_balance")
                .tag("wallet_id", walletId)
                .tag("currency", currency)
                .gauge();

        assertNotNull(gauge, "Gauge should be registered");
        assertEquals(100.50, gauge.value(), 0.001, "Gauge value should match the balance");
    }

    @Test
    @DisplayName("Should update existing wallet balance gauge")
    void shouldUpdateExistingWalletBalanceGauge() {
        // Given
        String walletId = "wallet-456";
        String currency = "USD";
        BigDecimal initialBalance = new BigDecimal("50.25");
        BigDecimal updatedBalance = new BigDecimal("75.75");

        // When - First update (creates gauge)
        walletGaugeRegistry.updateWalletBalance(walletId, initialBalance, currency);
        
        // Then - Verify initial gauge
        Gauge gauge = meterRegistry.find("wallet_balance")
                .tag("wallet_id", walletId)
                .tag("currency", currency)
                .gauge();
        assertNotNull(gauge);
        assertEquals(50.25, gauge.value(), 0.001);

        // When - Second update (updates existing gauge)
        walletGaugeRegistry.updateWalletBalance(walletId, updatedBalance, currency);

        // Then - Verify updated gauge
        assertEquals(75.75, gauge.value(), 0.001, "Gauge value should be updated");
    }

    @Test
    @DisplayName("Should handle multiple wallets with different currencies")
    void shouldHandleMultipleWalletsWithDifferentCurrencies() {
        // Given
        String wallet1 = "wallet-001";
        String wallet2 = "wallet-002";
        BigDecimal balance1 = new BigDecimal("100.00");
        BigDecimal balance2 = new BigDecimal("200.00");

        // When
        walletGaugeRegistry.updateWalletBalance(wallet1, balance1, "BRL");
        walletGaugeRegistry.updateWalletBalance(wallet2, balance2, "USD");

        // Then
        Gauge gauge1 = meterRegistry.find("wallet_balance")
                .tag("wallet_id", wallet1)
                .tag("currency", "BRL")
                .gauge();
        Gauge gauge2 = meterRegistry.find("wallet_balance")
                .tag("wallet_id", wallet2)
                .tag("currency", "USD")
                .gauge();

        assertNotNull(gauge1, "First wallet gauge should exist");
        assertNotNull(gauge2, "Second wallet gauge should exist");
        assertEquals(100.00, gauge1.value(), 0.001);
        assertEquals(200.00, gauge2.value(), 0.001);
    }

    @Test
    @DisplayName("Should handle same wallet with multiple currencies")
    void shouldHandleSameWalletWithMultipleCurrencies() {
        // Given
        String walletId = "wallet-multi";
        BigDecimal brlBalance = new BigDecimal("500.00");
        BigDecimal usdBalance = new BigDecimal("100.00");

        // When
        walletGaugeRegistry.updateWalletBalance(walletId, brlBalance, "BRL");
        walletGaugeRegistry.updateWalletBalance(walletId, usdBalance, "USD");

        // Then
        Gauge brlGauge = meterRegistry.find("wallet_balance")
                .tag("wallet_id", walletId)
                .tag("currency", "BRL")
                .gauge();
        Gauge usdGauge = meterRegistry.find("wallet_balance")
                .tag("wallet_id", walletId)
                .tag("currency", "USD")
                .gauge();

        assertNotNull(brlGauge, "BRL gauge should exist");
        assertNotNull(usdGauge, "USD gauge should exist");
        assertEquals(500.00, brlGauge.value(), 0.001);
        assertEquals(100.00, usdGauge.value(), 0.001);
    }

    @Test
    @DisplayName("Should handle null balance as zero")
    void shouldHandleNullBalanceAsZero() {
        // Given
        String walletId = "wallet-null";
        String currency = "BRL";

        // When
        walletGaugeRegistry.updateWalletBalance(walletId, null, currency);

        // Then
        Gauge gauge = meterRegistry.find("wallet_balance")
                .tag("wallet_id", walletId)
                .tag("currency", currency)
                .gauge();

        assertNotNull(gauge, "Gauge should be registered even with null balance");
        assertEquals(0.0, gauge.value(), 0.001, "Null balance should be treated as zero");
    }

    @Test
    @DisplayName("Should handle zero balance")
    void shouldHandleZeroBalance() {
        // Given
        String walletId = "wallet-zero";
        BigDecimal balance = BigDecimal.ZERO;
        String currency = "EUR";

        // When
        walletGaugeRegistry.updateWalletBalance(walletId, balance, currency);

        // Then
        Gauge gauge = meterRegistry.find("wallet_balance")
                .tag("wallet_id", walletId)
                .tag("currency", currency)
                .gauge();

        assertNotNull(gauge, "Gauge should be registered with zero balance");
        assertEquals(0.0, gauge.value(), 0.001, "Zero balance should be handled correctly");
    }

    @Test
    @DisplayName("Should handle negative balance")
    void shouldHandleNegativeBalance() {
        // Given
        String walletId = "wallet-negative";
        BigDecimal balance = new BigDecimal("-50.25");
        String currency = "BRL";

        // When
        walletGaugeRegistry.updateWalletBalance(walletId, balance, currency);

        // Then
        Gauge gauge = meterRegistry.find("wallet_balance")
                .tag("wallet_id", walletId)
                .tag("currency", currency)
                .gauge();

        assertNotNull(gauge, "Gauge should be registered with negative balance");
        assertEquals(-50.25, gauge.value(), 0.001, "Negative balance should be handled correctly");
    }

    @Test
    @DisplayName("Should handle large balance values")
    void shouldHandleLargeBalanceValues() {
        // Given
        String walletId = "wallet-large";
        BigDecimal balance = new BigDecimal("999999999.99");
        String currency = "BRL";

        // When
        walletGaugeRegistry.updateWalletBalance(walletId, balance, currency);

        // Then
        Gauge gauge = meterRegistry.find("wallet_balance")
                .tag("wallet_id", walletId)
                .tag("currency", currency)
                .gauge();

        assertNotNull(gauge, "Gauge should be registered with large balance");
        assertEquals(999999999.99, gauge.value(), 0.001, "Large balance should be handled correctly");
    }

    @Test
    @DisplayName("Should handle high precision decimal values")
    void shouldHandleHighPrecisionDecimalValues() {
        // Given
        String walletId = "wallet-precision";
        BigDecimal balance = new BigDecimal("123.456789");
        String currency = "BTC";

        // When
        walletGaugeRegistry.updateWalletBalance(walletId, balance, currency);

        // Then
        Gauge gauge = meterRegistry.find("wallet_balance")
                .tag("wallet_id", walletId)
                .tag("currency", currency)
                .gauge();

        assertNotNull(gauge, "Gauge should be registered with high precision balance");
        assertEquals(123.456789, gauge.value(), 0.000001, "High precision balance should be handled correctly");
    }

    @Test
    @DisplayName("Should skip update when meter registry is not initialized")
    void shouldSkipUpdateWhenMeterRegistryNotInitialized() {
        // Given
        WalletGaugeRegistry uninitializedRegistry = new WalletGaugeRegistry();
        String walletId = "wallet-uninitialized";
        BigDecimal balance = new BigDecimal("100.00");
        String currency = "BRL";

        // When & Then - Should not throw exception
        assertDoesNotThrow(() -> 
            uninitializedRegistry.updateWalletBalance(walletId, balance, currency)
        );

        // Verify no gauge was registered in our test registry
        Gauge gauge = meterRegistry.find("wallet_balance")
                .tag("wallet_id", walletId)
                .tag("currency", currency)
                .gauge();
        assertNull(gauge, "No gauge should be registered when registry is not initialized");
    }

    @Test
    @DisplayName("Should handle special characters in wallet ID")
    void shouldHandleSpecialCharactersInWalletId() {
        // Given
        String walletId = "wallet-special-chars-@#$%";
        BigDecimal balance = new BigDecimal("42.42");
        String currency = "BRL";

        // When
        walletGaugeRegistry.updateWalletBalance(walletId, balance, currency);

        // Then
        Gauge gauge = meterRegistry.find("wallet_balance")
                .tag("wallet_id", walletId)
                .tag("currency", currency)
                .gauge();

        assertNotNull(gauge, "Gauge should be registered with special characters in wallet ID");
        assertEquals(42.42, gauge.value(), 0.001, "Balance should be correct with special characters");
    }

    @Test
    @DisplayName("Should handle multiple updates to same wallet efficiently")
    void shouldHandleMultipleUpdatesToSameWalletEfficiently() {
        // Given
        String walletId = "wallet-multiple-updates";
        String currency = "BRL";
        BigDecimal[] balances = {
            new BigDecimal("10.00"),
            new BigDecimal("20.00"),
            new BigDecimal("30.00"),
            new BigDecimal("40.00"),
            new BigDecimal("50.00")
        };

        // When - Multiple updates
        for (BigDecimal balance : balances) {
            walletGaugeRegistry.updateWalletBalance(walletId, balance, currency);
        }

        // Then - Should only have one gauge registered
        Gauge gauge = meterRegistry.find("wallet_balance")
                .tag("wallet_id", walletId)
                .tag("currency", currency)
                .gauge();

        assertNotNull(gauge, "Gauge should exist after multiple updates");
        assertEquals(50.00, gauge.value(), 0.001, "Final balance should be the last updated value");

        // Verify only one gauge exists for this wallet/currency combination
        long gaugeCount = meterRegistry.getMeters().stream()
                .filter(meter -> meter.getId().getName().equals("wallet_balance"))
                .filter(meter -> walletId.equals(meter.getId().getTag("wallet_id")))
                .filter(meter -> currency.equals(meter.getId().getTag("currency")))
                .count();

        assertEquals(1, gaugeCount, "Should only have one gauge registered per wallet/currency combination");
    }
}

package com.recargapay.wallet.infra.metrics;

import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static com.recargapay.wallet.infra.metrics.MetricsConstants.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("WalletBalanceMetrics Tests")
class WalletBalanceMetricsTest {

    private MeterRegistry meterRegistry;
    private WalletBalanceMetrics walletBalanceMetrics;

    @BeforeEach
    void setUp() {
        // Use SimpleMeterRegistry for testing
        meterRegistry = new SimpleMeterRegistry();
        walletBalanceMetrics = new WalletBalanceMetrics(meterRegistry);
        
        // Call initialize to simulate @PostConstruct
        walletBalanceMetrics.initialize();
    }

    @Test
    @DisplayName("Should initialize component successfully")
    void initialize_ShouldCompleteWithoutError() {
        // When initialize is called (already done in setUp)
        // Then no exception should be thrown and component should be ready
        assertNotNull(walletBalanceMetrics);
    }

    @Test
    @DisplayName("Should create new gauge when updating balance for first time")
    void updateBalance_FirstTime_ShouldCreateNewGauge() {
        // Arrange
        String walletId = "wallet-123";
        BigDecimal balance = new BigDecimal("100.50");
        String currency = "BRL";

        // Act
        walletBalanceMetrics.updateBalance(walletId, balance, currency);

        // Assert
        Gauge gauge = meterRegistry.find(WALLET_BALANCE)
                .tag(TAG_WALLET_ID, walletId)
                .tag(TAG_CURRENCY, currency)
                .gauge();
        
        assertNotNull(gauge);
        assertEquals(balance.doubleValue(), gauge.value(), 0.01);
    }

    @Test
    @DisplayName("Should update existing gauge when balance changes")
    void updateBalance_ExistingWallet_ShouldUpdateGaugeValue() {
        // Arrange
        String walletId = "wallet-456";
        BigDecimal initialBalance = new BigDecimal("200.00");
        BigDecimal updatedBalance = new BigDecimal("350.75");
        String currency = "USD";

        // Act - First update
        walletBalanceMetrics.updateBalance(walletId, initialBalance, currency);
        
        Gauge gaugeAfterFirst = meterRegistry.find(WALLET_BALANCE)
                .tag(TAG_WALLET_ID, walletId)
                .tag(TAG_CURRENCY, currency)
                .gauge();
        
        // Act - Second update
        walletBalanceMetrics.updateBalance(walletId, updatedBalance, currency);
        
        Gauge gaugeAfterSecond = meterRegistry.find(WALLET_BALANCE)
                .tag(TAG_WALLET_ID, walletId)
                .tag(TAG_CURRENCY, currency)
                .gauge();

        // Assert
        assertNotNull(gaugeAfterFirst);
        assertNotNull(gaugeAfterSecond);
        assertSame(gaugeAfterFirst, gaugeAfterSecond); // Should be the same gauge instance
        // Both gauges reference the same AtomicReference, so they both show the latest value
        assertEquals(updatedBalance.doubleValue(), gaugeAfterSecond.value(), 0.01);
    }

    @Test
    @DisplayName("Should handle multiple wallets with same currency")
    void updateBalance_MultipleWalletsSameCurrency_ShouldCreateSeparateGauges() {
        // Arrange
        String wallet1 = "wallet-111";
        String wallet2 = "wallet-222";
        BigDecimal balance1 = new BigDecimal("100.00");
        BigDecimal balance2 = new BigDecimal("200.00");
        String currency = "BRL";

        // Act
        walletBalanceMetrics.updateBalance(wallet1, balance1, currency);
        walletBalanceMetrics.updateBalance(wallet2, balance2, currency);

        // Assert
        Gauge gauge1 = meterRegistry.find(WALLET_BALANCE)
                .tag(TAG_WALLET_ID, wallet1)
                .tag(TAG_CURRENCY, currency)
                .gauge();
        
        Gauge gauge2 = meterRegistry.find(WALLET_BALANCE)
                .tag(TAG_WALLET_ID, wallet2)
                .tag(TAG_CURRENCY, currency)
                .gauge();

        assertNotNull(gauge1);
        assertNotNull(gauge2);
        assertNotSame(gauge1, gauge2); // Should be different gauge instances
        assertEquals(balance1.doubleValue(), gauge1.value(), 0.01);
        assertEquals(balance2.doubleValue(), gauge2.value(), 0.01);
    }

    @Test
    @DisplayName("Should handle same wallet with multiple currencies")
    void updateBalance_SameWalletMultipleCurrencies_ShouldCreateSeparateGauges() {
        // Arrange
        String walletId = "wallet-333";
        BigDecimal balanceBRL = new BigDecimal("500.00");
        BigDecimal balanceUSD = new BigDecimal("100.00");
        String currencyBRL = "BRL";
        String currencyUSD = "USD";

        // Act
        walletBalanceMetrics.updateBalance(walletId, balanceBRL, currencyBRL);
        walletBalanceMetrics.updateBalance(walletId, balanceUSD, currencyUSD);

        // Assert
        Gauge gaugeBRL = meterRegistry.find(WALLET_BALANCE)
                .tag(TAG_WALLET_ID, walletId)
                .tag(TAG_CURRENCY, currencyBRL)
                .gauge();
        
        Gauge gaugeUSD = meterRegistry.find(WALLET_BALANCE)
                .tag(TAG_WALLET_ID, walletId)
                .tag(TAG_CURRENCY, currencyUSD)
                .gauge();

        assertNotNull(gaugeBRL);
        assertNotNull(gaugeUSD);
        assertNotSame(gaugeBRL, gaugeUSD); // Should be different gauge instances
        assertEquals(balanceBRL.doubleValue(), gaugeBRL.value(), 0.01);
        assertEquals(balanceUSD.doubleValue(), gaugeUSD.value(), 0.01);
    }

    @Test
    @DisplayName("Should handle zero balance correctly")
    void updateBalance_ZeroBalance_ShouldRecordCorrectly() {
        // Arrange
        String walletId = "wallet-zero";
        BigDecimal zeroBalance = BigDecimal.ZERO;
        String currency = "EUR";

        // Act
        walletBalanceMetrics.updateBalance(walletId, zeroBalance, currency);

        // Assert
        Gauge gauge = meterRegistry.find(WALLET_BALANCE)
                .tag(TAG_WALLET_ID, walletId)
                .tag(TAG_CURRENCY, currency)
                .gauge();
        
        assertNotNull(gauge);
        assertEquals(0.0, gauge.value(), 0.01);
    }

    @Test
    @DisplayName("Should handle negative balance correctly")
    void updateBalance_NegativeBalance_ShouldRecordCorrectly() {
        // Arrange
        String walletId = "wallet-negative";
        BigDecimal negativeBalance = new BigDecimal("-50.25");
        String currency = "GBP";

        // Act
        walletBalanceMetrics.updateBalance(walletId, negativeBalance, currency);

        // Assert
        Gauge gauge = meterRegistry.find(WALLET_BALANCE)
                .tag(TAG_WALLET_ID, walletId)
                .tag(TAG_CURRENCY, currency)
                .gauge();
        
        assertNotNull(gauge);
        assertEquals(negativeBalance.doubleValue(), gauge.value(), 0.01);
    }

    @Test
    @DisplayName("Should handle large balance values correctly")
    void updateBalance_LargeBalance_ShouldRecordCorrectly() {
        // Arrange
        String walletId = "wallet-large";
        BigDecimal largeBalance = new BigDecimal("999999999.99");
        String currency = "JPY";

        // Act
        walletBalanceMetrics.updateBalance(walletId, largeBalance, currency);

        // Assert
        Gauge gauge = meterRegistry.find(WALLET_BALANCE)
                .tag(TAG_WALLET_ID, walletId)
                .tag(TAG_CURRENCY, currency)
                .gauge();
        
        assertNotNull(gauge);
        assertEquals(largeBalance.doubleValue(), gauge.value(), 0.01);
    }

    @Test
    @DisplayName("Should handle high precision decimal values correctly")
    void updateBalance_HighPrecisionDecimal_ShouldRecordCorrectly() {
        // Arrange
        String walletId = "wallet-precision";
        BigDecimal preciseBalance = new BigDecimal("123.456789");
        String currency = "BTC";

        // Act
        walletBalanceMetrics.updateBalance(walletId, preciseBalance, currency);

        // Assert
        Gauge gauge = meterRegistry.find(WALLET_BALANCE)
                .tag(TAG_WALLET_ID, walletId)
                .tag(TAG_CURRENCY, currency)
                .gauge();
        
        assertNotNull(gauge);
        assertEquals(preciseBalance.doubleValue(), gauge.value(), 0.000001);
    }

    @Test
    @DisplayName("Should handle multiple updates to same wallet-currency combination")
    void updateBalance_MultipleUpdates_ShouldAlwaysReflectLatestValue() {
        // Arrange
        String walletId = "wallet-updates";
        String currency = "CHF";
        BigDecimal[] balances = {
            new BigDecimal("100.00"),
            new BigDecimal("150.50"),
            new BigDecimal("75.25"),
            new BigDecimal("200.00")
        };

        // Act & Assert for each update
        for (BigDecimal balance : balances) {
            walletBalanceMetrics.updateBalance(walletId, balance, currency);
            
            Gauge gauge = meterRegistry.find(WALLET_BALANCE)
                    .tag(TAG_WALLET_ID, walletId)
                    .tag(TAG_CURRENCY, currency)
                    .gauge();
            
            assertNotNull(gauge);
            assertEquals(balance.doubleValue(), gauge.value(), 0.01);
        }
    }

    @Test
    @DisplayName("Should handle concurrent updates correctly")
    void updateBalance_ConcurrentUpdates_ShouldHandleCorrectly() throws InterruptedException {
        // Arrange
        String walletId = "wallet-concurrent";
        String currency = "CAD";
        BigDecimal finalBalance = new BigDecimal("500.00");
        int numberOfThreads = 10;
        Thread[] threads = new Thread[numberOfThreads];

        // Act - Create multiple threads updating the same wallet
        for (int i = 0; i < numberOfThreads; i++) {
            final BigDecimal balance = new BigDecimal(i * 10);
            threads[i] = new Thread(() -> {
                walletBalanceMetrics.updateBalance(walletId, balance, currency);
            });
            threads[i].start();
        }

        // Wait for all threads to complete
        for (Thread thread : threads) {
            thread.join();
        }

        // Final update to ensure we have a known final state
        walletBalanceMetrics.updateBalance(walletId, finalBalance, currency);

        // Assert
        Gauge gauge = meterRegistry.find(WALLET_BALANCE)
                .tag(TAG_WALLET_ID, walletId)
                .tag(TAG_CURRENCY, currency)
                .gauge();
        
        assertNotNull(gauge);
        assertEquals(finalBalance.doubleValue(), gauge.value(), 0.01);
    }

    @Test
    @DisplayName("Should handle special characters in wallet ID")
    void updateBalance_SpecialCharactersInWalletId_ShouldRecordCorrectly() {
        // Arrange
        String walletId = "wallet-123_special-chars.test";
        BigDecimal balance = new BigDecimal("300.00");
        String currency = "SEK";

        // Act
        walletBalanceMetrics.updateBalance(walletId, balance, currency);

        // Assert
        Gauge gauge = meterRegistry.find(WALLET_BALANCE)
                .tag(TAG_WALLET_ID, walletId)
                .tag(TAG_CURRENCY, currency)
                .gauge();
        
        assertNotNull(gauge);
        assertEquals(balance.doubleValue(), gauge.value(), 0.01);
    }

    @Test
    @DisplayName("Should create unique keys for wallet-currency combinations")
    void updateBalance_DifferentCombinations_ShouldCreateUniqueGauges() {
        // Arrange
        String wallet1 = "wallet-A";
        String wallet2 = "wallet-B";
        String currency1 = "USD";
        String currency2 = "EUR";
        BigDecimal balance = new BigDecimal("100.00");

        // Act - Create all possible combinations
        walletBalanceMetrics.updateBalance(wallet1, balance, currency1);
        walletBalanceMetrics.updateBalance(wallet1, balance, currency2);
        walletBalanceMetrics.updateBalance(wallet2, balance, currency1);
        walletBalanceMetrics.updateBalance(wallet2, balance, currency2);

        // Assert - All combinations should have separate gauges
        assertNotNull(meterRegistry.find(WALLET_BALANCE)
                .tag(TAG_WALLET_ID, wallet1)
                .tag(TAG_CURRENCY, currency1)
                .gauge());
        
        assertNotNull(meterRegistry.find(WALLET_BALANCE)
                .tag(TAG_WALLET_ID, wallet1)
                .tag(TAG_CURRENCY, currency2)
                .gauge());
        
        assertNotNull(meterRegistry.find(WALLET_BALANCE)
                .tag(TAG_WALLET_ID, wallet2)
                .tag(TAG_CURRENCY, currency1)
                .gauge());
        
        assertNotNull(meterRegistry.find(WALLET_BALANCE)
                .tag(TAG_WALLET_ID, wallet2)
                .tag(TAG_CURRENCY, currency2)
                .gauge());

        // Verify all gauges have the expected value
        assertEquals(4, meterRegistry.find(WALLET_BALANCE).gauges().size());
        meterRegistry.find(WALLET_BALANCE).gauges().forEach(gauge -> 
            assertEquals(balance.doubleValue(), gauge.value(), 0.01)
        );
    }
}

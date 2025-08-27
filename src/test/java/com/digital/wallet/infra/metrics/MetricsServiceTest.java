package com.digital.wallet.infra.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.concurrent.TimeUnit;

import static com.digital.wallet.infra.metrics.MetricsConstants.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("MetricsService Tests")
class MetricsServiceTest {

    @Mock
    private WalletGaugeRegistry walletGaugeRegistry;

    private MeterRegistry meterRegistry;
    private MetricsService metricsService;

    private final String defaultCurrency = "BRL";

    @BeforeEach
    void setUp() {
        // Use SimpleMeterRegistry for testing
        meterRegistry = new SimpleMeterRegistry();
        
        // Create MetricsService with real MeterRegistry and mocked WalletGaugeRegistry
        metricsService = new MetricsService(meterRegistry, defaultCurrency, walletGaugeRegistry);
    }

    @Test
    @DisplayName("Should initialize MetricsService with all counters and timers")
    void constructor_ShouldInitializeAllMetrics() {
        // Verify that all counters are created
        Counter depositCounter = meterRegistry.find(TRANSACTION_COUNT)
                .tag(TAG_OPERATION, OPERATION_DEPOSIT)
                .counter();
        assertNotNull(depositCounter);

        Counter withdrawalCounter = meterRegistry.find(TRANSACTION_COUNT)
                .tag(TAG_OPERATION, OPERATION_WITHDRAWAL)
                .counter();
        assertNotNull(withdrawalCounter);

        Counter transferCounter = meterRegistry.find(TRANSACTION_COUNT)
                .tag(TAG_OPERATION, OPERATION_TRANSFER)
                .counter();
        assertNotNull(transferCounter);

        Counter errorCounter = meterRegistry.find(TRANSACTION_ERRORS).counter();
        assertNotNull(errorCounter);

        // Verify that all timers are created
        Timer depositTimer = meterRegistry.find(TRANSACTION_DURATION)
                .tag(TAG_OPERATION, OPERATION_DEPOSIT)
                .timer();
        assertNotNull(depositTimer);

        Timer withdrawalTimer = meterRegistry.find(TRANSACTION_DURATION)
                .tag(TAG_OPERATION, OPERATION_WITHDRAWAL)
                .timer();
        assertNotNull(withdrawalTimer);

        Timer transferTimer = meterRegistry.find(TRANSACTION_DURATION)
                .tag(TAG_OPERATION, OPERATION_TRANSFER)
                .timer();
        assertNotNull(transferTimer);
    }

    @Test
    @DisplayName("Should record deposit transaction with default currency")
    void recordDepositTransaction_WithDefaultCurrency_ShouldIncrementCountersAndRecordTime() {
        // Arrange
        BigDecimal amount = new BigDecimal("100.50");
        long durationMs = 250L;

        // Act
        metricsService.recordDepositTransaction(amount, durationMs);

        // Assert
        Counter depositCounter = meterRegistry.find(TRANSACTION_COUNT)
                .tag(TAG_OPERATION, OPERATION_DEPOSIT)
                .counter();
        assertEquals(1.0, depositCounter.count());

        Timer depositTimer = meterRegistry.find(TRANSACTION_DURATION)
                .tag(TAG_OPERATION, OPERATION_DEPOSIT)
                .timer();
        assertEquals(1, depositTimer.count());
        assertEquals(durationMs, depositTimer.totalTime(TimeUnit.MILLISECONDS), 0.1);

        Counter amountCounter = meterRegistry.find(TRANSACTION_AMOUNT)
                .tag(TAG_OPERATION, OPERATION_DEPOSIT)
                .tag(TAG_CURRENCY, defaultCurrency)
                .counter();
        assertEquals(amount.doubleValue(), amountCounter.count());
    }

    @Test
    @DisplayName("Should record deposit transaction with specified currency")
    void recordDepositTransaction_WithSpecifiedCurrency_ShouldIncrementCountersAndRecordTime() {
        // Arrange
        BigDecimal amount = new BigDecimal("75.25");
        long durationMs = 180L;
        String currency = "USD";

        // Act
        metricsService.recordDepositTransaction(amount, durationMs, currency);

        // Assert
        Counter depositCounter = meterRegistry.find(TRANSACTION_COUNT)
                .tag(TAG_OPERATION, OPERATION_DEPOSIT)
                .counter();
        assertEquals(1.0, depositCounter.count());

        Timer depositTimer = meterRegistry.find(TRANSACTION_DURATION)
                .tag(TAG_OPERATION, OPERATION_DEPOSIT)
                .timer();
        assertEquals(1, depositTimer.count());
        assertEquals(durationMs, depositTimer.totalTime(TimeUnit.MILLISECONDS), 0.1);

        Counter amountCounter = meterRegistry.find(TRANSACTION_AMOUNT)
                .tag(TAG_OPERATION, OPERATION_DEPOSIT)
                .tag(TAG_CURRENCY, currency)
                .counter();
        assertEquals(amount.doubleValue(), amountCounter.count());
    }

    @Test
    @DisplayName("Should record withdrawal transaction with default currency")
    void recordWithdrawalTransaction_WithDefaultCurrency_ShouldIncrementCountersAndRecordTime() {
        // Arrange
        BigDecimal amount = new BigDecimal("50.75");
        long durationMs = 300L;

        // Act
        metricsService.recordWithdrawalTransaction(amount, durationMs);

        // Assert
        Counter withdrawalCounter = meterRegistry.find(TRANSACTION_COUNT)
                .tag(TAG_OPERATION, OPERATION_WITHDRAWAL)
                .counter();
        assertEquals(1.0, withdrawalCounter.count());

        Timer withdrawalTimer = meterRegistry.find(TRANSACTION_DURATION)
                .tag(TAG_OPERATION, OPERATION_WITHDRAWAL)
                .timer();
        assertEquals(1, withdrawalTimer.count());
        assertEquals(durationMs, withdrawalTimer.totalTime(TimeUnit.MILLISECONDS), 0.1);

        Counter amountCounter = meterRegistry.find(TRANSACTION_AMOUNT)
                .tag(TAG_OPERATION, OPERATION_WITHDRAWAL)
                .tag(TAG_CURRENCY, defaultCurrency)
                .counter();
        assertEquals(amount.doubleValue(), amountCounter.count());
    }

    @Test
    @DisplayName("Should record withdrawal transaction with specified currency")
    void recordWithdrawalTransaction_WithSpecifiedCurrency_ShouldIncrementCountersAndRecordTime() {
        // Arrange
        BigDecimal amount = new BigDecimal("25.00");
        long durationMs = 150L;
        String currency = "EUR";

        // Act
        metricsService.recordWithdrawalTransaction(amount, durationMs, currency);

        // Assert
        Counter withdrawalCounter = meterRegistry.find(TRANSACTION_COUNT)
                .tag(TAG_OPERATION, OPERATION_WITHDRAWAL)
                .counter();
        assertEquals(1.0, withdrawalCounter.count());

        Timer withdrawalTimer = meterRegistry.find(TRANSACTION_DURATION)
                .tag(TAG_OPERATION, OPERATION_WITHDRAWAL)
                .timer();
        assertEquals(1, withdrawalTimer.count());
        assertEquals(durationMs, withdrawalTimer.totalTime(TimeUnit.MILLISECONDS), 0.1);

        Counter amountCounter = meterRegistry.find(TRANSACTION_AMOUNT)
                .tag(TAG_OPERATION, OPERATION_WITHDRAWAL)
                .tag(TAG_CURRENCY, currency)
                .counter();
        assertEquals(amount.doubleValue(), amountCounter.count());
    }

    @Test
    @DisplayName("Should record transfer transaction with default currency")
    void recordTransferTransaction_WithDefaultCurrency_ShouldIncrementCountersAndRecordTime() {
        // Arrange
        BigDecimal amount = new BigDecimal("200.00");
        long durationMs = 400L;

        // Act
        metricsService.recordTransferTransaction(amount, durationMs);

        // Assert
        Counter transferCounter = meterRegistry.find(TRANSACTION_COUNT)
                .tag(TAG_OPERATION, OPERATION_TRANSFER)
                .counter();
        assertEquals(1.0, transferCounter.count());

        Timer transferTimer = meterRegistry.find(TRANSACTION_DURATION)
                .tag(TAG_OPERATION, OPERATION_TRANSFER)
                .timer();
        assertEquals(1, transferTimer.count());
        assertEquals(durationMs, transferTimer.totalTime(TimeUnit.MILLISECONDS), 0.1);

        Counter amountCounter = meterRegistry.find(TRANSACTION_AMOUNT)
                .tag(TAG_OPERATION, OPERATION_TRANSFER)
                .tag(TAG_CURRENCY, defaultCurrency)
                .counter();
        assertEquals(amount.doubleValue(), amountCounter.count());
    }

    @Test
    @DisplayName("Should record transfer transaction with specified currency")
    void recordTransferTransaction_WithSpecifiedCurrency_ShouldIncrementCountersAndRecordTime() {
        // Arrange
        BigDecimal amount = new BigDecimal("150.50");
        long durationMs = 350L;
        String currency = "GBP";

        // Act
        metricsService.recordTransferTransaction(amount, durationMs, currency);

        // Assert
        Counter transferCounter = meterRegistry.find(TRANSACTION_COUNT)
                .tag(TAG_OPERATION, OPERATION_TRANSFER)
                .counter();
        assertEquals(1.0, transferCounter.count());

        Timer transferTimer = meterRegistry.find(TRANSACTION_DURATION)
                .tag(TAG_OPERATION, OPERATION_TRANSFER)
                .timer();
        assertEquals(1, transferTimer.count());
        assertEquals(durationMs, transferTimer.totalTime(TimeUnit.MILLISECONDS), 0.1);

        Counter amountCounter = meterRegistry.find(TRANSACTION_AMOUNT)
                .tag(TAG_OPERATION, OPERATION_TRANSFER)
                .tag(TAG_CURRENCY, currency)
                .counter();
        assertEquals(amount.doubleValue(), amountCounter.count());
    }

    @Test
    @DisplayName("Should record transaction error")
    void recordTransactionError_ShouldIncrementErrorCounter() {
        // Arrange
        String operation = OPERATION_DEPOSIT;
        String errorType = "INSUFFICIENT_FUNDS";

        // Act
        metricsService.recordTransactionError(operation, errorType);

        // Assert
        Counter errorCounter = meterRegistry.find(TRANSACTION_ERRORS)
                .tag(TAG_OPERATION, operation)
                .tag(TAG_ERROR, errorType)
                .counter();
        assertEquals(1.0, errorCounter.count());
    }

    @Test
    @DisplayName("Should record wallet balance with default currency")
    void recordWalletBalance_WithDefaultCurrency_ShouldCallWalletGaugeRegistry() {
        // Arrange
        String walletId = "wallet-123";
        BigDecimal balance = new BigDecimal("500.00");

        // Act
        metricsService.recordWalletBalance(walletId, balance);

        // Assert
        verify(walletGaugeRegistry).updateWalletBalance(walletId, balance, defaultCurrency);
    }

    @Test
    @DisplayName("Should record wallet balance with specified currency")
    void recordWalletBalance_WithSpecifiedCurrency_ShouldCallWalletGaugeRegistry() {
        // Arrange
        String walletId = "wallet-456";
        BigDecimal balance = new BigDecimal("750.25");
        String currency = "USD";

        // Act
        metricsService.recordWalletBalance(walletId, balance, currency);

        // Assert
        verify(walletGaugeRegistry).updateWalletBalance(walletId, balance, currency);
    }

    @Test
    @DisplayName("Should handle multiple transactions correctly")
    void multipleTransactions_ShouldAccumulateMetrics() {
        // Arrange & Act
        metricsService.recordDepositTransaction(new BigDecimal("100.00"), 200L);
        metricsService.recordDepositTransaction(new BigDecimal("50.00"), 150L);
        metricsService.recordWithdrawalTransaction(new BigDecimal("25.00"), 100L);

        // Assert
        Counter depositCounter = meterRegistry.find(TRANSACTION_COUNT)
                .tag(TAG_OPERATION, OPERATION_DEPOSIT)
                .counter();
        assertEquals(2.0, depositCounter.count());

        Counter withdrawalCounter = meterRegistry.find(TRANSACTION_COUNT)
                .tag(TAG_OPERATION, OPERATION_WITHDRAWAL)
                .counter();
        assertEquals(1.0, withdrawalCounter.count());

        Timer depositTimer = meterRegistry.find(TRANSACTION_DURATION)
                .tag(TAG_OPERATION, OPERATION_DEPOSIT)
                .timer();
        assertEquals(2, depositTimer.count());
        assertEquals(350.0, depositTimer.totalTime(TimeUnit.MILLISECONDS), 0.1);

        Counter depositAmountCounter = meterRegistry.find(TRANSACTION_AMOUNT)
                .tag(TAG_OPERATION, OPERATION_DEPOSIT)
                .tag(TAG_CURRENCY, defaultCurrency)
                .counter();
        assertEquals(150.0, depositAmountCounter.count());
    }

    @Test
    @DisplayName("Should handle zero amounts correctly")
    void recordTransaction_WithZeroAmount_ShouldRecordCorrectly() {
        // Arrange
        BigDecimal zeroAmount = BigDecimal.ZERO;
        long durationMs = 100L;

        // Act
        metricsService.recordDepositTransaction(zeroAmount, durationMs);

        // Assert
        Counter depositCounter = meterRegistry.find(TRANSACTION_COUNT)
                .tag(TAG_OPERATION, OPERATION_DEPOSIT)
                .counter();
        assertEquals(1.0, depositCounter.count());

        Counter amountCounter = meterRegistry.find(TRANSACTION_AMOUNT)
                .tag(TAG_OPERATION, OPERATION_DEPOSIT)
                .tag(TAG_CURRENCY, defaultCurrency)
                .counter();
        assertEquals(0.0, amountCounter.count());
    }

    @Test
    @DisplayName("Should handle large amounts correctly")
    void recordTransaction_WithLargeAmount_ShouldRecordCorrectly() {
        // Arrange
        BigDecimal largeAmount = new BigDecimal("999999.99");
        long durationMs = 500L;

        // Act
        metricsService.recordTransferTransaction(largeAmount, durationMs);

        // Assert
        Counter transferCounter = meterRegistry.find(TRANSACTION_COUNT)
                .tag(TAG_OPERATION, OPERATION_TRANSFER)
                .counter();
        assertEquals(1.0, transferCounter.count());

        Counter amountCounter = meterRegistry.find(TRANSACTION_AMOUNT)
                .tag(TAG_OPERATION, OPERATION_TRANSFER)
                .tag(TAG_CURRENCY, defaultCurrency)
                .counter();
        assertEquals(largeAmount.doubleValue(), amountCounter.count());
    }

    @Test
    @DisplayName("Should handle different error types correctly")
    void recordTransactionError_WithDifferentErrorTypes_ShouldCreateSeparateCounters() {
        // Arrange & Act
        metricsService.recordTransactionError(OPERATION_DEPOSIT, "INSUFFICIENT_FUNDS");
        metricsService.recordTransactionError(OPERATION_DEPOSIT, "VALIDATION_ERROR");
        metricsService.recordTransactionError(OPERATION_WITHDRAWAL, "INSUFFICIENT_FUNDS");

        // Assert
        Counter depositInsufficientFundsCounter = meterRegistry.find(TRANSACTION_ERRORS)
                .tag(TAG_OPERATION, OPERATION_DEPOSIT)
                .tag(TAG_ERROR, "INSUFFICIENT_FUNDS")
                .counter();
        assertEquals(1.0, depositInsufficientFundsCounter.count());

        Counter depositValidationErrorCounter = meterRegistry.find(TRANSACTION_ERRORS)
                .tag(TAG_OPERATION, OPERATION_DEPOSIT)
                .tag(TAG_ERROR, "VALIDATION_ERROR")
                .counter();
        assertEquals(1.0, depositValidationErrorCounter.count());

        Counter withdrawalInsufficientFundsCounter = meterRegistry.find(TRANSACTION_ERRORS)
                .tag(TAG_OPERATION, OPERATION_WITHDRAWAL)
                .tag(TAG_ERROR, "INSUFFICIENT_FUNDS")
                .counter();
        assertEquals(1.0, withdrawalInsufficientFundsCounter.count());
    }

    @Test
    @DisplayName("Should handle empty currency gracefully")
    void recordTransaction_WithEmptyCurrency_ShouldRecordCorrectly() {
        // Arrange
        BigDecimal amount = new BigDecimal("100.00");
        long durationMs = 200L;
        String emptyCurrency = "";

        // Act
        metricsService.recordDepositTransaction(amount, durationMs, emptyCurrency);

        // Assert
        Counter amountCounter = meterRegistry.find(TRANSACTION_AMOUNT)
                .tag(TAG_OPERATION, OPERATION_DEPOSIT)
                .tag(TAG_CURRENCY, emptyCurrency)
                .counter();
        assertEquals(amount.doubleValue(), amountCounter.count());
    }
}

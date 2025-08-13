package com.recargapay.wallet.infra.metrics;

import com.recargapay.wallet.core.domain.Transaction;
import org.aspectj.lang.ProceedingJoinPoint;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.UUID;

import static com.recargapay.wallet.core.domain.TransactionType.*;
import static com.recargapay.wallet.infra.metrics.MetricsConstants.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MetricsAspectTest {

    @Mock
    private MetricsService metricsService;

    @Mock
    private ProceedingJoinPoint joinPoint;

    @InjectMocks
    private MetricsAspect metricsAspect;

    private final BigDecimal testAmount = new BigDecimal("100.50");
    private final String testCurrency = "BRL";
    private final String testResult = "operation-result";

    @BeforeEach
    void setUp() {
        // Reset all mocks before each test
        reset(metricsService, joinPoint);
    }

    @Test
    void recordDepositMetrics_WithAmountAndCurrency_ShouldRecordMetricsWithCurrency() throws Throwable {
        // Given
        Object[] args = {testAmount, testCurrency};
        when(joinPoint.getArgs()).thenReturn(args);
        when(joinPoint.proceed()).thenReturn(testResult);

        // When
        Object result = metricsAspect.recordDepositMetrics(joinPoint);

        // Then
        assertEquals(testResult, result);
        verify(joinPoint).proceed();
        verify(metricsService).recordDepositTransaction(eq(testAmount), anyLong(), eq(testCurrency));
        verifyNoMoreInteractions(metricsService);
    }

    @Test
    void recordDepositMetrics_WithAmountOnly_ShouldRecordMetricsWithoutCurrency() throws Throwable {
        // Given
        Object[] args = {testAmount, "not-a-currency"};
        when(joinPoint.getArgs()).thenReturn(args);
        when(joinPoint.proceed()).thenReturn(testResult);

        // When
        Object result = metricsAspect.recordDepositMetrics(joinPoint);

        // Then
        assertEquals(testResult, result);
        verify(joinPoint).proceed();
        verify(metricsService).recordDepositTransaction(eq(testAmount), anyLong());
        verifyNoMoreInteractions(metricsService);
    }

    @Test
    void recordDepositMetrics_WithException_ShouldRecordErrorAndRethrow() throws Throwable {
        // Given
        Object[] args = {testAmount};
        RuntimeException testException = new RuntimeException("Test error");
        when(joinPoint.getArgs()).thenReturn(args);
        when(joinPoint.proceed()).thenThrow(testException);

        // When & Then
        RuntimeException thrown = assertThrows(RuntimeException.class, 
            () -> metricsAspect.recordDepositMetrics(joinPoint));

        assertEquals(testException, thrown);
        verify(joinPoint).proceed();
        verify(metricsService).recordTransactionError(OPERATION_DEPOSIT, "RuntimeException");
        verifyNoMoreInteractions(metricsService);
    }

    @Test
    void recordWithdrawalMetrics_WithAmountAndCurrency_ShouldRecordMetricsWithCurrency() throws Throwable {
        // Given
        Object[] args = {testAmount, testCurrency};
        when(joinPoint.getArgs()).thenReturn(args);
        when(joinPoint.proceed()).thenReturn(testResult);

        // When
        Object result = metricsAspect.recordWithdrawalMetrics(joinPoint);

        // Then
        assertEquals(testResult, result);
        verify(joinPoint).proceed();
        verify(metricsService).recordWithdrawalTransaction(eq(testAmount), anyLong(), eq(testCurrency));
        verifyNoMoreInteractions(metricsService);
    }

    @Test
    void recordWithdrawalMetrics_WithAmountOnly_ShouldRecordMetricsWithoutCurrency() throws Throwable {
        // Given
        Object[] args = {testAmount};
        when(joinPoint.getArgs()).thenReturn(args);
        when(joinPoint.proceed()).thenReturn(testResult);

        // When
        Object result = metricsAspect.recordWithdrawalMetrics(joinPoint);

        // Then
        assertEquals(testResult, result);
        verify(joinPoint).proceed();
        verify(metricsService).recordWithdrawalTransaction(eq(testAmount), anyLong());
        verifyNoMoreInteractions(metricsService);
    }

    @Test
    void recordWithdrawalMetrics_WithException_ShouldRecordErrorAndRethrow() throws Throwable {
        // Given
        Object[] args = {testAmount};
        IllegalArgumentException testException = new IllegalArgumentException("Invalid amount");
        when(joinPoint.getArgs()).thenReturn(args);
        when(joinPoint.proceed()).thenThrow(testException);

        // When & Then
        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, 
            () -> metricsAspect.recordWithdrawalMetrics(joinPoint));

        assertEquals(testException, thrown);
        verify(joinPoint).proceed();
        verify(metricsService).recordTransactionError(OPERATION_WITHDRAWAL, "IllegalArgumentException");
        verifyNoMoreInteractions(metricsService);
    }

    @Test
    void recordTransferMetrics_WithAmountAndCurrency_ShouldRecordMetricsWithCurrency() throws Throwable {
        // Given
        Object[] args = {testAmount, testCurrency, "extra-param"};
        when(joinPoint.getArgs()).thenReturn(args);
        when(joinPoint.proceed()).thenReturn(testResult);

        // When
        Object result = metricsAspect.recordTransferMetrics(joinPoint);

        // Then
        assertEquals(testResult, result);
        verify(joinPoint).proceed();
        verify(metricsService).recordTransferTransaction(eq(testAmount), anyLong(), eq(testCurrency));
        verifyNoMoreInteractions(metricsService);
    }

    @Test
    void recordTransferMetrics_WithAmountOnly_ShouldRecordMetricsWithoutCurrency() throws Throwable {
        // Given
        Object[] args = {testAmount, "wallet-id", 123};
        when(joinPoint.getArgs()).thenReturn(args);
        when(joinPoint.proceed()).thenReturn(testResult);

        // When
        Object result = metricsAspect.recordTransferMetrics(joinPoint);

        // Then
        assertEquals(testResult, result);
        verify(joinPoint).proceed();
        verify(metricsService).recordTransferTransaction(eq(testAmount), anyLong());
        verifyNoMoreInteractions(metricsService);
    }

    @Test
    void recordTransferMetrics_WithException_ShouldRecordErrorAndRethrow() throws Throwable {
        // Given
        Object[] args = {testAmount};
        IllegalStateException testException = new IllegalStateException("Transfer failed");
        when(joinPoint.getArgs()).thenReturn(args);
        when(joinPoint.proceed()).thenThrow(testException);

        // When & Then
        IllegalStateException thrown = assertThrows(IllegalStateException.class, 
            () -> metricsAspect.recordTransferMetrics(joinPoint));

        assertEquals(testException, thrown);
        verify(joinPoint).proceed();
        verify(metricsService).recordTransactionError(OPERATION_TRANSFER, "IllegalStateException");
        verifyNoMoreInteractions(metricsService);
    }

    @Test
    void extractAmount_WithBigDecimalArgument_ShouldReturnAmount() throws Throwable {
        // Given
        BigDecimal expectedAmount = new BigDecimal("250.75");
        Object[] args = {"wallet-id", expectedAmount, "USD"};
        when(joinPoint.getArgs()).thenReturn(args);
        when(joinPoint.proceed()).thenReturn(testResult);

        // When
        metricsAspect.recordDepositMetrics(joinPoint);

        // Then
        verify(metricsService).recordDepositTransaction(eq(expectedAmount), anyLong(), eq("USD"));
    }

    @Test
    void extractAmount_WithNoBigDecimalArgument_ShouldReturnZero() throws Throwable {
        // Given
        Object[] args = {"wallet-id", "not-a-number", 123};
        when(joinPoint.getArgs()).thenReturn(args);
        when(joinPoint.proceed()).thenReturn(testResult);

        // When
        metricsAspect.recordDepositMetrics(joinPoint);

        // Then
        verify(metricsService).recordDepositTransaction(eq(BigDecimal.ZERO), anyLong());
    }

    @Test
    void extractAmount_WithNullArgs_ShouldReturnZero() throws Throwable {
        // Given
        when(joinPoint.getArgs()).thenReturn(null);
        when(joinPoint.proceed()).thenReturn(testResult);

        // When
        metricsAspect.recordDepositMetrics(joinPoint);

        // Then
        verify(metricsService).recordDepositTransaction(eq(BigDecimal.ZERO), anyLong());
    }

    @Test
    void extractAmount_WithEmptyArgs_ShouldReturnZero() throws Throwable {
        // Given
        Object[] args = {};
        when(joinPoint.getArgs()).thenReturn(args);
        when(joinPoint.proceed()).thenReturn(testResult);

        // When
        metricsAspect.recordDepositMetrics(joinPoint);

        // Then
        verify(metricsService).recordDepositTransaction(eq(BigDecimal.ZERO), anyLong());
    }

    @Test
    void extractCurrency_WithValidCurrencyCode_ShouldReturnCurrency() throws Throwable {
        // Given
        Object[] args = {testAmount, "EUR", "wallet-id"};
        when(joinPoint.getArgs()).thenReturn(args);
        when(joinPoint.proceed()).thenReturn(testResult);

        // When
        metricsAspect.recordDepositMetrics(joinPoint);

        // Then
        verify(metricsService).recordDepositTransaction(eq(testAmount), anyLong(), eq("EUR"));
    }

    @Test
    void extractCurrency_WithInvalidCurrencyCode_ShouldReturnNull() throws Throwable {
        // Given - lowercase currency code should be ignored
        Object[] args = {testAmount, "usd", "wallet-id"};
        when(joinPoint.getArgs()).thenReturn(args);
        when(joinPoint.proceed()).thenReturn(testResult);

        // When
        metricsAspect.recordDepositMetrics(joinPoint);

        // Then
        verify(metricsService).recordDepositTransaction(eq(testAmount), anyLong());
    }

    @Test
    void extractCurrency_WithLongString_ShouldReturnNull() throws Throwable {
        // Given - string longer than 3 characters should be ignored
        Object[] args = {testAmount, "DOLLAR", "wallet-id"};
        when(joinPoint.getArgs()).thenReturn(args);
        when(joinPoint.proceed()).thenReturn(testResult);

        // When
        metricsAspect.recordDepositMetrics(joinPoint);

        // Then
        verify(metricsService).recordDepositTransaction(eq(testAmount), anyLong());
    }

    @Test
    void extractCurrency_WithShortString_ShouldReturnNull() throws Throwable {
        // Given - string shorter than 3 characters should be ignored
        Object[] args = {testAmount, "US", "wallet-id"};
        when(joinPoint.getArgs()).thenReturn(args);
        when(joinPoint.proceed()).thenReturn(testResult);

        // When
        metricsAspect.recordDepositMetrics(joinPoint);

        // Then
        verify(metricsService).recordDepositTransaction(eq(testAmount), anyLong());
    }

    @Test
    void extractCurrency_WithNullArgs_ShouldReturnNull() throws Throwable {
        // Given
        when(joinPoint.getArgs()).thenReturn(null);
        when(joinPoint.proceed()).thenReturn(testResult);

        // When
        metricsAspect.recordDepositMetrics(joinPoint);

        // Then
        verify(metricsService).recordDepositTransaction(eq(BigDecimal.ZERO), anyLong());
    }

    @Test
    void extractCurrency_WithEmptyArgs_ShouldReturnNull() throws Throwable {
        // Given
        Object[] args = {};
        when(joinPoint.getArgs()).thenReturn(args);
        when(joinPoint.proceed()).thenReturn(testResult);

        // When
        metricsAspect.recordDepositMetrics(joinPoint);

        // Then
        verify(metricsService).recordDepositTransaction(eq(BigDecimal.ZERO), anyLong());
    }

    @Test
    void extractCurrency_WithMultipleCurrencyCodes_ShouldReturnFirst() throws Throwable {
        // Given - should return the first valid currency code found
        Object[] args = {testAmount, "GBP", "JPY", "wallet-id"};
        when(joinPoint.getArgs()).thenReturn(args);
        when(joinPoint.proceed()).thenReturn(testResult);

        // When
        metricsAspect.recordDepositMetrics(joinPoint);

        // Then
        verify(metricsService).recordDepositTransaction(eq(testAmount), anyLong(), eq("GBP"));
    }

    @Test
    void recordTransactionMetrics_ShouldMeasureExecutionTime() throws Throwable {
        // Given
        Object[] args = {testAmount};
        when(joinPoint.getArgs()).thenReturn(args);
        when(joinPoint.proceed()).thenAnswer(invocation -> {
            // Simulate some processing time
            Thread.sleep(10);
            return testResult;
        });

        // When
        long startTime = System.currentTimeMillis();
        metricsAspect.recordDepositMetrics(joinPoint);
        long endTime = System.currentTimeMillis();

        // Then
        verify(metricsService).recordDepositTransaction(eq(testAmount), longThat(duration -> 
            duration >= 0 && duration <= (endTime - startTime + 100) // Allow some tolerance
        ));
    }

    @Test
    void recordTransactionMetrics_WithMixedArguments_ShouldExtractCorrectly() throws Throwable {
        // Given - mixed argument types
        Object[] args = {
            "wallet-id-123", 
            new BigDecimal("999.99"), 
            42, 
            "USD", 
            true, 
            "another-string"
        };
        when(joinPoint.getArgs()).thenReturn(args);
        when(joinPoint.proceed()).thenReturn(testResult);

        // When
        metricsAspect.recordTransferMetrics(joinPoint);

        // Then
        verify(metricsService).recordTransferTransaction(
            eq(new BigDecimal("999.99")), 
            anyLong(), 
            eq("USD")
        );
    }

    @Test
    void allOperationMethods_ShouldHandleNullResult() throws Throwable {
        // Given
        Object[] args = {testAmount, testCurrency};
        when(joinPoint.getArgs()).thenReturn(args);
        when(joinPoint.proceed()).thenReturn(null);

        // When & Then
        assertNull(metricsAspect.recordDepositMetrics(joinPoint));
        assertNull(metricsAspect.recordWithdrawalMetrics(joinPoint));
        assertNull(metricsAspect.recordTransferMetrics(joinPoint));

        // Verify metrics were still recorded
        verify(metricsService).recordDepositTransaction(eq(testAmount), anyLong(), eq(testCurrency));
        verify(metricsService).recordWithdrawalTransaction(eq(testAmount), anyLong(), eq(testCurrency));
        verify(metricsService).recordTransferTransaction(eq(testAmount), anyLong(), eq(testCurrency));
    }

    @Test
    @DisplayName("Should use default currency methods when currency extraction returns null")
    void shouldUseDefaultCurrencyMethodsWhenCurrencyExtractionReturnsNull() throws Throwable {
        // Given
        ProceedingJoinPoint joinPoint = mock(ProceedingJoinPoint.class);
        Object[] args = {BigDecimal.valueOf(100.0), "invalid_currency_format"}; // Not a 3-char uppercase currency
        when(joinPoint.getArgs()).thenReturn(args);
        when(joinPoint.proceed()).thenReturn("success");
        
        // When
        Object result = metricsAspect.recordDepositMetrics(joinPoint);
        
        // Then
        assertEquals("success", result);
        verify(metricsService).recordDepositTransaction(eq(BigDecimal.valueOf(100.0)), anyLong());
        verify(metricsService, never()).recordDepositTransaction(any(BigDecimal.class), anyLong(), anyString());
    }

    @Test
    @DisplayName("Should handle empty args array in extractAmount method")
    void shouldHandleEmptyArgsArrayInExtractAmountMethod() throws Throwable {
        // Given
        ProceedingJoinPoint joinPoint = mock(ProceedingJoinPoint.class);
        Object[] emptyArgs = {};
        when(joinPoint.getArgs()).thenReturn(emptyArgs);
        when(joinPoint.proceed()).thenReturn("success");
        
        // When
        Object result = metricsAspect.recordDepositMetrics(joinPoint);
        
        // Then
        assertEquals("success", result);
        verify(metricsService).recordDepositTransaction(eq(BigDecimal.ZERO), anyLong());
    }

    @Test
    @DisplayName("Should handle null args array in extractAmount method")
    void shouldHandleNullArgsArrayInExtractAmountMethod() throws Throwable {
        // Given
        ProceedingJoinPoint joinPoint = mock(ProceedingJoinPoint.class);
        when(joinPoint.getArgs()).thenReturn(null);
        when(joinPoint.proceed()).thenReturn("success");
        
        // When
        Object result = metricsAspect.recordDepositMetrics(joinPoint);
        
        // Then
        assertEquals("success", result);
        verify(metricsService).recordDepositTransaction(eq(BigDecimal.ZERO), anyLong());
    }
}

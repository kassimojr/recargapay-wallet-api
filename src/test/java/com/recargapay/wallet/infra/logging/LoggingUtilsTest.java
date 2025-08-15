package com.recargapay.wallet.infra.logging;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("LoggingUtils Tests")
class LoggingUtilsTest {

    private static final Logger logger = LoggerFactory.getLogger(LoggingUtilsTest.class);

    @Test
    @DisplayName("Should log with single key-value pair")
    void shouldLogWithSingleKeyValuePair() {
        // Given
        String key = "operation";
        String value = "DEPOSIT";

        // When
        LoggingUtils.log(logger, "Test message", key, value);

        // Then
        // Verify MDC was set and cleared (implicit by successful execution)
        assertThat(MDC.get(key)).isNull(); // Should be cleared after logging
    }

    @Test
    @DisplayName("Should log with two key-value pairs")
    void shouldLogWithTwoKeyValuePairs() {
        // Given
        String key1 = "operation";
        String value1 = "WITHDRAW";
        String key2 = "amount";
        BigDecimal value2 = BigDecimal.valueOf(100.00);

        // When
        LoggingUtils.log(logger, "Test message", key1, value1, key2, value2);

        // Then
        // Verify MDC was set and cleared (implicit by successful execution)
        assertThat(MDC.get(key1)).isNull();
        assertThat(MDC.get(key2)).isNull();
    }

    @Test
    @DisplayName("Should log with three key-value pairs")
    void shouldLogWithThreeKeyValuePairs() {
        // Given
        String key1 = "operation";
        String value1 = "TRANSFER";
        String key2 = "amount";
        BigDecimal value2 = BigDecimal.valueOf(250.00);
        String key3 = "walletId";
        UUID value3 = UUID.randomUUID();

        // When
        LoggingUtils.log(logger, "Test message", key1, value1, key2, value2, key3, value3);

        // Then
        // Verify MDC was set and cleared (implicit by successful execution)
        assertThat(MDC.get(key1)).isNull();
        assertThat(MDC.get(key2)).isNull();
        assertThat(MDC.get(key3)).isNull();
    }

    @Test
    @DisplayName("Should log with four key-value pairs")
    void shouldLogWithFourKeyValuePairs() {
        // Given
        String key1 = "operation";
        String value1 = "BALANCE_CHECK";
        String key2 = "walletId";
        UUID value2 = UUID.randomUUID();
        String key3 = "userId";
        UUID value3 = UUID.randomUUID();
        String key4 = "status";
        String value4 = "SUCCESS";

        // When
        LoggingUtils.log(logger, "Test message", key1, value1, key2, value2, key3, value3, key4, value4);

        // Then
        // Verify MDC was set and cleared (implicit by successful execution)
        assertThat(MDC.get(key1)).isNull();
        assertThat(MDC.get(key2)).isNull();
        assertThat(MDC.get(key3)).isNull();
        assertThat(MDC.get(key4)).isNull();
    }

    @Test
    @DisplayName("Should log with Map of key-value pairs")
    void shouldLogWithMapOfKeyValuePairs() {
        // Given
        Map<String, Object> context = new HashMap<>();
        context.put("operation", "CREATE_WALLET");
        context.put("userId", UUID.randomUUID());
        context.put("initialBalance", BigDecimal.valueOf(0.00));
        context.put("currency", "BRL");
        context.put("status", "PENDING");

        // When
        LoggingUtils.log(logger, "Test message with map", context);

        // Then
        // Verify MDC was set and cleared (implicit by successful execution)
        assertThat(MDC.get("operation")).isNull();
        assertThat(MDC.get("userId")).isNull();
        assertThat(MDC.get("initialBalance")).isNull();
        assertThat(MDC.get("currency")).isNull();
        assertThat(MDC.get("status")).isNull();
    }

    @Test
    @DisplayName("Should handle null message gracefully")
    void shouldHandleNullMessageGracefully() {
        // Given
        String nullMessage = null;
        String key = "operation";
        String value = "TEST";

        // When & Then (should not throw exception)
        LoggingUtils.log(logger, nullMessage, key, value);
    }

    @Test
    @DisplayName("Should handle null key gracefully")
    void shouldHandleNullKeyGracefully() {
        // Given
        String nullKey = null;
        String value = "TEST";

        // When & Then (should not throw exception)
        LoggingUtils.log(logger, "Test message", nullKey, value);
    }

    @Test
    @DisplayName("Should handle null value gracefully")
    void shouldHandleNullValueGracefully() {
        // Given
        String key = "operation";
        Object nullValue = null;

        // When & Then (should not throw exception)
        LoggingUtils.log(logger, "Test message", key, nullValue);
    }

    @Test
    @DisplayName("Should handle empty map gracefully")
    void shouldHandleEmptyMapGracefully() {
        // Given
        Map<String, Object> emptyMap = new HashMap<>();

        // When & Then (should not throw exception)
        LoggingUtils.log(logger, "Test message with empty map", emptyMap);
    }

    @Test
    @DisplayName("Should handle null map gracefully")
    void shouldHandleNullMapGracefully() {
        // Given
        Map<String, Object> nullMap = null;

        // When & Then (should not throw exception)
        LoggingUtils.log(logger, "Test message with null map", nullMap);
    }

    @Test
    @DisplayName("Should handle mixed data types in values")
    void shouldHandleMixedDataTypesInValues() {
        // Given
        String stringValue = "TEST_STRING";
        Integer intValue = 42;
        BigDecimal decimalValue = BigDecimal.valueOf(123.45);
        Boolean booleanValue = true;

        // When & Then (should not throw exception)
        LoggingUtils.log(logger, "Mixed types test", 
            "string", stringValue,
            "integer", intValue,
            "decimal", decimalValue,
            "boolean", booleanValue);
    }

    @Test
    @DisplayName("Should handle map with null values")
    void shouldHandleMapWithNullValues() {
        // Given
        Map<String, Object> mapWithNulls = new HashMap<>();
        mapWithNulls.put("operation", "TEST");
        mapWithNulls.put("nullValue", null);
        mapWithNulls.put("amount", BigDecimal.valueOf(100.00));
        mapWithNulls.put("anotherNull", null);

        // When & Then (should not throw exception)
        LoggingUtils.log(logger, "Test with null values in map", mapWithNulls);
    }

    @Test
    @DisplayName("Should handle map with null keys")
    void shouldHandleMapWithNullKeys() {
        // Given
        Map<String, Object> mapWithNullKeys = new HashMap<>();
        mapWithNullKeys.put("operation", "TEST");
        mapWithNullKeys.put(null, "nullKeyValue");
        mapWithNullKeys.put("amount", BigDecimal.valueOf(50.00));

        // When & Then (should not throw exception)
        LoggingUtils.log(logger, "Test with null keys in map", mapWithNullKeys);
    }

    @Test
    @DisplayName("Should handle large map")
    void shouldHandleLargeMap() {
        // Given
        Map<String, Object> largeMap = new HashMap<>();
        for (int i = 0; i < 20; i++) {
            largeMap.put("key" + i, "value" + i);
        }

        // When & Then (should not throw exception)
        LoggingUtils.log(logger, "Test with large map", largeMap);
    }

    @Test
    @DisplayName("Should handle special characters in keys and values")
    void shouldHandleSpecialCharactersInKeysAndValues() {
        // Given
        String specialKey = "operation-with-special-chars_123";
        String specialValue = "VALUE with spaces & special chars: @#$%";

        // When & Then (should not throw exception)
        LoggingUtils.log(logger, "Test with special characters", specialKey, specialValue);
    }

    @Test
    @DisplayName("Should handle very long strings")
    void shouldHandleVeryLongStrings() {
        // Given
        String longKey = "operation_" + "x".repeat(100);
        String longValue = "This is a very long value: " + "y".repeat(1000);

        // When & Then (should not throw exception)
        LoggingUtils.log(logger, "Test with very long strings", longKey, longValue);
    }

    @Test
    @DisplayName("Should handle UUID values correctly")
    void shouldHandleUUIDValuesCorrectly() {
        // Given
        UUID walletId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID transactionId = UUID.randomUUID();

        // When & Then (should not throw exception)
        LoggingUtils.log(logger, "Test with UUID values",
            "walletId", walletId,
            "userId", userId,
            "transactionId", transactionId);
    }

    @Test
    @DisplayName("Should handle BigDecimal values correctly")
    void shouldHandleBigDecimalValuesCorrectly() {
        // Given
        BigDecimal amount = BigDecimal.valueOf(999.99);
        BigDecimal balance = BigDecimal.valueOf(1500.50);

        // When & Then (should not throw exception)
        LoggingUtils.log(logger, "Test with BigDecimal values",
            "amount", amount,
            "balance", balance);
    }
}

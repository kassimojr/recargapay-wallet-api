package com.digital.wallet.core.exceptions;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("InsufficientFundsException Tests")
class InsufficientFundsExceptionTest {

    @Test
    @DisplayName("Should create exception with message only")
    void shouldCreateExceptionWithMessageOnly() {
        // Given
        String message = "Insufficient funds for transaction";

        // When
        InsufficientFundsException exception = new InsufficientFundsException(message);

        // Then
        assertEquals(message, exception.getMessage());
        assertNull(exception.getCause());
    }

    @Test
    @DisplayName("Should create exception with message and cause")
    void shouldCreateExceptionWithMessageAndCause() {
        // Given
        String message = "Insufficient funds with cause";
        Throwable cause = new RuntimeException("Balance check failed");

        // When
        InsufficientFundsException exception = new InsufficientFundsException(message, cause);

        // Then
        assertEquals(message, exception.getMessage());
        assertEquals(cause, exception.getCause());
    }

    @Test
    @DisplayName("Should extend RuntimeException")
    void shouldExtendRuntimeException() {
        // Given
        InsufficientFundsException exception = new InsufficientFundsException("test");

        // Then
        assertTrue(exception instanceof RuntimeException);
    }

    @Test
    @DisplayName("Should handle null message gracefully")
    void shouldHandleNullMessageGracefully() {
        // When
        InsufficientFundsException exception = new InsufficientFundsException(null);

        // Then
        assertNull(exception.getMessage());
    }

    @Test
    @DisplayName("Should handle null cause gracefully")
    void shouldHandleNullCauseGracefully() {
        // Given
        String message = "test message";

        // When
        InsufficientFundsException exception = new InsufficientFundsException(message, null);

        // Then
        assertEquals(message, exception.getMessage());
        assertNull(exception.getCause());
    }
}

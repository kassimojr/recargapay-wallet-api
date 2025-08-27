package com.digital.wallet.core.exceptions;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("SameWalletTransferException Tests")
class SameWalletTransferExceptionTest {

    @Test
    @DisplayName("Should create exception with message only")
    void shouldCreateExceptionWithMessageOnly() {
        // Given
        String message = "Cannot transfer to the same wallet";

        // When
        SameWalletTransferException exception = new SameWalletTransferException(message);

        // Then
        assertEquals(message, exception.getMessage());
        assertNull(exception.getCause());
    }

    @Test
    @DisplayName("Should create exception with message and cause")
    void shouldCreateExceptionWithMessageAndCause() {
        // Given
        String message = "Transfer to same wallet error";
        Throwable cause = new RuntimeException("Validation error");

        // When
        SameWalletTransferException exception = new SameWalletTransferException(message, cause);

        // Then
        assertEquals(message, exception.getMessage());
        assertEquals(cause, exception.getCause());
    }

    @Test
    @DisplayName("Should extend RuntimeException")
    void shouldExtendRuntimeException() {
        // Given
        SameWalletTransferException exception = new SameWalletTransferException("test");

        // Then
        assertTrue(exception instanceof RuntimeException);
    }

    @Test
    @DisplayName("Should handle null message gracefully")
    void shouldHandleNullMessageGracefully() {
        // When
        SameWalletTransferException exception = new SameWalletTransferException(null);

        // Then
        assertNull(exception.getMessage());
    }

    @Test
    @DisplayName("Should handle null cause gracefully")
    void shouldHandleNullCauseGracefully() {
        // Given
        String message = "test message";

        // When
        SameWalletTransferException exception = new SameWalletTransferException(message, null);

        // Then
        assertEquals(message, exception.getMessage());
        assertNull(exception.getCause());
    }
}

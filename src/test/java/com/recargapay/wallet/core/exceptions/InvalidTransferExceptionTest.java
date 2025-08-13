package com.recargapay.wallet.core.exceptions;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("InvalidTransferException Tests")
class InvalidTransferExceptionTest {

    @Test
    @DisplayName("Should create InvalidTransferException with message")
    void shouldCreateInvalidTransferExceptionWithMessage() {
        // Given
        String message = "Invalid transfer operation";

        // When
        InvalidTransferException exception = new InvalidTransferException(message);

        // Then
        assertThat(exception.getMessage()).isEqualTo(message);
        assertThat(exception).isInstanceOf(RuntimeException.class);
    }

    @Test
    @DisplayName("Should create InvalidTransferException with null message")
    void shouldCreateInvalidTransferExceptionWithNullMessage() {
        // When
        InvalidTransferException exception = new InvalidTransferException(null);

        // Then
        assertThat(exception.getMessage()).isNull();
        assertThat(exception).isInstanceOf(RuntimeException.class);
    }

    @Test
    @DisplayName("Should create InvalidTransferException with empty message")
    void shouldCreateInvalidTransferExceptionWithEmptyMessage() {
        // Given
        String message = "";

        // When
        InvalidTransferException exception = new InvalidTransferException(message);

        // Then
        assertThat(exception.getMessage()).isEmpty();
        assertThat(exception).isInstanceOf(RuntimeException.class);
    }

    @Test
    @DisplayName("Should throw InvalidTransferException when used in throw statement")
    void shouldThrowInvalidTransferExceptionWhenUsedInThrowStatement() {
        // Given
        String message = "Transfer not allowed";

        // When & Then
        assertThatThrownBy(() -> {
            throw new InvalidTransferException(message);
        })
        .isInstanceOf(InvalidTransferException.class)
        .hasMessage(message);
    }

    @Test
    @DisplayName("Should create InvalidTransferException with detailed message")
    void shouldCreateInvalidTransferExceptionWithDetailedMessage() {
        // Given
        String message = "Invalid transfer: source and destination wallets cannot be the same";

        // When
        InvalidTransferException exception = new InvalidTransferException(message);

        // Then
        assertThat(exception.getMessage()).isEqualTo(message);
        assertThat(exception.getMessage()).contains("Invalid transfer");
        assertThat(exception.getMessage()).contains("source and destination");
    }

    @Test
    @DisplayName("Should create InvalidTransferException with message and cause")
    void shouldCreateInvalidTransferExceptionWithMessageAndCause() {
        // Given
        String message = "Invalid transfer with cause";
        Throwable cause = new IllegalArgumentException("Invalid parameters");

        // When
        InvalidTransferException exception = new InvalidTransferException(message, cause);

        // Then
        assertThat(exception.getMessage()).isEqualTo(message);
        assertThat(exception.getCause()).isEqualTo(cause);
        assertThat(exception).isInstanceOf(RuntimeException.class);
    }

    @Test
    @DisplayName("Should handle null cause gracefully")
    void shouldHandleNullCauseGracefully() {
        // Given
        String message = "test message";

        // When
        InvalidTransferException exception = new InvalidTransferException(message, null);

        // Then
        assertThat(exception.getMessage()).isEqualTo(message);
        assertThat(exception.getCause()).isNull();
    }
}

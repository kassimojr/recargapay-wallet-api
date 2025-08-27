package com.digital.wallet.core.exceptions;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("InvalidDateFormatException Tests")
class InvalidDateFormatExceptionTest {

    @Test
    @DisplayName("Should create exception with message only")
    void shouldCreateExceptionWithMessageOnly() {
        // Given
        String message = "Invalid date format provided";

        // When
        InvalidDateFormatException exception = new InvalidDateFormatException(message);

        // Then
        assertEquals(message, exception.getMessage());
        assertEquals("INVALID_DATE_FORMAT", exception.getErrorCode());
        assertNotNull(exception.getExpectedFormats());
        assertEquals(2, exception.getExpectedFormats().length);
        assertEquals("ISO (2023-01-01T12:00:00)", exception.getExpectedFormats()[0]);
        assertEquals("Simple (2023-01-01 12:00:00)", exception.getExpectedFormats()[1]);
    }

    @Test
    @DisplayName("Should create exception with message and cause")
    void shouldCreateExceptionWithMessageAndCause() {
        // Given
        String message = "Invalid date format with cause";
        Throwable cause = new RuntimeException("Parsing error");

        // When
        InvalidDateFormatException exception = new InvalidDateFormatException(message, cause);

        // Then
        assertEquals(message, exception.getMessage());
        assertEquals(cause, exception.getCause());
        assertEquals("INVALID_DATE_FORMAT", exception.getErrorCode());
        assertNotNull(exception.getExpectedFormats());
        assertEquals(3, exception.getExpectedFormats().length);
        assertEquals("ISO (2023-01-01T12:00:00)", exception.getExpectedFormats()[0]);
        assertEquals("Simple (2023-01-01 12:00:00)", exception.getExpectedFormats()[1]);
        assertEquals("Date (2023-01-01)", exception.getExpectedFormats()[2]);
    }

    @Test
    @DisplayName("Should create exception with message and custom expected formats")
    void shouldCreateExceptionWithMessageAndCustomExpectedFormats() {
        // Given
        String message = "Custom format error";
        String[] customFormats = {"yyyy-MM-dd", "dd/MM/yyyy", "MM-dd-yyyy"};

        // When
        InvalidDateFormatException exception = new InvalidDateFormatException(message, customFormats);

        // Then
        assertEquals(message, exception.getMessage());
        assertEquals("INVALID_DATE_FORMAT", exception.getErrorCode());
        assertNotNull(exception.getExpectedFormats());
        assertEquals(3, exception.getExpectedFormats().length);
        assertArrayEquals(customFormats, exception.getExpectedFormats());
    }

    @Test
    @DisplayName("Should create exception with message, cause and custom expected formats")
    void shouldCreateExceptionWithMessageCauseAndCustomExpectedFormats() {
        // Given
        String message = "Complex format error";
        Throwable cause = new IllegalArgumentException("Invalid format");
        String[] customFormats = {"yyyy/MM/dd HH:mm:ss", "dd-MM-yyyy HH:mm"};

        // When
        InvalidDateFormatException exception = new InvalidDateFormatException(message, cause, customFormats);

        // Then
        assertEquals(message, exception.getMessage());
        assertEquals(cause, exception.getCause());
        assertEquals("INVALID_DATE_FORMAT", exception.getErrorCode());
        assertNotNull(exception.getExpectedFormats());
        assertEquals(2, exception.getExpectedFormats().length);
        assertArrayEquals(customFormats, exception.getExpectedFormats());
    }

    @Test
    @DisplayName("Should have correct ResponseStatus annotation")
    void shouldHaveCorrectResponseStatusAnnotation() {
        // When
        ResponseStatus responseStatus = InvalidDateFormatException.class.getAnnotation(ResponseStatus.class);

        // Then
        assertNotNull(responseStatus);
        assertEquals(HttpStatus.BAD_REQUEST, responseStatus.value());
    }

    @Test
    @DisplayName("Should return correct error code")
    void shouldReturnCorrectErrorCode() {
        // Given
        InvalidDateFormatException exception = new InvalidDateFormatException("Test message");

        // When
        String errorCode = exception.getErrorCode();

        // Then
        assertEquals("INVALID_DATE_FORMAT", errorCode);
    }

    @Test
    @DisplayName("Should handle null expected formats gracefully")
    void shouldHandleNullExpectedFormatsGracefully() {
        // Given
        String message = "Test with null formats";
        String[] nullFormats = null;

        // When
        InvalidDateFormatException exception = new InvalidDateFormatException(message, nullFormats);

        // Then
        assertEquals(message, exception.getMessage());
        assertEquals("INVALID_DATE_FORMAT", exception.getErrorCode());
        assertNull(exception.getExpectedFormats());
    }

    @Test
    @DisplayName("Should handle empty expected formats array")
    void shouldHandleEmptyExpectedFormatsArray() {
        // Given
        String message = "Test with empty formats";
        String[] emptyFormats = {};

        // When
        InvalidDateFormatException exception = new InvalidDateFormatException(message, emptyFormats);

        // Then
        assertEquals(message, exception.getMessage());
        assertEquals("INVALID_DATE_FORMAT", exception.getErrorCode());
        assertNotNull(exception.getExpectedFormats());
        assertEquals(0, exception.getExpectedFormats().length);
    }
}

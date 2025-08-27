package com.digital.wallet.core.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exception thrown when the provided date format is invalid
 */
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class InvalidDateFormatException extends RuntimeException {
    private static final String ERROR_CODE = "INVALID_DATE_FORMAT";
    private static final String DEFAULT_MESSAGE = "Invalid date format";
    private final String[] expectedFormats;

    public InvalidDateFormatException(String message) {
        super(message);
        this.expectedFormats = new String[]{"ISO (2023-01-01T12:00:00)", "Simple (2023-01-01 12:00:00)"};
    }

    public InvalidDateFormatException(String message, Throwable cause) {
        super(message, cause);
        this.expectedFormats = new String[]{"ISO (2023-01-01T12:00:00)", "Simple (2023-01-01 12:00:00)", "Date (2023-01-01)"};
    }

    public InvalidDateFormatException(String message, String[] expectedFormats) {
        super(message);
        this.expectedFormats = expectedFormats;
    }

    public InvalidDateFormatException(String message, Throwable cause, String[] expectedFormats) {
        super(message, cause);
        this.expectedFormats = expectedFormats;
    }

    public String getErrorCode() {
        return ERROR_CODE;
    }

    public String[] getExpectedFormats() {
        return expectedFormats;
    }
}

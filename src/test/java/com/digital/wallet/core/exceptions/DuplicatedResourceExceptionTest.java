package com.digital.wallet.core.exceptions;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class DuplicatedResourceExceptionTest {

    private static final String ERROR_MESSAGE = "Resource already exists";
    private static final Exception CAUSE = new RuntimeException("Original cause");

    @Test
    void constructor_ShouldCreateExceptionWithMessage_WhenOnlyMessageProvided() {
        // Arrange & Act
        DuplicatedResourceException exception = new DuplicatedResourceException(ERROR_MESSAGE);
        
        // Assert
        assertEquals(ERROR_MESSAGE, exception.getMessage());
        assertNull(exception.getCause());
    }

    @Test
    void constructor_ShouldCreateExceptionWithMessageAndCause_WhenBothProvided() {
        // Arrange & Act
        DuplicatedResourceException exception = new DuplicatedResourceException(ERROR_MESSAGE, CAUSE);
        
        // Assert
        assertEquals(ERROR_MESSAGE, exception.getMessage());
        assertEquals(CAUSE, exception.getCause());
    }
}

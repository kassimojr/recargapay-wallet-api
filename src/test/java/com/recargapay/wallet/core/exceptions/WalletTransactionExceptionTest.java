package com.recargapay.wallet.core.exceptions;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

/**
 * Tests for the WalletTransactionException class.
 */
class WalletTransactionExceptionTest {

    private static final String TEST_MESSAGE = "Test message";
    private static final Exception TEST_CAUSE = new RuntimeException("Test cause");

    @Test
    void testConstructorWithMessage() {
        // when
        WalletTransactionException exception = new WalletTransactionException(TEST_MESSAGE);
        
        // then
        assertEquals(TEST_MESSAGE, exception.getMessage());
        assertNotNull(exception);
    }
    
    @Test
    void testConstructorWithMessageAndCause() {
        // when
        WalletTransactionException exception = new WalletTransactionException(TEST_MESSAGE, TEST_CAUSE);
        
        // then
        assertEquals(TEST_MESSAGE, exception.getMessage());
        assertEquals(TEST_CAUSE, exception.getCause());
        assertNotNull(exception);
    }
}

package com.recargapay.wallet.core.exceptions;

/**
 * Specific exception for wallet transaction-related issues.
 * Used to provide more contextualized and specific error messages
 * for problems during transaction operations.
 */
public class WalletTransactionException extends RuntimeException {
    
    /**
     * Creates a new exception with the specified message.
     *
     * @param message The error message
     */
    public WalletTransactionException(String message) {
        super(message);
    }

    /**
     * Creates a new exception with the specified message and original cause.
     *
     * @param message The error message
     * @param cause The original exception that caused this error
     */
    public WalletTransactionException(String message, Throwable cause) {
        super(message, cause);
    }
}

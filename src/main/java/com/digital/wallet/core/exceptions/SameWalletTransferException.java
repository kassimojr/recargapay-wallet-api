package com.digital.wallet.core.exceptions;

/**
 * Exception thrown when a transfer attempt is made between the same wallet
 */
public class SameWalletTransferException extends RuntimeException {

    public SameWalletTransferException(String message) {
        super(message);
    }

    public SameWalletTransferException(String message, Throwable cause) {
        super(message, cause);
    }
}

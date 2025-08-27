package com.digital.wallet.core.exceptions;

/**
 * Exceção lançada quando uma operação de transferência é inválida
 */
public class InvalidTransferException extends RuntimeException {
    
    public InvalidTransferException(String message) {
        super(message);
    }
    
    public InvalidTransferException(String message, Throwable cause) {
        super(message, cause);
    }
}

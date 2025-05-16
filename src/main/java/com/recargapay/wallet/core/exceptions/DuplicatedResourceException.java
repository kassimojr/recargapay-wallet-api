package com.recargapay.wallet.core.exceptions;

public class DuplicatedResourceException extends RuntimeException {
    
    public DuplicatedResourceException(String message) {
        super(message);
    }
    
    public DuplicatedResourceException(String message, Throwable cause) {
        super(message, cause);
    }
}

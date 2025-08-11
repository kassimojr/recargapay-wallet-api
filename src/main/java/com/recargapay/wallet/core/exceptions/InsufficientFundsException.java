package com.recargapay.wallet.core.exceptions;

/**
 * Exceção lançada quando uma operação financeira não pode ser concluída devido a saldo insuficiente
 */
public class InsufficientFundsException extends RuntimeException {

    public InsufficientFundsException(String message) {
        super(message);
    }

    public InsufficientFundsException(String message, Throwable cause) {
        super(message, cause);
    }
}

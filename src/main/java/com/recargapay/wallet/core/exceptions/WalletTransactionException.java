package com.recargapay.wallet.core.exceptions;

/**
 * Exceção específica para problemas relacionados a transações na carteira.
 * Utilizada para fornecer mensagens de erro mais contextualizadas e específicas
 * para problemas durante operações de transação.
 */
public class WalletTransactionException extends RuntimeException {
    
    /**
     * Cria uma nova exceção com a mensagem especificada.
     *
     * @param message A mensagem de erro
     */
    public WalletTransactionException(String message) {
        super(message);
    }

    /**
     * Cria uma nova exceção com a mensagem especificada e a causa original.
     *
     * @param message A mensagem de erro
     * @param cause A exceção original que causou este erro
     */
    public WalletTransactionException(String message, Throwable cause) {
        super(message, cause);
    }
}

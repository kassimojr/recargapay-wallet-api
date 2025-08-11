package com.recargapay.wallet.core.ports.out;

/**
 * Port interface for domain-level logging operations.
 * This abstraction allows the domain to log events without depending on specific logging implementations.
 */
public interface DomainLogger {
    /**
     * Logs the start of a business operation
     *
     * @param operation Type of operation being performed
     * @param walletId Wallet identifier
     * @param amount Operation amount
     */
    void logOperationStart(String operation, String walletId, String amount);
    
    /**
     * Logs the start of a transfer operation
     *
     * @param operation Type of operation being performed (should be TRANSFER)
     * @param fromWalletId Source wallet identifier
     * @param toWalletId Destination wallet identifier
     * @param amount Operation amount
     */
    void logTransferStart(String operation, String fromWalletId, String toWalletId, String amount);
    
    /**
     * Logs the successful completion of a business operation
     *
     * @param operation Type of operation performed
     * @param walletId Wallet identifier
     * @param amount Operation amount
     * @param transactionId Resulting transaction identifier
     */
    void logOperationSuccess(String operation, String walletId, String amount, String transactionId);
    
    /**
     * Logs the successful completion of a transfer operation
     *
     * @param operation Type of operation performed (should be TRANSFER)
     * @param fromWalletId Source wallet identifier
     * @param toWalletId Destination wallet identifier
     * @param amount Operation amount
     * @param transactionId Resulting transaction identifier
     */
    void logTransferSuccess(String operation, String fromWalletId, String toWalletId, String amount, String transactionId);
    
    /**
     * Logs an error that occurred during a business operation
     *
     * @param operation Type of operation that failed
     * @param walletId Wallet identifier
     * @param errorType Type of error
     * @param errorMessage Detailed error message
     */
    void logOperationError(String operation, String walletId, String errorType, String errorMessage);
}

package com.recargapay.wallet.core.ports.in;

import com.recargapay.wallet.core.domain.Transaction;

import java.util.List;
import java.util.UUID;

/**
 * Interface for transaction history related use cases
 */
public interface TransactionHistoryUseCase {
    
    /**
     * Retrieves transaction history for a wallet
     * @param walletId Wallet ID
     * @return List of wallet transactions
     */
    List<Transaction> getTransactionHistory(UUID walletId);
    
    /**
     * Retrieves transaction history for a wallet within a specific period
     * @param walletId Wallet ID
     * @param startDate Start date of the period (ISO or simple format)
     * @param endDate End date of the period (ISO or simple format)
     * @return List of wallet transactions within the specified period
     */
    List<Transaction> getTransactionHistory(UUID walletId, String startDate, String endDate);
    
    /**
     * Retrieves transaction history for a wallet based on the provided parameters.
     * This method unifies different ways of searching history:
     * - If only date is provided, returns transactions for that specific day
     * - If startDate and endDate are provided, returns transactions within that period
     * - If no parameters are provided, returns all transactions
     * 
     * @param walletId Wallet ID
     * @param date Specific date (optional)
     * @param startDate Start date of the period (optional)
     * @param endDate End date of the period (optional)
     * @return List of wallet transactions filtered according to the parameters
     */
    List<Transaction> getFilteredTransactionHistory(UUID walletId, String date, String startDate, String endDate);
}

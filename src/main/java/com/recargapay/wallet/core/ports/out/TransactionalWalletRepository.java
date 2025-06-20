package com.recargapay.wallet.core.ports.out;

import com.recargapay.wallet.core.domain.Transaction;
import com.recargapay.wallet.core.domain.TransactionType;
import com.recargapay.wallet.core.domain.Wallet;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

/**
 * Interface for transactional operations on wallets.
 * Extends the basic wallet repository with operations that
 * require specific transactional control.
 */
public interface TransactionalWalletRepository extends WalletRepository {
    
    /**
     * Updates a wallet's balance
     * @param walletId Wallet ID
     * @param amount Amount to be added or subtracted
     * @param isDebit true for debit, false for credit
     * @return true if the update was successful
     */
    boolean updateWalletBalance(UUID walletId, BigDecimal amount, boolean isDebit);
    
    /**
     * Creates a new transaction for a wallet
     * @param walletId Wallet ID
     * @param amount Transaction amount
     * @param type Transaction type
     * @param relatedUserId Related user ID (optional)
     * @param timestamp Transaction date/time
     * @return Created transaction
     */
    Transaction createTransaction(UUID walletId, BigDecimal amount, 
                              TransactionType type, UUID relatedUserId,
                              LocalDateTime timestamp);
}

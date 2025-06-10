package com.recargapay.wallet.core.services;

import com.recargapay.wallet.core.domain.Transaction;
import com.recargapay.wallet.core.domain.TransactionType;
import com.recargapay.wallet.core.domain.Wallet;
import com.recargapay.wallet.core.exceptions.WalletNotFoundException;
import com.recargapay.wallet.core.ports.in.DepositUseCase;
import com.recargapay.wallet.core.ports.out.TransactionalWalletRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Implementation of the use case for deposits in a wallet
 */
@Service
public class DepositService implements DepositUseCase {

    private final TransactionalWalletRepository walletRepository;
    private final Logger logger = LoggerFactory.getLogger(DepositService.class);

    /**
     * Constructor
     * 
     * @param walletRepository wallet repository with support for transactional operations
     */
    public DepositService(TransactionalWalletRepository walletRepository) {
        this.walletRepository = walletRepository;
    }

    /**
     * Makes a deposit into a wallet, updating its balance and recording the transaction
     *
     * @param walletId the wallet ID, must not be null
     * @param amount amount to be deposited, must not be null
     * @return the created deposit transaction
     * @throws IllegalArgumentException if wallet ID is null or deposit amount is null/zero/negative
     * @throws WalletNotFoundException if the wallet is not found
     */
    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public Transaction deposit(UUID walletId, BigDecimal amount) {
        validateDepositParams(walletId, amount);
        logger.info("Starting deposit operation for wallet {} with amount {}", walletId, amount);
        
        Wallet wallet = walletRepository.findById(walletId)
                .orElseThrow(() -> {
                    logger.error("Wallet not found: {}", walletId);
                    return new WalletNotFoundException("Wallet not found: " + walletId);
                });
        
        logger.debug("Wallet found: {} with current balance: {}", walletId, wallet.getBalance());

        // Update wallet balance
        if (!walletRepository.updateWalletBalance(walletId, amount, false)) {
            throw new WalletNotFoundException("Wallet not found or could not be updated: " + walletId);
        }
        
        // Create transaction
        LocalDateTime now = LocalDateTime.now();
        Transaction transaction = walletRepository.createTransaction(
            walletId, amount, TransactionType.DEPOSIT, wallet.getUserId(), now
        );
        
        logger.info("Deposit successfully completed for wallet {}: {}", walletId, transaction);
        
        return transaction;
    }

    /**
     * Validates deposit parameters
     *
     * @param walletId Wallet ID
     * @param amount Amount to deposit
     * @throws IllegalArgumentException if parameters are invalid
     */
    private void validateDepositParams(UUID walletId, BigDecimal amount) {
        if (walletId == null) {
            throw new IllegalArgumentException("Wallet ID cannot be null");
        }

        if (amount == null) {
            throw new IllegalArgumentException("Deposit amount cannot be null");
        }

        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Deposit amount must be greater than zero");
        }
    }
}

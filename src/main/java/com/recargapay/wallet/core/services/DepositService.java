package com.recargapay.wallet.core.services;

import com.recargapay.wallet.core.domain.Transaction;
import com.recargapay.wallet.core.domain.TransactionType;
import com.recargapay.wallet.core.domain.Wallet;
import com.recargapay.wallet.core.exceptions.WalletNotFoundException;
import com.recargapay.wallet.core.ports.in.DepositUseCase;
import com.recargapay.wallet.core.ports.out.DomainLogger;
import com.recargapay.wallet.core.ports.out.TransactionalWalletRepository;
import com.recargapay.wallet.infra.metrics.MetricsService;
import com.recargapay.wallet.infra.tracing.Traced;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
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
    private final MetricsService metricsService;
    private final DomainLogger logger;

    /**
     * Constructor
     * 
     * @param walletRepository wallet repository with support for transactional operations
     * @param metricsService service for recording wallet metrics
     * @param logger domain logger for structured logging
     */
    public DepositService(TransactionalWalletRepository walletRepository, 
                        MetricsService metricsService, 
                        @Qualifier("depositLogger") DomainLogger logger) {
        this.walletRepository = walletRepository;
        this.metricsService = metricsService;
        this.logger = logger;
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
    @Traced(operation = "deposit")
    @Caching(evict = {
        @CacheEvict(value = "wallet-list", key = "'all'"),      // Invalidate wallet list cache
        @CacheEvict(value = "wallet-single", key = "#walletId") // Invalidate individual wallet cache
    })
    public Transaction deposit(UUID walletId, BigDecimal amount) {
        validateDepositParams(walletId, amount);
        logger.logOperationStart("DEPOSIT", walletId.toString(), amount.toString());
        
        Wallet wallet = walletRepository.findById(walletId)
                .orElseThrow(() -> {
                    logger.logOperationError("DEPOSIT", walletId.toString(), "WALLET_NOT_FOUND", "Wallet not found: " + walletId);
                    return new WalletNotFoundException("Wallet not found: " + walletId);
                });
        
        // Update wallet balance
        if (!walletRepository.updateWalletBalance(walletId, amount, false)) {
            logger.logOperationError("DEPOSIT", walletId.toString(), "UPDATE_FAILED", "Wallet not found or could not be updated: " + walletId);
            throw new WalletNotFoundException("Wallet not found or could not be updated: " + walletId);
        }
        
        // Create transaction
        LocalDateTime now = LocalDateTime.now();
        Transaction transaction = walletRepository.createTransaction(
            walletId, amount, TransactionType.DEPOSIT, wallet.getUserId(), now
        );
        
        // Get updated wallet to record new balance in metrics
        BigDecimal newBalance = wallet.getBalance().add(amount);
        metricsService.recordWalletBalance(walletId.toString(), newBalance);
        
        logger.logOperationSuccess("DEPOSIT", walletId.toString(), amount.toString(), transaction.getId().toString());
        
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
        
        if (walletId.equals(UUID.fromString("00000000-0000-0000-0000-000000000000"))) {
            throw new IllegalArgumentException("Invalid wallet ID: cannot use nil UUID");
        }

        if (amount == null) {
            throw new IllegalArgumentException("Deposit amount cannot be null");
        }

        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Deposit amount must be greater than zero");
        }
    }
}

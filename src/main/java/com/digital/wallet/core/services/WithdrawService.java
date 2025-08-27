package com.digital.wallet.core.services;

import com.digital.wallet.core.domain.Transaction;
import com.digital.wallet.core.domain.TransactionType;
import com.digital.wallet.core.domain.Wallet;
import com.digital.wallet.core.exceptions.InsufficientFundsException;
import com.digital.wallet.core.exceptions.WalletNotFoundException;
import com.digital.wallet.core.ports.in.WithdrawUseCase;
import com.digital.wallet.core.ports.out.DomainLogger;
import com.digital.wallet.core.ports.out.TransactionalWalletRepository;
import com.digital.wallet.infra.metrics.MetricsService;
import com.digital.wallet.infra.tracing.Traced;
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
 * Implementation of the use case for withdrawals from a wallet
 */
@Service
public class WithdrawService implements WithdrawUseCase {

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
    public WithdrawService(TransactionalWalletRepository walletRepository, 
                         MetricsService metricsService, 
                         @Qualifier("withdrawLogger") DomainLogger logger) {
        this.walletRepository = walletRepository;
        this.metricsService = metricsService;
        this.logger = logger;
    }

    /**
     * Performs a withdrawal from a wallet, updating its balance and recording the transaction
     *
     * @param walletId the wallet ID, must not be null
     * @param amount amount to be withdrawn, must not be null
     * @return the created withdrawal transaction
     * @throws IllegalArgumentException if wallet id is null or withdrawal amount is null/zero/negative
     * @throws WalletNotFoundException if the wallet is not found
     * @throws InsufficientFundsException if the wallet does not have sufficient balance
     */
    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    @Traced(operation = "withdraw")
    @Caching(evict = {
        @CacheEvict(value = "wallet-list", key = "'all'"),      // Invalidate wallet list cache
        @CacheEvict(value = "wallet-single", key = "#walletId") // Invalidate individual wallet cache
    })
    public Transaction withdraw(UUID walletId, BigDecimal amount) {
        validateWithdrawParams(walletId, amount);
        logger.logOperationStart("WITHDRAW", walletId.toString(), amount.toString());
        
        Wallet wallet = walletRepository.findById(walletId)
                .orElseThrow(() -> {
                    logger.logOperationError("WITHDRAW", walletId.toString(), "WALLET_NOT_FOUND", "Wallet not found: " + walletId);
                    return new WalletNotFoundException("Wallet not found: " + walletId);
                });
        
        // Checking if there is sufficient balance
        if (wallet.getBalance().compareTo(amount) < 0) {
            String errorMsg = String.format("Insufficient balance for withdrawal. Wallet: %s, Balance: %s, Withdrawal amount: %s", 
                    walletId, wallet.getBalance(), amount);
            logger.logOperationError("WITHDRAW", walletId.toString(), "INSUFFICIENT_FUNDS", errorMsg);
            throw new InsufficientFundsException(errorMsg);
        }
        
        // Update wallet balance
        if (!walletRepository.updateWalletBalance(walletId, amount, true)) {
            logger.logOperationError("WITHDRAW", walletId.toString(), "UPDATE_FAILED", "Wallet not found or could not be updated: " + walletId);
            throw new WalletNotFoundException("Wallet not found or could not be updated: " + walletId);
        }
        
        // Create transaction
        LocalDateTime now = LocalDateTime.now();
        Transaction transaction = walletRepository.createTransaction(
            walletId, amount, TransactionType.WITHDRAW, wallet.getUserId(), now
        );
        
        // Get updated wallet to record new balance in metrics
        BigDecimal newBalance = wallet.getBalance().subtract(amount);
        metricsService.recordWalletBalance(walletId.toString(), newBalance);
        
        logger.logOperationSuccess("WITHDRAW", walletId.toString(), amount.toString(), transaction.getId().toString());
        
        return transaction;
    }

    /**
     * Validates the withdrawal parameters
     *
     * @param walletId wallet ID
     * @param amount amount to withdraw
     * @throws IllegalArgumentException if the parameters are invalid
     */
    private void validateWithdrawParams(UUID walletId, BigDecimal amount) {
        if (walletId == null) {
            throw new IllegalArgumentException("Wallet ID cannot be null");
        }

        if (amount == null) {
            throw new IllegalArgumentException("Withdrawal amount cannot be null");
        }

        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Withdrawal amount must be greater than zero");
        }
    }
}

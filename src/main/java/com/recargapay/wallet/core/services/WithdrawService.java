package com.recargapay.wallet.core.services;

import com.recargapay.wallet.core.domain.Transaction;
import com.recargapay.wallet.core.domain.TransactionType;
import com.recargapay.wallet.core.domain.Wallet;
import com.recargapay.wallet.core.exceptions.InsufficientFundsException;
import com.recargapay.wallet.core.exceptions.WalletNotFoundException;
import com.recargapay.wallet.core.ports.in.WithdrawUseCase;
import com.recargapay.wallet.core.ports.out.TransactionalWalletRepository;
import com.recargapay.wallet.infra.metrics.MetricsService;
import com.recargapay.wallet.infra.tracing.Traced;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
    private final Logger logger = LoggerFactory.getLogger(WithdrawService.class);

    /**
     * Constructor
     * 
     * @param walletRepository wallet repository with support for transactional operations
     * @param metricsService service for recording wallet metrics
     */
    public WithdrawService(TransactionalWalletRepository walletRepository, MetricsService metricsService) {
        this.walletRepository = walletRepository;
        this.metricsService = metricsService;
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
    public Transaction withdraw(UUID walletId, BigDecimal amount) {
        validateWithdrawParams(walletId, amount);
        logger.info("Starting withdrawal operation for wallet {} in the amount of {}", walletId, amount);
        
        Wallet wallet = walletRepository.findById(walletId)
                .orElseThrow(() -> {
                    logger.error("Wallet not found: {}", walletId);
                    return new WalletNotFoundException("Wallet not found: " + walletId);
                });
        
        logger.debug("Wallet found: {} with current balance: {}", walletId, wallet.getBalance());
        
        // Checking if there is sufficient balance
        if (wallet.getBalance().compareTo(amount) < 0) {
            String errorMsg = String.format("Insufficient balance for withdrawal. Wallet: %s, Balance: %s, Withdrawal amount: %s", 
                    walletId, wallet.getBalance(), amount);
            logger.error(errorMsg);
            throw new InsufficientFundsException(errorMsg);
        }
        
        // Update wallet balance
        if (!walletRepository.updateWalletBalance(walletId, amount, true)) {
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
        
        logger.info("Withdrawal successfully completed for wallet {}: {}", walletId, transaction);
        
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

package com.digital.wallet.core.services;

import com.digital.wallet.core.domain.Transaction;
import com.digital.wallet.core.domain.TransactionType;
import com.digital.wallet.core.domain.Wallet;
import com.digital.wallet.core.exceptions.InsufficientFundsException;
import com.digital.wallet.core.exceptions.SameWalletTransferException;
import com.digital.wallet.core.exceptions.WalletNotFoundException;
import com.digital.wallet.core.ports.in.TransferFundsUseCase;
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
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Service responsible for transferring funds between wallets
 */
@Service
public class TransferFundsService implements TransferFundsUseCase {

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
    public TransferFundsService(TransactionalWalletRepository walletRepository, 
                              MetricsService metricsService, 
                              @Qualifier("transferLogger") DomainLogger logger) {
        this.walletRepository = walletRepository;
        this.metricsService = metricsService;
        this.logger = logger;
    }

    /**
     * Transfers an amount from one wallet to another
     *
     * @param fromWalletId source of the transfer, must not be null
     * @param toWalletId   destination of the transfer, must not be null
     * @param amount       amount to be transferred, must be greater than zero
     * @throws IllegalArgumentException if any parameter is null or the amount is zero/negative
     * @throws WalletNotFoundException if any of the wallets is not found
     * @throws InsufficientFundsException if the source wallet does not have sufficient balance
     * @throws SameWalletTransferException if the source and destination wallets are the same
     */
    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    @Traced(operation = "transfer")
    @Caching(evict = {
        @CacheEvict(value = "wallet-list", key = "'all'"),           // Invalidate wallet list cache
        @CacheEvict(value = "wallet-single", key = "#fromWalletId"), // Invalidate source wallet cache
        @CacheEvict(value = "wallet-single", key = "#toWalletId")    // Invalidate destination wallet cache
    })
    public List<Transaction> transfer(UUID fromWalletId, UUID toWalletId, BigDecimal amount) {
        validateTransferParams(fromWalletId, toWalletId, amount);
        logger.logTransferStart("TRANSFER", fromWalletId.toString(), toWalletId.toString(), amount.toString());

        // Fetch wallets without pessimistic locking
        Wallet fromWallet = walletRepository.findById(fromWalletId)
                .orElseThrow(() -> {
                    logger.logOperationError("TRANSFER", fromWalletId.toString(), "SOURCE_WALLET_NOT_FOUND", "Source wallet not found: " + fromWalletId);
                    return new WalletNotFoundException("Source wallet not found: " + fromWalletId);
                });

        Wallet toWallet = walletRepository.findById(toWalletId)
                .orElseThrow(() -> {
                    logger.logOperationError("TRANSFER", fromWalletId.toString(), "DESTINATION_WALLET_NOT_FOUND", "Destination wallet not found: " + toWalletId);
                    return new WalletNotFoundException("Destination wallet not found: " + toWalletId);
                });

        // Check balance
        if (fromWallet.getBalance().compareTo(amount) < 0) {
            String message = String.format("Insufficient balance. Wallet: %s, Balance: %s, Amount: %s",
                    fromWalletId, fromWallet.getBalance(), amount);
            logger.logOperationError("TRANSFER", fromWalletId.toString(), "INSUFFICIENT_FUNDS", message);
            throw new InsufficientFundsException(message);
        }

        // Update source wallet balance
        if (!walletRepository.updateWalletBalance(fromWalletId, amount, true)) {
            logger.logOperationError("TRANSFER", fromWalletId.toString(), "SOURCE_UPDATE_FAILED", "Source wallet not found or could not be updated: " + fromWalletId);
            throw new WalletNotFoundException("Source wallet not found or could not be updated: " + fromWalletId);
        }
        
        // Update destination wallet balance
        if (!walletRepository.updateWalletBalance(toWalletId, amount, false)) {
            // Revert the previous operation
            walletRepository.updateWalletBalance(fromWalletId, amount, false);
            logger.logOperationError("TRANSFER", fromWalletId.toString(), "DESTINATION_UPDATE_FAILED", "Destination wallet not found or could not be updated: " + toWalletId);
            throw new WalletNotFoundException("Destination wallet not found or could not be updated: " + toWalletId);
        }
        
        // Creating transactions
        LocalDateTime now = LocalDateTime.now();
        List<Transaction> transactions = new ArrayList<>();
        
        // TRANSFER_OUT transaction in the source wallet
        Transaction outTransaction = walletRepository.createTransaction(
            fromWalletId, amount, TransactionType.TRANSFER_OUT, toWallet.getUserId(), now
        );
        transactions.add(outTransaction);
        
        // TRANSFER_IN transaction in the destination wallet
        Transaction inTransaction = walletRepository.createTransaction(
            toWalletId, amount, TransactionType.TRANSFER_IN, fromWallet.getUserId(), now
        );
        transactions.add(inTransaction);

        // Record updated wallet balances in metrics
        BigDecimal fromWalletNewBalance = fromWallet.getBalance().subtract(amount);
        BigDecimal toWalletNewBalance = toWallet.getBalance().add(amount);
        
        metricsService.recordWalletBalance(fromWalletId.toString(), fromWalletNewBalance);
        metricsService.recordWalletBalance(toWalletId.toString(), toWalletNewBalance);

        logger.logTransferSuccess("TRANSFER", fromWalletId.toString(), toWalletId.toString(), amount.toString(), outTransaction.getId().toString());
                
        return transactions;
    }

    /**
     * Validates the transfer parameters
     *
     * @param fromWalletId Source wallet ID
     * @param toWalletId   Destination wallet ID
     * @param amount       Amount to transfer
     * @throws IllegalArgumentException if the parameters are invalid
     * @throws SameWalletTransferException if the source and destination wallets are the same
     */
    private void validateTransferParams(UUID fromWalletId, UUID toWalletId, BigDecimal amount) {
        if (fromWalletId == null) {
            throw new IllegalArgumentException("Source wallet ID cannot be null");
        }

        if (toWalletId == null) {
            throw new IllegalArgumentException("Destination wallet ID cannot be null");
        }

        if (fromWalletId.equals(toWalletId)) {
            throw new SameWalletTransferException("Source and destination wallets cannot be the same");
        }

        if (amount == null) {
            throw new IllegalArgumentException("Transfer amount cannot be null");
        }

        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Transfer amount must be greater than zero");
        }
    }
}

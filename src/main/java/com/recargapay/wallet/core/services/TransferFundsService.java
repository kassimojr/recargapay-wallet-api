package com.recargapay.wallet.core.services;

import com.recargapay.wallet.core.domain.Transaction;
import com.recargapay.wallet.core.domain.TransactionType;
import com.recargapay.wallet.core.domain.Wallet;
import com.recargapay.wallet.core.exceptions.InsufficientFundsException;
import com.recargapay.wallet.core.exceptions.SameWalletTransferException;
import com.recargapay.wallet.core.exceptions.WalletNotFoundException;
import com.recargapay.wallet.core.ports.in.TransferFundsUseCase;
import com.recargapay.wallet.core.ports.out.TransactionalWalletRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
    private final Logger logger = LoggerFactory.getLogger(TransferFundsService.class);

    /**
     * Constructor
     *
     * @param walletRepository wallet repository with support for transactional operations
     */
    public TransferFundsService(TransactionalWalletRepository walletRepository) {
        this.walletRepository = walletRepository;
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
    public List<Transaction> transfer(UUID fromWalletId, UUID toWalletId, BigDecimal amount) {
        validateTransferParams(fromWalletId, toWalletId, amount);
        logger.info("Starting transfer of {} from wallet {} to {}", amount, fromWalletId, toWalletId);

        // Fetch wallets without pessimistic locking
        Wallet fromWallet = walletRepository.findById(fromWalletId)
                .orElseThrow(() -> {
                    logger.error("Source wallet not found: {}", fromWalletId);
                    return new WalletNotFoundException("Source wallet not found: " + fromWalletId);
                });

        Wallet toWallet = walletRepository.findById(toWalletId)
                .orElseThrow(() -> {
                    logger.error("Destination wallet not found: {}", toWalletId);
                    return new WalletNotFoundException("Destination wallet not found: " + toWalletId);
                });

        // Check balance
        if (fromWallet.getBalance().compareTo(amount) < 0) {
            String message = String.format("Insufficient balance. Wallet: %s, Balance: %s, Amount: %s",
                    fromWalletId, fromWallet.getBalance(), amount);
            logger.error(message);
            throw new InsufficientFundsException(message);
        }

        // Update source wallet balance
        if (!walletRepository.updateWalletBalance(fromWalletId, amount, true)) {
            throw new WalletNotFoundException("Source wallet not found or could not be updated: " + fromWalletId);
        }
        
        // Update destination wallet balance
        if (!walletRepository.updateWalletBalance(toWalletId, amount, false)) {
            // Revert the previous operation
            walletRepository.updateWalletBalance(fromWalletId, amount, false);
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

        logger.info("Transfer successfully completed. Source: {}, Destination: {}, Amount: {}", 
                fromWalletId, toWalletId, amount);
                
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

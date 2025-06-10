package com.recargapay.wallet.core.services;

import com.recargapay.wallet.core.domain.Transaction;
import com.recargapay.wallet.core.domain.TransactionType;
import com.recargapay.wallet.core.domain.Wallet;
import com.recargapay.wallet.core.exceptions.InvalidDateFormatException;
import com.recargapay.wallet.core.exceptions.UserNotFoundException;
import com.recargapay.wallet.core.exceptions.WalletAlreadyExistsException;
import com.recargapay.wallet.core.exceptions.WalletNotFoundException;
import com.recargapay.wallet.core.ports.in.CreateWalletUseCase;
import com.recargapay.wallet.core.ports.out.TransactionRepository;
import com.recargapay.wallet.core.ports.out.UserRepository;
import com.recargapay.wallet.core.ports.out.WalletRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.UUID;

@Service
public class CreateWalletService implements CreateWalletUseCase {
    private final WalletRepository walletRepository;
    private final UserRepository userRepository;
    private final TransactionRepository transactionRepository;
    
    private static final DateTimeFormatter ISO_FORMATTER = DateTimeFormatter.ISO_DATE_TIME;
    private static final DateTimeFormatter SIMPLE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public CreateWalletService(
            WalletRepository walletRepository, 
            UserRepository userRepository,
            TransactionRepository transactionRepository) {
        this.walletRepository = walletRepository;
        this.userRepository = userRepository;
        this.transactionRepository = transactionRepository;
    }

    @Override
    @Transactional
    public Wallet create(Wallet wallet) {
        // Validate if the user exists before creating the wallet
        if (!userRepository.findById(wallet.getUserId()).isPresent()) {
            throw new UserNotFoundException("User not found: " + wallet.getUserId());
        }
        
        // Check if the user already has a wallet
        if (walletRepository.findByUserId(wallet.getUserId()).isPresent()) {
            throw new WalletAlreadyExistsException("User already has a wallet: " + wallet.getUserId());
        }
        
        return walletRepository.save(wallet);
    }

    @Override
    public Wallet findById(UUID walletId) {
        return walletRepository.findById(walletId)
            .orElseThrow(() -> new WalletNotFoundException("Wallet not found: " + walletId));
    }

    @Override
    public Wallet findBalanceAt(UUID walletId, String at) {
        // Get the wallet to ensure it exists
        Wallet wallet = walletRepository.findById(walletId)
            .orElseThrow(() -> new WalletNotFoundException("Wallet not found: " + walletId));
        
        // Convert the date string to LocalDateTime
        LocalDateTime targetDateTime = parseDateTime(at);
        
        // Find all transactions up to the specified date
        List<Transaction> transactions = transactionRepository.findByWalletIdAndTimestampLessThanEqual(
            walletId, targetDateTime);
        
        // Calculate the historical balance based on transactions
        BigDecimal historicalBalance = calculateHistoricalBalance(transactions);
        
        // Create a copy of the wallet with the historical balance
        Wallet historicalWallet = new Wallet();
        historicalWallet.setId(wallet.getId());
        historicalWallet.setUserId(wallet.getUserId());
        historicalWallet.setBalance(historicalBalance);
        historicalWallet.setCreatedAt(wallet.getCreatedAt());
        
        return historicalWallet;
    }
    
    /**
     * Calculates the balance based on a list of transactions
     */
    private BigDecimal calculateHistoricalBalance(List<Transaction> transactions) {
        BigDecimal balance = BigDecimal.ZERO;
        
        for (Transaction transaction : transactions) {
            if (transaction.getType() == TransactionType.DEPOSIT) {
                balance = balance.add(transaction.getAmount());
            } else if (transaction.getType() == TransactionType.WITHDRAW) {
                balance = balance.subtract(transaction.getAmount());
            } else if (transaction.getType() == TransactionType.TRANSFER_IN) {
                balance = balance.add(transaction.getAmount());
            } else if (transaction.getType() == TransactionType.TRANSFER_OUT) {
                balance = balance.subtract(transaction.getAmount());
            }
        }
        
        return balance;
    }
    
    /**
     * Parse date in ISO or simple format to LocalDateTime
     */
    private LocalDateTime parseDateTime(String dateTimeStr) {
        try {
            // Try ISO format
            return LocalDateTime.parse(dateTimeStr, ISO_FORMATTER);
        } catch (DateTimeParseException e1) {
            try {
                // Try simple format
                return LocalDateTime.parse(dateTimeStr, SIMPLE_FORMATTER);
            } catch (DateTimeParseException e2) {
                // Throw exception with informative message
                throw new InvalidDateFormatException(
                    "Invalid date format: " + dateTimeStr + 
                    ". Use ISO format (2023-01-01T12:00:00) or simple format (2023-01-01 12:00:00).");
            }
        }
    }
}

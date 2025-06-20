package com.recargapay.wallet.core.services;

import com.recargapay.wallet.core.domain.Transaction;
import com.recargapay.wallet.core.domain.Wallet;
import com.recargapay.wallet.core.exceptions.InvalidDateFormatException;
import com.recargapay.wallet.core.exceptions.WalletNotFoundException;
import com.recargapay.wallet.core.ports.in.TransactionHistoryUseCase;
import com.recargapay.wallet.core.ports.out.TransactionRepository;
import com.recargapay.wallet.core.ports.out.WalletRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.UUID;

@Service
public class TransactionHistoryService implements TransactionHistoryUseCase {

    private final WalletRepository walletRepository;
    private final TransactionRepository transactionRepository;
    
    private static final DateTimeFormatter ISO_FORMATTER = DateTimeFormatter.ISO_DATE_TIME;
    private static final DateTimeFormatter SIMPLE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final DateTimeFormatter DATE_ONLY_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public TransactionHistoryService(
            WalletRepository walletRepository,
            TransactionRepository transactionRepository) {
        this.walletRepository = walletRepository;
        this.transactionRepository = transactionRepository;
    }
    
    @Override
    public List<Transaction> getTransactionHistory(UUID walletId) {
        // Check if the wallet exists
        walletRepository.findById(walletId)
            .orElseThrow(() -> new WalletNotFoundException("Wallet not found: " + walletId));
        
        // Get all wallet transactions
        return transactionRepository.findByWalletId(walletId);
    }
    
    @Override
    public List<Transaction> getTransactionHistory(UUID walletId, String startDate, String endDate) {
        // Check if the wallet exists
        walletRepository.findById(walletId)
            .orElseThrow(() -> new WalletNotFoundException("Wallet not found: " + walletId));
        
        // Convert date strings to LocalDateTime
        LocalDateTime startDateTime = parseToDateTime(startDate, true); // start of day for startDate
        LocalDateTime endDateTime = parseToDateTime(endDate, false);    // end of day for endDate
        
        // Check if the start date is before the end date
        if (startDateTime.isAfter(endDateTime)) {
            throw new InvalidDateFormatException(
                "The start date (" + startDate + ") must be before the end date (" + endDate + ").");
        }
        
        // Get transactions within the specified period
        return transactionRepository.findByWalletIdAndTimestampBetween(walletId, startDateTime, endDateTime);
    }
    
    @Override
    public List<Transaction> getFilteredTransactionHistory(UUID walletId, String date, String startDate, String endDate) {
        // Check if the wallet exists
        walletRepository.findById(walletId)
            .orElseThrow(() -> new WalletNotFoundException("Wallet not found: " + walletId));
        
        // Filter logic based on parameters
        if (date != null && !date.isEmpty()) {
            // Case 1: Specific date
            return getTransactionHistory(walletId, date, date);
        } else if (startDate != null && !startDate.isEmpty() && endDate != null && !endDate.isEmpty()) {
            // Case 2: Date range
            return getTransactionHistory(walletId, startDate, endDate);
        } else {
            // Case 3: No filters - all transactions
            return getTransactionHistory(walletId);
        }
    }
    
    /**
     * Parse date string to LocalDateTime with support for multiple formats
     * @param dateTimeStr String containing the date
     * @param startOfDay If true, sets time to start of day (00:00:00); if false, to end of day (23:59:59)
     * @return Converted LocalDateTime
     * @throws InvalidDateFormatException if the format is invalid
     */
    private LocalDateTime parseToDateTime(String dateTimeStr, boolean startOfDay) {
        try {
            // Try ISO format
            return LocalDateTime.parse(dateTimeStr, ISO_FORMATTER);
        } catch (DateTimeParseException e1) {
            try {
                // Try simple format
                return LocalDateTime.parse(dateTimeStr, SIMPLE_FORMATTER);
            } catch (DateTimeParseException e2) {
                try {
                    // Try date-only format (yyyy-MM-dd)
                    LocalDate date = LocalDate.parse(dateTimeStr, DATE_ONLY_FORMATTER);
                    if (startOfDay) {
                        return date.atTime(LocalTime.MIN); // 00:00:00
                    } else {
                        return date.atTime(LocalTime.MAX); // 23:59:59.999999999
                    }
                } catch (DateTimeParseException e3) {
                    // Throw exception with informative message
                    throw new InvalidDateFormatException(
                        "Invalid date format: " + dateTimeStr + 
                        ". Use ISO format (2023-01-01T12:00:00), simple format (2023-01-01 12:00:00) or date-only format (2023-01-01).",
                        e3);
                }
            }
        }
    }
}

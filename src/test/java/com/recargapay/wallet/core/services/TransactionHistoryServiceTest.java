package com.recargapay.wallet.core.services;

import com.recargapay.wallet.core.ports.out.TransactionRepository;
import com.recargapay.wallet.core.ports.out.WalletRepository;
import com.recargapay.wallet.core.domain.Transaction;
import com.recargapay.wallet.core.domain.Wallet;
import com.recargapay.wallet.core.exceptions.InvalidDateFormatException;
import com.recargapay.wallet.core.exceptions.WalletNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("TransactionHistoryService Tests")
class TransactionHistoryServiceTest {

    @Mock
    private WalletRepository walletRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @InjectMocks
    private TransactionHistoryService transactionHistoryService;

    private UUID walletId;
    private Wallet wallet;
    private List<Transaction> mockTransactions;

    @BeforeEach
    void setUp() {
        walletId = UUID.randomUUID();
        wallet = new Wallet();
        wallet.setId(walletId);
        wallet.setBalance(new BigDecimal("1000.00"));

        // Create mock transactions
        Transaction transaction1 = new Transaction();
        transaction1.setId(UUID.randomUUID());
        transaction1.setWalletId(walletId);
        transaction1.setAmount(new BigDecimal("100.00"));
        transaction1.setTimestamp(LocalDateTime.now().minusDays(1));

        Transaction transaction2 = new Transaction();
        transaction2.setId(UUID.randomUUID());
        transaction2.setWalletId(walletId);
        transaction2.setAmount(new BigDecimal("50.00"));
        transaction2.setTimestamp(LocalDateTime.now().minusDays(2));

        mockTransactions = Arrays.asList(transaction1, transaction2);
    }

    @Test
    @DisplayName("Should get transaction history successfully")
    void shouldGetTransactionHistorySuccessfully() {
        // Given
        when(walletRepository.findById(walletId)).thenReturn(Optional.of(wallet));
        when(transactionRepository.findByWalletId(walletId)).thenReturn(mockTransactions);

        // When
        List<Transaction> result = transactionHistoryService.getTransactionHistory(walletId);

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(mockTransactions, result);
    }

    @Test
    @DisplayName("Should throw WalletNotFoundException when wallet not found")
    void shouldThrowWalletNotFoundExceptionWhenWalletNotFound() {
        // Given
        when(walletRepository.findById(walletId)).thenReturn(Optional.empty());

        // When & Then
        WalletNotFoundException exception = assertThrows(
            WalletNotFoundException.class,
            () -> transactionHistoryService.getTransactionHistory(walletId)
        );

        assertEquals("Wallet not found: " + walletId, exception.getMessage());
    }

    @Test
    @DisplayName("Should get transaction history with date range successfully")
    void shouldGetTransactionHistoryWithDateRangeSuccessfully() {
        // Given
        String startDate = "2023-01-01";
        String endDate = "2023-01-31";
        
        when(walletRepository.findById(walletId)).thenReturn(Optional.of(wallet));
        when(transactionRepository.findByWalletIdAndTimestampBetween(
            eq(walletId), any(LocalDateTime.class), any(LocalDateTime.class)))
            .thenReturn(mockTransactions);

        // When
        List<Transaction> result = transactionHistoryService.getTransactionHistory(walletId, startDate, endDate);

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(mockTransactions, result);
    }

    @Test
    @DisplayName("Should throw exception when start date is after end date")
    void shouldThrowExceptionWhenStartDateIsAfterEndDate() {
        // Given
        String startDate = "2023-01-31";
        String endDate = "2023-01-01";
        
        when(walletRepository.findById(walletId)).thenReturn(Optional.of(wallet));

        // When & Then
        InvalidDateFormatException exception = assertThrows(
            InvalidDateFormatException.class,
            () -> transactionHistoryService.getTransactionHistory(walletId, startDate, endDate)
        );

        assertTrue(exception.getMessage().contains("must be before the end date"));
    }

    @Test
    @DisplayName("Should parse ISO format date successfully")
    void shouldParseISOFormatDateSuccessfully() {
        // Given
        String startDate = "2023-01-01T10:00:00";
        String endDate = "2023-01-01T20:00:00";
        
        when(walletRepository.findById(walletId)).thenReturn(Optional.of(wallet));
        when(transactionRepository.findByWalletIdAndTimestampBetween(
            eq(walletId), any(LocalDateTime.class), any(LocalDateTime.class)))
            .thenReturn(mockTransactions);

        // When
        List<Transaction> result = transactionHistoryService.getTransactionHistory(walletId, startDate, endDate);

        // Then
        assertNotNull(result);
        assertEquals(mockTransactions, result);
    }

    @Test
    @DisplayName("Should parse simple format date successfully")
    void shouldParseSimpleFormatDateSuccessfully() {
        // Given
        String startDate = "2023-01-01 10:00:00";
        String endDate = "2023-01-01 20:00:00";
        
        when(walletRepository.findById(walletId)).thenReturn(Optional.of(wallet));
        when(transactionRepository.findByWalletIdAndTimestampBetween(
            eq(walletId), any(LocalDateTime.class), any(LocalDateTime.class)))
            .thenReturn(mockTransactions);

        // When
        List<Transaction> result = transactionHistoryService.getTransactionHistory(walletId, startDate, endDate);

        // Then
        assertNotNull(result);
        assertEquals(mockTransactions, result);
    }

    @Test
    @DisplayName("Should throw exception for invalid date format")
    void shouldThrowExceptionForInvalidDateFormat() {
        // Given
        String startDate = "invalid-date";
        String endDate = "2023-01-31";
        
        when(walletRepository.findById(walletId)).thenReturn(Optional.of(wallet));

        // When & Then
        InvalidDateFormatException exception = assertThrows(
            InvalidDateFormatException.class,
            () -> transactionHistoryService.getTransactionHistory(walletId, startDate, endDate)
        );

        assertTrue(exception.getMessage().contains("Invalid date format"));
        assertTrue(exception.getMessage().contains("invalid-date"));
    }

    @Test
    @DisplayName("Should get filtered transaction history with specific date")
    void shouldGetFilteredTransactionHistoryWithSpecificDate() {
        // Given
        String date = "2023-01-15";
        
        when(walletRepository.findById(walletId)).thenReturn(Optional.of(wallet));
        when(transactionRepository.findByWalletIdAndTimestampBetween(
            eq(walletId), any(LocalDateTime.class), any(LocalDateTime.class)))
            .thenReturn(mockTransactions);

        // When
        List<Transaction> result = transactionHistoryService.getFilteredTransactionHistory(
            walletId, date, null, null);

        // Then
        assertNotNull(result);
        assertEquals(mockTransactions, result);
    }

    @Test
    @DisplayName("Should get filtered transaction history with date range")
    void shouldGetFilteredTransactionHistoryWithDateRange() {
        // Given
        String startDate = "2023-01-01";
        String endDate = "2023-01-31";
        
        when(walletRepository.findById(walletId)).thenReturn(Optional.of(wallet));
        when(transactionRepository.findByWalletIdAndTimestampBetween(
            eq(walletId), any(LocalDateTime.class), any(LocalDateTime.class)))
            .thenReturn(mockTransactions);

        // When
        List<Transaction> result = transactionHistoryService.getFilteredTransactionHistory(
            walletId, null, startDate, endDate);

        // Then
        assertNotNull(result);
        assertEquals(mockTransactions, result);
    }

    @Test
    @DisplayName("Should get filtered transaction history without filters")
    void shouldGetFilteredTransactionHistoryWithoutFilters() {
        // Given
        when(walletRepository.findById(walletId)).thenReturn(Optional.of(wallet));
        when(transactionRepository.findByWalletId(walletId)).thenReturn(mockTransactions);

        // When
        List<Transaction> result = transactionHistoryService.getFilteredTransactionHistory(
            walletId, null, null, null);

        // Then
        assertNotNull(result);
        assertEquals(mockTransactions, result);
    }

    @Test
    @DisplayName("Should get filtered transaction history with empty date strings")
    void shouldGetFilteredTransactionHistoryWithEmptyDateStrings() {
        // Given
        when(walletRepository.findById(walletId)).thenReturn(Optional.of(wallet));
        when(transactionRepository.findByWalletId(walletId)).thenReturn(mockTransactions);

        // When
        List<Transaction> result = transactionHistoryService.getFilteredTransactionHistory(
            walletId, "", "", "");

        // Then
        assertNotNull(result);
        assertEquals(mockTransactions, result);
    }

    @Test
    @DisplayName("Should throw WalletNotFoundException in filtered history when wallet not found")
    void shouldThrowWalletNotFoundExceptionInFilteredHistoryWhenWalletNotFound() {
        // Given
        when(walletRepository.findById(walletId)).thenReturn(Optional.empty());

        // When & Then
        WalletNotFoundException exception = assertThrows(
            WalletNotFoundException.class,
            () -> transactionHistoryService.getFilteredTransactionHistory(walletId, null, null, null)
        );

        assertEquals("Wallet not found: " + walletId, exception.getMessage());
    }

    @Test
    @DisplayName("Should handle date-only format with start of day")
    void shouldHandleDateOnlyFormatWithStartOfDay() {
        // Given
        String startDate = "2023-01-01";
        String endDate = "2023-01-02";
        
        when(walletRepository.findById(walletId)).thenReturn(Optional.of(wallet));
        when(transactionRepository.findByWalletIdAndTimestampBetween(
            eq(walletId), any(LocalDateTime.class), any(LocalDateTime.class)))
            .thenReturn(mockTransactions);

        // When
        List<Transaction> result = transactionHistoryService.getTransactionHistory(walletId, startDate, endDate);

        // Then
        assertNotNull(result);
        assertEquals(mockTransactions, result);
    }
}

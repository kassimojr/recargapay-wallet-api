package com.recargapay.wallet.adapter.dtos;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("TransactionHistoryResponseDTO Tests")
class TransactionHistoryResponseDTOTest {

    @Test
    @DisplayName("Should create TransactionHistoryResponseDTO with all fields using constructor")
    void shouldCreateTransactionHistoryResponseDTOWithAllFieldsUsingConstructor() {
        // Given
        UUID walletId = UUID.randomUUID();
        String userName = "John Doe";
        BigDecimal currentBalance = BigDecimal.valueOf(500.00);
        
        TransactionDTO transaction1 = TransactionDTO.builder()
                .id(UUID.randomUUID())
                .amount(BigDecimal.valueOf(100.00))
                .type("DEPOSIT")
                .timestamp(LocalDateTime.now())
                .build();
                
        TransactionDTO transaction2 = TransactionDTO.builder()
                .id(UUID.randomUUID())
                .amount(BigDecimal.valueOf(50.00))
                .type("WITHDRAW")
                .timestamp(LocalDateTime.now())
                .build();
                
        List<TransactionDTO> transactions = Arrays.asList(transaction1, transaction2);

        // When
        TransactionHistoryResponseDTO response = new TransactionHistoryResponseDTO(
                walletId, userName, currentBalance, transactions);

        // Then
        assertThat(response.getWalletId()).isEqualTo(walletId);
        assertThat(response.getUserName()).isEqualTo(userName);
        assertThat(response.getCurrentBalance()).isEqualTo(currentBalance);
        assertThat(response.getTransactions()).hasSize(2);
        assertThat(response.getTransactions()).containsExactly(transaction1, transaction2);
        assertThat(response.getTotalTransactions()).isEqualTo(2);
    }

    @Test
    @DisplayName("Should create TransactionHistoryResponseDTO with empty constructor")
    void shouldCreateTransactionHistoryResponseDTOWithEmptyConstructor() {
        // When
        TransactionHistoryResponseDTO response = new TransactionHistoryResponseDTO();

        // Then
        assertThat(response.getWalletId()).isNull();
        assertThat(response.getUserName()).isNull();
        assertThat(response.getCurrentBalance()).isNull();
        assertThat(response.getTransactions()).isNull();
        assertThat(response.getTotalTransactions()).isNull();
    }

    @Test
    @DisplayName("Should create TransactionHistoryResponseDTO with empty transactions")
    void shouldCreateTransactionHistoryResponseDTOWithEmptyTransactions() {
        // Given
        UUID walletId = UUID.randomUUID();
        String userName = "Jane Doe";
        BigDecimal currentBalance = BigDecimal.valueOf(100.00);
        List<TransactionDTO> transactions = Arrays.asList();

        // When
        TransactionHistoryResponseDTO response = new TransactionHistoryResponseDTO(
                walletId, userName, currentBalance, transactions);

        // Then
        assertThat(response.getWalletId()).isEqualTo(walletId);
        assertThat(response.getUserName()).isEqualTo(userName);
        assertThat(response.getCurrentBalance()).isEqualTo(currentBalance);
        assertThat(response.getTransactions()).isEmpty();
        assertThat(response.getTotalTransactions()).isEqualTo(0);
    }

    @Test
    @DisplayName("Should create TransactionHistoryResponseDTO with null values")
    void shouldCreateTransactionHistoryResponseDTOWithNullValues() {
        // When
        TransactionHistoryResponseDTO response = new TransactionHistoryResponseDTO(
                null, null, null, null);

        // Then
        assertThat(response.getWalletId()).isNull();
        assertThat(response.getUserName()).isNull();
        assertThat(response.getCurrentBalance()).isNull();
        assertThat(response.getTransactions()).isNull();
        assertThat(response.getTotalTransactions()).isEqualTo(0);
    }

    @Test
    @DisplayName("Should handle setters correctly")
    void shouldHandleSettersCorrectly() {
        // Given
        TransactionHistoryResponseDTO response = new TransactionHistoryResponseDTO();
        UUID walletId = UUID.randomUUID();
        String userName = "Test User";
        BigDecimal currentBalance = BigDecimal.valueOf(250.00);
        
        TransactionDTO transaction = TransactionDTO.builder()
                .id(UUID.randomUUID())
                .amount(BigDecimal.valueOf(75.00))
                .type("TRANSFER")
                .timestamp(LocalDateTime.now())
                .build();
                
        List<TransactionDTO> transactions = Arrays.asList(transaction);

        // When
        response.setWalletId(walletId);
        response.setUserName(userName);
        response.setCurrentBalance(currentBalance);
        response.setTransactions(transactions);

        // Then
        assertThat(response.getWalletId()).isEqualTo(walletId);
        assertThat(response.getUserName()).isEqualTo(userName);
        assertThat(response.getCurrentBalance()).isEqualTo(currentBalance);
        assertThat(response.getTransactions()).hasSize(1);
        assertThat(response.getTransactions()).containsExactly(transaction);
        assertThat(response.getTotalTransactions()).isEqualTo(1);
    }

    @Test
    @DisplayName("Should handle manual totalTransactions setter")
    void shouldHandleManualTotalTransactionsSetter() {
        // Given
        TransactionHistoryResponseDTO response = new TransactionHistoryResponseDTO();
        Integer totalTransactions = 100;

        // When
        response.setTotalTransactions(totalTransactions);

        // Then
        assertThat(response.getTotalTransactions()).isEqualTo(totalTransactions);
    }

    @Test
    @DisplayName("Should handle large transaction lists")
    void shouldHandleLargeTransactionLists() {
        // Given
        UUID walletId = UUID.randomUUID();
        String userName = "Power User";
        BigDecimal currentBalance = BigDecimal.valueOf(1000.00);
        
        List<TransactionDTO> transactions = Arrays.asList(
                TransactionDTO.builder().id(UUID.randomUUID()).amount(BigDecimal.valueOf(100)).type("DEPOSIT").build(),
                TransactionDTO.builder().id(UUID.randomUUID()).amount(BigDecimal.valueOf(200)).type("DEPOSIT").build(),
                TransactionDTO.builder().id(UUID.randomUUID()).amount(BigDecimal.valueOf(50)).type("WITHDRAW").build(),
                TransactionDTO.builder().id(UUID.randomUUID()).amount(BigDecimal.valueOf(75)).type("WITHDRAW").build(),
                TransactionDTO.builder().id(UUID.randomUUID()).amount(BigDecimal.valueOf(300)).type("TRANSFER").build()
        );

        // When
        TransactionHistoryResponseDTO response = new TransactionHistoryResponseDTO(
                walletId, userName, currentBalance, transactions);

        // Then
        assertThat(response.getWalletId()).isEqualTo(walletId);
        assertThat(response.getUserName()).isEqualTo(userName);
        assertThat(response.getCurrentBalance()).isEqualTo(currentBalance);
        assertThat(response.getTransactions()).hasSize(5);
        assertThat(response.getTotalTransactions()).isEqualTo(5);
    }

    @Test
    @DisplayName("Should handle zero balance")
    void shouldHandleZeroBalance() {
        // Given
        UUID walletId = UUID.randomUUID();
        String userName = "Zero Balance User";
        BigDecimal currentBalance = BigDecimal.ZERO;
        List<TransactionDTO> transactions = Arrays.asList();

        // When
        TransactionHistoryResponseDTO response = new TransactionHistoryResponseDTO(
                walletId, userName, currentBalance, transactions);

        // Then
        assertThat(response.getCurrentBalance()).isEqualTo(BigDecimal.ZERO);
        assertThat(response.getTotalTransactions()).isEqualTo(0);
    }

    @Test
    @DisplayName("Should handle negative balance")
    void shouldHandleNegativeBalance() {
        // Given
        UUID walletId = UUID.randomUUID();
        String userName = "Negative Balance User";
        BigDecimal currentBalance = BigDecimal.valueOf(-50.00);
        List<TransactionDTO> transactions = Arrays.asList();

        // When
        TransactionHistoryResponseDTO response = new TransactionHistoryResponseDTO(
                walletId, userName, currentBalance, transactions);

        // Then
        assertThat(response.getCurrentBalance()).isEqualTo(BigDecimal.valueOf(-50.00));
        assertThat(response.getCurrentBalance()).isNegative();
    }

    @Test
    @DisplayName("Should set totalTransactions to zero when transactions list is null")
    void shouldSetTotalTransactionsToZeroWhenTransactionsListIsNull() {
        // Given
        TransactionHistoryResponseDTO dto = new TransactionHistoryResponseDTO();
        
        // When
        dto.setTransactions(null);
        
        // Then
        assertThat(dto.getTransactions()).isNull();
        assertThat(dto.getTotalTransactions()).isEqualTo(0);
    }

    @Test
    @DisplayName("Should calculate totalTransactions correctly when setting transactions list")
    void shouldCalculateTotalTransactionsCorrectlyWhenSettingTransactionsList() {
        // Given
        TransactionHistoryResponseDTO dto = new TransactionHistoryResponseDTO();
        List<TransactionDTO> transactions = Arrays.asList(
            TransactionDTO.builder().id(UUID.randomUUID()).amount(BigDecimal.valueOf(100)).type("DEPOSIT").build(),
            TransactionDTO.builder().id(UUID.randomUUID()).amount(BigDecimal.valueOf(50)).type("WITHDRAW").build()
        );
        
        // When
        dto.setTransactions(transactions);
        
        // Then
        assertThat(dto.getTransactions()).isEqualTo(transactions);
        assertThat(dto.getTotalTransactions()).isEqualTo(2);
    }
}

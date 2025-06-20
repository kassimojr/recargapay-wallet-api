package com.recargapay.wallet.adapter.dtos;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/**
 * DTO para resposta de histórico de transações
 */
public class TransactionHistoryResponseDTO {
    private UUID walletId;
    private String userName;
    private BigDecimal currentBalance;
    private List<TransactionDTO> transactions;
    private Integer totalTransactions;

    public TransactionHistoryResponseDTO() {
    }

    public TransactionHistoryResponseDTO(UUID walletId, String userName, BigDecimal currentBalance, 
                                        List<TransactionDTO> transactions) {
        this.walletId = walletId;
        this.userName = userName;
        this.currentBalance = currentBalance;
        this.transactions = transactions;
        this.totalTransactions = transactions != null ? transactions.size() : 0;
    }

    public UUID getWalletId() {
        return walletId;
    }

    public void setWalletId(UUID walletId) {
        this.walletId = walletId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public BigDecimal getCurrentBalance() {
        return currentBalance;
    }

    public void setCurrentBalance(BigDecimal currentBalance) {
        this.currentBalance = currentBalance;
    }

    public List<TransactionDTO> getTransactions() {
        return transactions;
    }

    public void setTransactions(List<TransactionDTO> transactions) {
        this.transactions = transactions;
        this.totalTransactions = transactions != null ? transactions.size() : 0;
    }

    public Integer getTotalTransactions() {
        return totalTransactions;
    }

    public void setTotalTransactions(Integer totalTransactions) {
        this.totalTransactions = totalTransactions;
    }
}

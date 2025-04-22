package com.recargapay.wallet.core.domain;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public class Transaction {
    private UUID id;
    private UUID walletId;
    private BigDecimal amount;
    private TransactionType type;
    private LocalDateTime timestamp;

    public Transaction(UUID id, UUID walletId, BigDecimal amount, TransactionType type, LocalDateTime timestamp) {
        this.id = id;
        this.walletId = walletId;
        this.amount = amount;
        this.type = type;
        this.timestamp = timestamp;
    }

    public UUID getId() {
        return id;
    }

    public UUID getWalletId() {
        return walletId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public TransactionType getType() {
        return type;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }
}
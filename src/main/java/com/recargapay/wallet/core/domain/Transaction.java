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
    private UUID relatedUserId;

    public Transaction(UUID id,
                       UUID walletId,
                       BigDecimal amount,
                       TransactionType type,
                       LocalDateTime timestamp,
                       UUID relatedUserId) {
        this.id = id;
        this.walletId = walletId;
        this.amount = amount;
        this.type = type;
        this.timestamp = timestamp;
        this.relatedUserId = relatedUserId;
    }

    public Transaction() {}

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getWalletId() {
        return walletId;
    }

    public void setWalletId(UUID walletId) {
        this.walletId = walletId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public TransactionType getType() {
        return type;
    }

    public void setType(TransactionType type) {
        this.type = type;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public UUID getRelatedUserId() {
        return relatedUserId;
    }

    public void setRelatedUserId(UUID relatedUserId) {
        this.relatedUserId = relatedUserId;
    }
}
package io.github.dev.recargapaywalletapi.adapter.dtos;

import io.github.dev.recargapaywalletapi.core.domain.TransactionType;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
public class TransactionDTO {
    private UUID id;
    private UUID walletId;
    private BigDecimal amount;
    private TransactionType type;
    private LocalDateTime timestamp;
}

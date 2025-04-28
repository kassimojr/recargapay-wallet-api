package com.recargapay.wallet.adapter.dtos;

import com.recargapay.wallet.core.domain.TransactionType;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionDTO {
    private UUID id;
    private UUID walletId;
    private BigDecimal amount;
    private String type;
    private LocalDateTime timestamp;
    private UUID relatedUserId;
}

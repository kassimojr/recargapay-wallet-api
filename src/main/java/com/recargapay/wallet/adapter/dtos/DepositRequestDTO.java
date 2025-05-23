package com.recargapay.wallet.adapter.dtos;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DepositRequestDTO {
    @NotNull
    private UUID walletId;
    @NotNull
    @Positive
    private BigDecimal amount;
}

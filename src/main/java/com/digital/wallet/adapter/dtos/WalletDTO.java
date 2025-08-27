package com.digital.wallet.adapter.dtos;

import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WalletDTO {
    private UUID id;
    private UUID userId;
    private String userName;
    private BigDecimal balance;
}

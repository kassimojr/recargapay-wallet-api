package com.recargapay.wallet.adapter.dtos;

import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateWalletRequestDTO {
    @NotNull
    private UUID userId;
}

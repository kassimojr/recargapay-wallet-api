package com.digital.wallet.adapter.dtos;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransferResponseDTO {
    private TransactionDTO sourceTransaction;
    private TransactionDTO destinationTransaction;
}

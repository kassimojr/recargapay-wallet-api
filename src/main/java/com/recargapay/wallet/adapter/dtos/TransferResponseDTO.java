package com.recargapay.wallet.adapter.dtos;

import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransferResponseDTO {
    private TransactionDTO sourceTransaction;
    private TransactionDTO destinationTransaction;
}

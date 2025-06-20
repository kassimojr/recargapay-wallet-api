package com.recargapay.wallet.core.ports.in;

import com.recargapay.wallet.core.domain.Transaction;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public interface TransferFundsUseCase {
    /**
     * Transfers an amount from one wallet to another
     *
     * @param fromWalletId source of the transfer, must not be null
     * @param toWalletId   destination of the transfer, must not be null
     * @param amount       amount to be transferred, must be greater than zero
     * @return a list containing the two transactions created (outbound and inbound)
     */
    List<Transaction> transfer(UUID fromWalletId, UUID toWalletId, BigDecimal amount);
}

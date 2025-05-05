package com.recargapay.wallet.core.ports.in;

import java.math.BigDecimal;
import java.util.UUID;

public interface TransferFundsUseCase {
    void transfer(UUID fromWalletId, UUID toWalletId, BigDecimal amount);
}

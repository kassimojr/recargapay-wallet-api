package com.recargapay.wallet.core.ports.in;

import com.recargapay.wallet.core.domain.Transaction;
import java.math.BigDecimal;
import java.util.UUID;

public interface WithdrawUseCase {
    Transaction withdraw(UUID walletId, BigDecimal amount);
}

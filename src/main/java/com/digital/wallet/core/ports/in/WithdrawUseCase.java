package com.digital.wallet.core.ports.in;

import com.digital.wallet.core.domain.Transaction;
import java.math.BigDecimal;
import java.util.UUID;

public interface WithdrawUseCase {
    Transaction withdraw(UUID walletId, BigDecimal amount);
}

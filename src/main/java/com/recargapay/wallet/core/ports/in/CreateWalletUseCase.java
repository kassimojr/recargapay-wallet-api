package com.recargapay.wallet.core.ports.in;

import com.recargapay.wallet.core.domain.Wallet;

public interface CreateWalletUseCase {
    Wallet create(Wallet wallet);
}

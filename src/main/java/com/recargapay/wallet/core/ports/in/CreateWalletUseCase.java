package com.recargapay.wallet.core.ports.in;

import com.recargapay.wallet.core.domain.Wallet;

import java.util.UUID;

public interface CreateWalletUseCase {
    Wallet create(Wallet wallet);
    Wallet findById(UUID walletId);
    Wallet findBalanceAt(UUID walletId, String at);
}

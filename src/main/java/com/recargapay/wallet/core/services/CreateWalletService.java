package com.recargapay.wallet.core.services;

import com.recargapay.wallet.core.domain.Wallet;
import com.recargapay.wallet.core.ports.in.CreateWalletUseCase;
import com.recargapay.wallet.core.ports.out.WalletRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CreateWalletService implements CreateWalletUseCase {
    private final WalletRepository walletRepository;

    public CreateWalletService(WalletRepository walletRepository) {
        this.walletRepository = walletRepository;
    }

    @Override
    @Transactional
    public Wallet create(Wallet wallet) {
        // Aqui pode-se adicionar validações de negócio, se necessário
        return walletRepository.save(wallet);
    }
}

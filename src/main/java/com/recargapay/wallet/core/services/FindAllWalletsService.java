package com.recargapay.wallet.core.services;

import com.recargapay.wallet.core.domain.Wallet;
import com.recargapay.wallet.core.ports.in.FindAllWalletsUseCase;
import com.recargapay.wallet.core.ports.out.WalletRepository;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class FindAllWalletsService implements FindAllWalletsUseCase {
    private final WalletRepository walletRepository;

    public FindAllWalletsService(WalletRepository walletRepository) {
        this.walletRepository = walletRepository;
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "wallet-list", key = "'all'")
    public List<Wallet> findAll() {
        return walletRepository.findAll();
    }
}

package com.digital.wallet.core.ports.out;

import com.digital.wallet.core.domain.Wallet;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface WalletRepository {
    Optional<Wallet> findById(UUID walletId);
    Optional<Wallet> findByUserId(UUID userId);
    void update(Wallet wallet);
    Wallet save(Wallet wallet);
    void delete(UUID walletId);
    List<Wallet> findAll();
}

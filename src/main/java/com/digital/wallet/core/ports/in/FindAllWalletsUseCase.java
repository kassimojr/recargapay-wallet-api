package com.digital.wallet.core.ports.in;

import com.digital.wallet.core.domain.Wallet;
import java.util.List;

public interface FindAllWalletsUseCase {
    /**
     * Finds all wallets in the system
     * 
     * @return List of found wallets
     */
    List<Wallet> findAll();
}

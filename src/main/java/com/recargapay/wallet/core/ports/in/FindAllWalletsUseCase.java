package com.recargapay.wallet.core.ports.in;

import com.recargapay.wallet.core.domain.Wallet;
import java.util.List;

public interface FindAllWalletsUseCase {
    /**
     * Finds all wallets in the system
     * 
     * @return List of found wallets
     */
    List<Wallet> findAll();
}

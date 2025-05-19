package com.recargapay.wallet.core.ports.in;

import com.recargapay.wallet.core.domain.Wallet;
import java.util.List;

public interface FindAllWalletsUseCase {
    /**
     * Encontra todas as carteiras no sistema
     * 
     * @return Lista de carteiras encontradas
     */
    List<Wallet> findAll();
}

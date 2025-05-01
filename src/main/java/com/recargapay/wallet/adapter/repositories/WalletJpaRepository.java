package com.recargapay.wallet.adapter.repositories;

import com.recargapay.wallet.adapter.entities.WalletEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface WalletJpaRepository extends JpaRepository<WalletEntity, UUID> {
}

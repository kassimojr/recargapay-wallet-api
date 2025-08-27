package com.digital.wallet.adapter.repositories;

import com.digital.wallet.adapter.entities.WalletEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

public interface WalletJpaRepository extends JpaRepository<WalletEntity, UUID> {
    Optional<WalletEntity> findByUser_Id(UUID userId);
}

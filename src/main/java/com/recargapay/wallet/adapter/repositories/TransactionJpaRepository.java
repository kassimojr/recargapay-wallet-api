package com.recargapay.wallet.adapter.repositories;

import com.recargapay.wallet.adapter.entities.TransactionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface TransactionJpaRepository extends JpaRepository<TransactionEntity, UUID> {
}

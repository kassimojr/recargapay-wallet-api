package com.recargapay.wallet.adapter.repositories;

import com.recargapay.wallet.adapter.entities.TransactionEntity;
import com.recargapay.wallet.adapter.entities.WalletEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface TransactionJpaRepository extends JpaRepository<TransactionEntity, UUID> {
    /**
     * Finds all transactions of a wallet up to a specific date, ordered by timestamp
     */
    List<TransactionEntity> findByWalletIdAndTimestampLessThanEqualOrderByTimestampAsc(UUID walletId, LocalDateTime endDateTime);
    
    /**
     * Finds all transactions of a wallet
     */
    List<TransactionEntity> findByWalletOrderByTimestampAsc(WalletEntity wallet);
    
    /**
     * Finds all transactions of a wallet by wallet ID, ordered by timestamp
     */
    List<TransactionEntity> findByWalletIdOrderByTimestampAsc(UUID walletId);
    
    /**
     * Finds all transactions of a wallet within a specific period, ordered by timestamp
     */
    List<TransactionEntity> findByWalletIdAndTimestampBetweenOrderByTimestampAsc(UUID walletId, LocalDateTime startDateTime, LocalDateTime endDateTime);
}

package com.recargapay.wallet.core.ports.out;

import com.recargapay.wallet.core.domain.Transaction;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TransactionRepository {
    void save(Transaction transaction);
    Transaction saveAndReturn(Transaction transaction);
    void delete(UUID transactionId);
    Optional<Transaction> findById(UUID transactionId);
    List<Transaction> findAll();
    
    /**
     * Busca todas as transações de uma carteira até uma data específica
     * @param walletId ID da carteira
     * @param endDateTime Data/hora limite para as transações (inclusive)
     * @return Lista de transações da carteira até a data especificada
     */
    List<Transaction> findByWalletIdAndTimestampLessThanEqual(UUID walletId, LocalDateTime endDateTime);
    
    /**
     * Busca todas as transações de uma carteira
     * @param walletId ID da carteira
     * @return Lista de todas as transações da carteira
     */
    List<Transaction> findByWalletId(UUID walletId);
    
    /**
     * Busca todas as transações de uma carteira dentro de um período
     * @param walletId ID da carteira
     * @param startDateTime Data/hora inicial do período (inclusive)
     * @param endDateTime Data/hora final do período (inclusive)
     * @return Lista de transações da carteira dentro do período especificado
     */
    List<Transaction> findByWalletIdAndTimestampBetween(UUID walletId, LocalDateTime startDateTime, LocalDateTime endDateTime);
}

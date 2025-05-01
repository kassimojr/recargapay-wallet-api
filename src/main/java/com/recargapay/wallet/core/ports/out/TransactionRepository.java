package com.recargapay.wallet.core.ports.out;

import com.recargapay.wallet.core.domain.Transaction;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TransactionRepository {
    void save(Transaction transaction);
    Transaction saveAndReturn(Transaction transaction);
    void delete(UUID transactionId);
    Optional<Transaction> findById(UUID transactionId);
    List<Transaction> findAll();
}

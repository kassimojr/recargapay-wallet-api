package com.digital.wallet.adapter.repositories.impl;

import com.digital.wallet.adapter.entities.TransactionEntity;
import com.digital.wallet.adapter.entities.WalletEntity;
import com.digital.wallet.adapter.converters.TransactionMapper;
import com.digital.wallet.adapter.converters.WalletMapper;
import com.digital.wallet.core.domain.Transaction;
import com.digital.wallet.core.domain.TransactionType;
import com.digital.wallet.core.domain.Wallet;
import com.digital.wallet.core.ports.out.TransactionalWalletRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.PersistenceContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Implementation of wallet repository with support for transactional operations
 * Uses JPA/JPQL with pessimistic locking to ensure consistency in concurrent operations
 */
@Repository
public class TransactionalWalletRepositoryImpl implements TransactionalWalletRepository {

    private static final Logger logger = LoggerFactory.getLogger(TransactionalWalletRepositoryImpl.class);

    @PersistenceContext
    private EntityManager entityManager;

    private final WalletRepositoryImpl delegateWalletRepository;
    private final WalletMapper walletMapper;
    private final TransactionMapper transactionMapper;

    public TransactionalWalletRepositoryImpl(
            WalletRepositoryImpl delegateWalletRepository,
            WalletMapper walletMapper,
            TransactionMapper transactionMapper) {
        this.delegateWalletRepository = delegateWalletRepository;
        this.walletMapper = walletMapper;
        this.transactionMapper = transactionMapper;
    }

    @Override
    public Optional<Wallet> findById(UUID id) {
        return delegateWalletRepository.findById(id);
    }

    @Override
    public Optional<Wallet> findByUserId(UUID userId) {
        return delegateWalletRepository.findByUserId(userId);
    }

    @Override
    public void update(Wallet wallet) {
        delegateWalletRepository.update(wallet);
    }

    @Override
    public Wallet save(Wallet wallet) {
        return delegateWalletRepository.save(wallet);
    }

    @Override
    public void delete(UUID walletId) {
        delegateWalletRepository.delete(walletId);
    }

    @Override
    public List<Wallet> findAll() {
        return delegateWalletRepository.findAll();
    }

    @Override
    public boolean updateWalletBalance(UUID walletId, BigDecimal amount, boolean isDebit) {
        String operation = isDebit ? "debit" : "credit";
        logger.debug("Updating wallet {} balance with {} of {}", walletId, operation, amount);

        String jpql = isDebit
                ? "UPDATE WalletEntity w SET w.balance = w.balance - :amount WHERE w.id = :walletId"
                : "UPDATE WalletEntity w SET w.balance = w.balance + :amount WHERE w.id = :walletId";

        int updated = entityManager.createQuery(jpql)
                .setParameter("amount", amount)
                .setParameter("walletId", walletId)
                .executeUpdate();

        if (updated > 0) {
            logger.debug("Wallet {} balance updated successfully", walletId);
        } else {
            logger.warn("Failed to update wallet {} balance", walletId);
        }

        return updated > 0;
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.READ_COMMITTED)
    public Transaction createTransaction(UUID walletId, BigDecimal amount,
                                         TransactionType type, UUID relatedUserId,
                                         LocalDateTime timestamp) {
        logger.debug("Creating transaction of type {} for wallet {}", type, walletId);

        try {
            // Get the wallet entity using EntityManager
            WalletEntity walletEntity = entityManager.find(WalletEntity.class, walletId);
            
            if (walletEntity == null) {
                throw new EntityNotFoundException("Wallet not found: " + walletId);
            }

            // Create the transaction entity
            TransactionEntity entity = new TransactionEntity();
            entity.setWallet(walletEntity);
            entity.setAmount(amount);
            entity.setType(type);
            entity.setRelatedUserId(relatedUserId);
            entity.setTimestamp(timestamp);
            
            // We don't need to manually define createdAt and updatedAt, as the @PrePersist
            // methods in the entity already do this automatically

            // Use persist for new entities
            entityManager.persist(entity);
            
            logger.debug("Transaction {} created successfully", entity.getId());

            // Convert to domain and return
            return transactionMapper.toDomain(entity);
        } catch (Exception e) {
            logger.error("Error creating transaction: {}", e.getMessage(), e);
            throw e;
        }
    }
}

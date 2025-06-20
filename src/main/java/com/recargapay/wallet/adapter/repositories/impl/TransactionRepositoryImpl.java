package com.recargapay.wallet.adapter.repositories.impl;

import com.recargapay.wallet.core.domain.Transaction;
import com.recargapay.wallet.core.ports.out.TransactionRepository;
import com.recargapay.wallet.adapter.entities.TransactionEntity;
import com.recargapay.wallet.adapter.entities.WalletEntity;
import com.recargapay.wallet.adapter.repositories.TransactionJpaRepository;
import com.recargapay.wallet.adapter.repositories.WalletJpaRepository;
import com.recargapay.wallet.adapter.converters.TransactionMapper;
import com.recargapay.wallet.core.exceptions.WalletNotFoundException;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class TransactionRepositoryImpl implements TransactionRepository {

    private final TransactionJpaRepository jpaRepository;
    private final WalletJpaRepository walletJpaRepository;
    private final TransactionMapper transactionMapper;

    public TransactionRepositoryImpl(TransactionJpaRepository jpaRepository, 
                                    WalletJpaRepository walletJpaRepository,
                                    TransactionMapper transactionMapper) {
        this.jpaRepository = jpaRepository;
        this.walletJpaRepository = walletJpaRepository;
        this.transactionMapper = transactionMapper;
    }

    @Override
    public void save(Transaction transaction) {
        TransactionEntity entity = toEntity(transaction);
        jpaRepository.save(entity);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public Transaction saveAndReturn(Transaction transaction) {
        // Criamos uma nova entidade em vez de buscar e atualizar uma existente
        TransactionEntity entity = createNewEntity(transaction);
        TransactionEntity saved = jpaRepository.save(entity);
        return transactionMapper.toDomain(saved);
    }

    @Override
    public void delete(UUID transactionId) {
        jpaRepository.deleteById(transactionId);
    }

    @Override
    public Optional<Transaction> findById(UUID transactionId) {
        return jpaRepository.findById(transactionId).map(transactionMapper::toDomain);
    }

    @Override
    public List<Transaction> findAll() {
        return jpaRepository.findAll().stream().map(transactionMapper::toDomain).toList();
    }
    
    @Override
    public List<Transaction> findByWalletIdAndTimestampLessThanEqual(UUID walletId, LocalDateTime endDateTime) {
        List<TransactionEntity> entities = jpaRepository.findByWalletIdAndTimestampLessThanEqualOrderByTimestampAsc(
            walletId, endDateTime);
        return entities.stream().map(transactionMapper::toDomain).toList();
    }
    
    @Override
    public List<Transaction> findByWalletId(UUID walletId) {
        List<TransactionEntity> entities = jpaRepository.findByWalletIdOrderByTimestampAsc(walletId);
        return entities.stream().map(transactionMapper::toDomain).toList();
    }
    
    @Override
    public List<Transaction> findByWalletIdAndTimestampBetween(UUID walletId, LocalDateTime startDateTime, LocalDateTime endDateTime) {
        List<TransactionEntity> entities = jpaRepository.findByWalletIdAndTimestampBetweenOrderByTimestampAsc(
            walletId, startDateTime, endDateTime);
        return entities.stream().map(transactionMapper::toDomain).toList();
    }

    private TransactionEntity toEntity(Transaction domain) {
        if (domain == null) {
            return null;
        }
        
        TransactionEntity entity = new TransactionEntity();
        entity.setId(domain.getId());
        entity.setAmount(domain.getAmount());
        
        // Buscar a wallet pelo ID para estabelecer a relação
        if (domain.getWalletId() != null) {
            WalletEntity walletEntity = walletJpaRepository.findById(domain.getWalletId())
                .orElseThrow(() -> new WalletNotFoundException("Wallet not found: " + domain.getWalletId()));
            entity.setWallet(walletEntity);
        }
        
        entity.setType(domain.getType());
        entity.setTimestamp(domain.getTimestamp());
        entity.setRelatedUserId(domain.getRelatedUserId());
        
        return entity;
    }
    
    // Método alternativo que sempre cria uma nova entidade
    private TransactionEntity createNewEntity(Transaction domain) {
        if (domain == null) {
            return null;
        }
        
        TransactionEntity entity = new TransactionEntity();
        entity.setId(domain.getId());
        entity.setAmount(domain.getAmount());
        
        // Buscar a wallet pelo ID para estabelecer a relação
        if (domain.getWalletId() != null) {
            WalletEntity walletEntity = walletJpaRepository.findById(domain.getWalletId())
                .orElseThrow(() -> new WalletNotFoundException("Wallet not found: " + domain.getWalletId()));
            entity.setWallet(walletEntity);
        }
        
        entity.setType(domain.getType());
        entity.setTimestamp(domain.getTimestamp());
        entity.setRelatedUserId(domain.getRelatedUserId());
        
        return entity;
    }
}

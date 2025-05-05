package com.recargapay.wallet.adapter.repositories.impl;

import com.recargapay.wallet.core.domain.Transaction;
import com.recargapay.wallet.core.ports.out.TransactionRepository;
import com.recargapay.wallet.adapter.entities.TransactionEntity;
import com.recargapay.wallet.adapter.repositories.TransactionJpaRepository;
import com.recargapay.wallet.adapter.converters.TransactionMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class TransactionRepositoryImpl implements TransactionRepository {

    private final TransactionJpaRepository jpaRepository;
    private final TransactionMapper transactionMapper;

    public TransactionRepositoryImpl(TransactionJpaRepository jpaRepository, TransactionMapper transactionMapper) {
        this.jpaRepository = jpaRepository;
        this.transactionMapper = transactionMapper;
    }

    @Override
    public void save(Transaction transaction) {
        jpaRepository.save(transactionMapper.toEntity(transaction));
    }

    @Override
    public Transaction saveAndReturn(Transaction transaction) {
        TransactionEntity entity = transactionMapper.toEntity(transaction);
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

    
}

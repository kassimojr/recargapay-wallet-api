package com.digital.wallet.adapter.repositories.impl;

import com.digital.wallet.core.domain.Wallet;
import com.digital.wallet.core.ports.out.WalletRepository;
import com.digital.wallet.adapter.entities.WalletEntity;
import com.digital.wallet.adapter.repositories.WalletJpaRepository;
import com.digital.wallet.adapter.repositories.UserJpaRepository;
import com.digital.wallet.adapter.converters.WalletMapper;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@Primary
public class WalletRepositoryImpl implements WalletRepository {

    private final WalletJpaRepository jpaRepository;
    private final UserJpaRepository userJpaRepository;
    private final WalletMapper walletMapper;

    public WalletRepositoryImpl(WalletJpaRepository jpaRepository, UserJpaRepository userJpaRepository, WalletMapper walletMapper) {
        this.jpaRepository = jpaRepository;
        this.userJpaRepository = userJpaRepository;
        this.walletMapper = walletMapper;
    }

    @Override
    public Optional<Wallet> findById(UUID walletId) {
        return jpaRepository.findById(walletId).map(walletMapper::toDomain);
    }

    @Override
    public Optional<Wallet> findByUserId(UUID userId) {
        return jpaRepository.findByUser_Id(userId).map(walletMapper::toDomain);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public void update(Wallet wallet) {
        // Use the method that forces creation of a new entity
        jpaRepository.save(createNewEntity(wallet));
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public Wallet save(Wallet wallet) {
        // Use the method that forces creation of a new entity
        WalletEntity entity = createNewEntity(wallet);
        WalletEntity saved = jpaRepository.save(entity);
        return walletMapper.toDomain(saved);
    }

    @Override
    public void delete(UUID walletId) {
        jpaRepository.deleteById(walletId);
    }

    @Override
    public List<Wallet> findAll() {
        return jpaRepository.findAll().stream().map(walletMapper::toDomain).toList();
    }

    // Conversion using ModelMapper + UserJpaRepository to ensure correct relationship
    private WalletEntity toEntity(Wallet wallet) {
        if (wallet == null) return null;
        WalletEntity entity = walletMapper.toEntity(wallet);
        if (wallet.getUserId() != null) {
            entity.setUser(userJpaRepository.findById(wallet.getUserId()).orElse(null));
        }
        return entity;
    }
    
    // Method that always creates a new entity to avoid version problems
    private WalletEntity createNewEntity(Wallet wallet) {
        if (wallet == null) return null;
        
        // We create a new entity and fill it manually
        WalletEntity entity = new WalletEntity();
        entity.setId(wallet.getId());
        entity.setBalance(wallet.getBalance());
        
        if (wallet.getUserId() != null) {
            entity.setUser(userJpaRepository.findById(wallet.getUserId()).orElse(null));
        }
        
        return entity;
    }
}

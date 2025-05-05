package com.recargapay.wallet.adapter.repositories.impl;

import com.recargapay.wallet.core.domain.Wallet;
import com.recargapay.wallet.core.ports.out.WalletRepository;
import com.recargapay.wallet.adapter.entities.WalletEntity;
import com.recargapay.wallet.adapter.repositories.WalletJpaRepository;
import com.recargapay.wallet.adapter.repositories.UserJpaRepository;
import com.recargapay.wallet.adapter.converters.WalletMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
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
    public void update(Wallet wallet) {
        jpaRepository.save(toEntity(wallet));
    }

    @Override
    public Wallet save(Wallet wallet) {
        WalletEntity entity = toEntity(wallet);
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

    // Conversão usando ModelMapper + UserJpaRepository para garantir o relacionamento correto
    private WalletEntity toEntity(Wallet wallet) {
        if (wallet == null) return null;
        WalletEntity entity = walletMapper.toEntity(wallet);
        if (wallet.getUserId() != null) {
            entity.setUser(userJpaRepository.findById(wallet.getUserId()).orElse(null));
        }
        return entity;
    }
}

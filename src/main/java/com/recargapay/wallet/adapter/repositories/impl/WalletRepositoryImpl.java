package com.recargapay.wallet.adapter.repositories.impl;

import com.recargapay.wallet.core.domain.Wallet;
import com.recargapay.wallet.core.ports.out.WalletRepository;
import com.recargapay.wallet.adapter.entities.WalletEntity;
import com.recargapay.wallet.adapter.repositories.WalletJpaRepository;
import com.recargapay.wallet.adapter.repositories.UserJpaRepository;
import com.recargapay.wallet.adapter.converters.WalletMapper;
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
        // Usa o método que força a criação de uma nova entidade
        jpaRepository.save(createNewEntity(wallet));
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public Wallet save(Wallet wallet) {
        // Usa o método que força a criação de uma nova entidade
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

    // Conversão usando ModelMapper + UserJpaRepository para garantir o relacionamento correto
    private WalletEntity toEntity(Wallet wallet) {
        if (wallet == null) return null;
        WalletEntity entity = walletMapper.toEntity(wallet);
        if (wallet.getUserId() != null) {
            entity.setUser(userJpaRepository.findById(wallet.getUserId()).orElse(null));
        }
        return entity;
    }
    
    // Método que sempre cria uma nova entidade para evitar problemas de versão
    private WalletEntity createNewEntity(Wallet wallet) {
        if (wallet == null) return null;
        
        // Criamos uma nova entidade e preenchemos manualmente
        WalletEntity entity = new WalletEntity();
        entity.setId(wallet.getId());
        entity.setBalance(wallet.getBalance());
        
        if (wallet.getUserId() != null) {
            entity.setUser(userJpaRepository.findById(wallet.getUserId()).orElse(null));
        }
        
        return entity;
    }
}

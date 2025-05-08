package com.recargapay.wallet.adapter.repositories.impl;

import com.recargapay.wallet.adapter.converters.WalletMapper;
import com.recargapay.wallet.adapter.entities.WalletEntity;
import com.recargapay.wallet.adapter.repositories.WalletJpaRepository;
import com.recargapay.wallet.adapter.repositories.UserJpaRepository;
import com.recargapay.wallet.core.domain.Wallet;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class WalletRepositoryImplTest {
    @Mock
    private WalletJpaRepository jpaRepository;
    @Mock
    private UserJpaRepository userJpaRepository;
    @Mock
    private WalletMapper walletMapper;

    @InjectMocks
    private WalletRepositoryImpl walletRepository;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void findById_shouldReturnWallet() {
        UUID walletId = UUID.randomUUID();
        WalletEntity entity = new WalletEntity();
        Wallet wallet = new Wallet(walletId, UUID.randomUUID(), BigDecimal.TEN);
        when(jpaRepository.findById(walletId)).thenReturn(Optional.of(entity));
        when(walletMapper.toDomain(entity)).thenReturn(wallet);
        Optional<Wallet> result = walletRepository.findById(walletId);
        assertTrue(result.isPresent());
        assertEquals(wallet, result.get());
    }

    @Test
    void findById_shouldReturnEmpty() {
        UUID walletId = UUID.randomUUID();
        when(jpaRepository.findById(walletId)).thenReturn(Optional.empty());
        Optional<Wallet> result = walletRepository.findById(walletId);
        assertFalse(result.isPresent());
    }

    @Test
    void save_shouldReturnWallet() {
        Wallet wallet = new Wallet(UUID.randomUUID(), UUID.randomUUID(), BigDecimal.ONE);
        WalletEntity entity = new WalletEntity();
        WalletEntity savedEntity = new WalletEntity();
        Wallet expected = new Wallet(wallet.getId(), wallet.getUserId(), wallet.getBalance());
        when(walletMapper.toEntity(wallet)).thenReturn(entity);
        when(jpaRepository.save(entity)).thenReturn(savedEntity);
        when(walletMapper.toDomain(savedEntity)).thenReturn(expected);
        Wallet result = walletRepository.save(wallet);
        assertEquals(expected, result);
    }

    @Test
    void update_shouldCallSave() {
        Wallet wallet = new Wallet(UUID.randomUUID(), UUID.randomUUID(), BigDecimal.ONE);
        WalletEntity entity = new WalletEntity();
        when(walletMapper.toEntity(wallet)).thenReturn(entity);
        walletRepository.update(wallet);
        verify(jpaRepository, times(1)).save(entity);
    }

    @Test
    void delete_shouldCallDeleteById() {
        UUID walletId = UUID.randomUUID();
        walletRepository.delete(walletId);
        verify(jpaRepository, times(1)).deleteById(walletId);
    }

    @Test
    void findAll_shouldReturnWalletList() {
        WalletEntity entity = new WalletEntity();
        Wallet wallet = new Wallet(UUID.randomUUID(), UUID.randomUUID(), BigDecimal.ONE);
        when(jpaRepository.findAll()).thenReturn(List.of(entity));
        when(walletMapper.toDomain(entity)).thenReturn(wallet);
        List<Wallet> result = walletRepository.findAll();
        assertEquals(1, result.size());
        assertEquals(wallet, result.get(0));
    }
}

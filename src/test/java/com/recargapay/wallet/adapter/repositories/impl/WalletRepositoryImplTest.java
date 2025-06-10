package com.recargapay.wallet.adapter.repositories.impl;

import com.recargapay.wallet.adapter.converters.WalletMapper;
import com.recargapay.wallet.adapter.entities.WalletEntity;
import com.recargapay.wallet.adapter.repositories.WalletJpaRepository;
import com.recargapay.wallet.adapter.repositories.UserJpaRepository;
import com.recargapay.wallet.core.domain.Wallet;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
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
    
    private Wallet wallet;
    private WalletEntity savedEntity;
    private UUID walletId;
    private UUID userId;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        
        walletId = UUID.randomUUID();
        userId = UUID.randomUUID();
        wallet = new Wallet(walletId, userId, BigDecimal.ONE);
        savedEntity = new WalletEntity();
        savedEntity.setId(walletId);
        savedEntity.setBalance(BigDecimal.ONE);
        
        // Mock para toEntity (usado pelo método original)
        when(walletMapper.toEntity(any(Wallet.class))).thenReturn(new WalletEntity());
        
        // Mock para toDomain
        when(walletMapper.toDomain(any(WalletEntity.class))).thenReturn(wallet);
        
        // Mock para save
        when(jpaRepository.save(any(WalletEntity.class))).thenReturn(savedEntity);
    }

    @Test
    void findById_shouldReturnWallet() {
        WalletEntity entity = new WalletEntity();
        when(jpaRepository.findById(walletId)).thenReturn(Optional.of(entity));
        Optional<Wallet> result = walletRepository.findById(walletId);
        assertTrue(result.isPresent());
        assertEquals(wallet, result.get());
    }

    @Test
    void findById_shouldReturnEmpty() {
        when(jpaRepository.findById(walletId)).thenReturn(Optional.empty());
        Optional<Wallet> result = walletRepository.findById(walletId);
        assertFalse(result.isPresent());
    }

    @Test
    void save_shouldReturnWallet() {
        // Captura o argumento passado para save para verificar se os campos foram definidos corretamente
        ArgumentCaptor<WalletEntity> entityCaptor = ArgumentCaptor.forClass(WalletEntity.class);
        
        Wallet result = walletRepository.save(wallet);
        
        // Verifica que save foi chamado com algum argumento do tipo WalletEntity
        verify(jpaRepository).save(entityCaptor.capture());
        
        // Verifica que os campos importantes foram definidos corretamente
        WalletEntity capturedEntity = entityCaptor.getValue();
        assertEquals(walletId, capturedEntity.getId());
        assertEquals(BigDecimal.ONE, capturedEntity.getBalance());
        
        // Verifica que o resultado é o esperado
        assertEquals(wallet, result);
    }

    @Test
    void update_shouldCallSave() {
        // Captura o argumento passado para save para verificar se os campos foram definidos corretamente
        ArgumentCaptor<WalletEntity> entityCaptor = ArgumentCaptor.forClass(WalletEntity.class);
        
        walletRepository.update(wallet);
        
        // Verifica que save foi chamado
        verify(jpaRepository).save(entityCaptor.capture());
        
        // Verifica que os campos importantes foram definidos corretamente
        WalletEntity capturedEntity = entityCaptor.getValue();
        assertEquals(walletId, capturedEntity.getId());
        assertEquals(BigDecimal.ONE, capturedEntity.getBalance());
    }

    @Test
    void delete_shouldCallDeleteById() {
        walletRepository.delete(walletId);
        verify(jpaRepository, times(1)).deleteById(walletId);
    }

    @Test
    void findAll_shouldReturnWalletList() {
        WalletEntity entity = new WalletEntity();
        when(jpaRepository.findAll()).thenReturn(List.of(entity));
        List<Wallet> result = walletRepository.findAll();
        assertEquals(1, result.size());
        assertEquals(wallet, result.get(0));
    }
}

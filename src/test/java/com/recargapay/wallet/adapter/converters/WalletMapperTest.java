package com.recargapay.wallet.adapter.converters;

import com.recargapay.wallet.adapter.dtos.CreateWalletRequestDTO;
import com.recargapay.wallet.adapter.dtos.DepositRequestDTO;
import com.recargapay.wallet.adapter.dtos.WithdrawRequestDTO;
import com.recargapay.wallet.adapter.dtos.WalletDTO;
import com.recargapay.wallet.adapter.entities.WalletEntity;
import com.recargapay.wallet.adapter.entities.UserEntity;
import com.recargapay.wallet.core.domain.Wallet;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

class WalletMapperTest {
    private WalletMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new WalletMapper();
    }

    @Test
    void toDomainFromEntity_shouldMapFields() {
        WalletEntity entity = new WalletEntity();
        UUID id = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        BigDecimal balance = new BigDecimal("100.00");
        
        entity.setId(id);
        UserEntity userEntity = new UserEntity();
        userEntity.setId(userId);
        entity.setUser(userEntity);
        entity.setBalance(balance);
        
        Wallet wallet = mapper.toDomain(entity);
        
        assertNotNull(wallet);
        assertEquals(id, wallet.getId());
        assertEquals(userId, wallet.getUserId());
        assertEquals(balance, wallet.getBalance());
    }
    
    @Test
    void toEntity_shouldMapFields() {
        Wallet domain = new Wallet();
        UUID id = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        BigDecimal balance = new BigDecimal("100.00");
        
        domain.setId(id);
        domain.setUserId(userId);
        domain.setBalance(balance);
        
        WalletEntity entity = mapper.toEntity(domain);
        
        assertNotNull(entity);
        assertEquals(id, entity.getId());
        assertNotNull(entity.getUser());
        assertEquals(userId, entity.getUser().getId());
        assertEquals(balance, entity.getBalance());
    }

    @Test
    void toDomainFromCreateWalletRequestDTO_shouldMapFields() {
        CreateWalletRequestDTO dto = new CreateWalletRequestDTO();
        UUID userId = UUID.randomUUID();
        dto.setUserId(userId);
        
        Wallet wallet = mapper.toDomain(dto);
        
        assertNotNull(wallet);
        assertEquals(userId, wallet.getUserId());
        assertEquals(BigDecimal.ZERO, wallet.getBalance());
    }

    @Test
    void toDomainFromDepositRequestDTO_shouldMapFields() {
        DepositRequestDTO dto = new DepositRequestDTO();
        UUID walletId = UUID.randomUUID();
        dto.setWalletId(walletId);
        
        Wallet wallet = mapper.toDomain(dto);
        
        assertNotNull(wallet);
        assertEquals(walletId, wallet.getId());
    }

    @Test
    void toDomainFromWithdrawRequestDTO_shouldMapFields() {
        WithdrawRequestDTO dto = new WithdrawRequestDTO();
        UUID walletId = UUID.randomUUID();
        dto.setWalletId(walletId);
        
        Wallet wallet = mapper.toDomain(dto);
        
        assertNotNull(wallet);
        assertEquals(walletId, wallet.getId());
    }

    @Test
    void toDTO_shouldMapFields() {
        Wallet domain = new Wallet();
        UUID id = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        BigDecimal balance = new BigDecimal("100.00");
        
        domain.setId(id);
        domain.setUserId(userId);
        domain.setBalance(balance);
        
        WalletDTO dto = mapper.toDTO(domain);
        
        assertNotNull(dto);
        assertEquals(id, dto.getId());
        assertEquals(userId, dto.getUserId());
        assertEquals(balance, dto.getBalance());
    }

    @Test
    void toDTOList_shouldMapList() {
        Wallet wallet1 = new Wallet();
        wallet1.setId(UUID.randomUUID());
        
        Wallet wallet2 = new Wallet();
        wallet2.setId(UUID.randomUUID());
        
        List<Wallet> wallets = List.of(wallet1, wallet2);
        List<WalletDTO> dtos = mapper.toDTOList(wallets);
        
        assertEquals(2, dtos.size());
        assertEquals(wallet1.getId(), dtos.get(0).getId());
        assertEquals(wallet2.getId(), dtos.get(1).getId());
    }

    @Test
    void toDTOList_withNull_shouldReturnEmptyList() {
        List<WalletDTO> dtos = mapper.toDTOList(null);
        assertNotNull(dtos);
        assertTrue(dtos.isEmpty());
    }
    
    @Test
    void toDomain_withNullEntity_shouldReturnNull() {
        assertNull(mapper.toDomain((WalletEntity) null));
    }
    
    @Test
    void toEntity_withNullDomain_shouldReturnNull() {
        assertNull(mapper.toEntity(null));
    }
    
    @Test
    void toDTO_withNullDomain_shouldReturnNull() {
        assertNull(mapper.toDTO(null));
    }
}

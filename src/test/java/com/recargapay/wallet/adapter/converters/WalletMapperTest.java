package com.recargapay.wallet.adapter.converters;

import com.recargapay.wallet.adapter.dtos.CreateWalletRequestDTO;
import com.recargapay.wallet.adapter.dtos.DepositRequestDTO;
import com.recargapay.wallet.adapter.dtos.WalletDTO;
import com.recargapay.wallet.adapter.dtos.WithdrawRequestDTO;
import com.recargapay.wallet.adapter.entities.UserEntity;
import com.recargapay.wallet.adapter.entities.WalletEntity;
import com.recargapay.wallet.core.domain.User;
import com.recargapay.wallet.core.domain.Wallet;
import com.recargapay.wallet.core.ports.out.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class WalletMapperTest {

    @Mock
    private UserRepository userRepository;
    
    private WalletMapper walletMapper;
    private UUID id;
    private UUID userId;
    private BigDecimal balance;
    private String userName;

    @BeforeEach
    void setUp() {
        walletMapper = new WalletMapper(userRepository);
        
        id = UUID.randomUUID();
        userId = UUID.randomUUID();
        balance = new BigDecimal("100.00");
        userName = "Test User";
        
        // Configure mock user repository
        User mockUser = new User();
        mockUser.setId(userId);
        mockUser.setName(userName);
        when(userRepository.findById(userId)).thenReturn(Optional.of(mockUser));
    }

    @Test
    void toDomainFromEntity_shouldMapFields() {
        WalletEntity entity = new WalletEntity();
        entity.setId(id);
        UserEntity userEntity = new UserEntity();
        userEntity.setId(userId);
        entity.setUser(userEntity);
        entity.setBalance(balance);
        
        Wallet wallet = walletMapper.toDomain(entity);
        
        assertNotNull(wallet);
        assertEquals(id, wallet.getId());
        assertEquals(userId, wallet.getUserId());
        assertEquals(balance, wallet.getBalance());
    }
    
    @Test
    void toEntity_shouldMapFields() {
        Wallet domain = new Wallet();
        domain.setId(id);
        domain.setUserId(userId);
        domain.setBalance(balance);
        
        WalletEntity entity = walletMapper.toEntity(domain);
        
        assertNotNull(entity);
        assertEquals(id, entity.getId());
        assertNotNull(entity.getUser());
        assertEquals(userId, entity.getUser().getId());
        assertEquals(balance, entity.getBalance());
    }

    @Test
    void toDomainFromCreateWalletRequestDTO_shouldMapFields() {
        CreateWalletRequestDTO dto = new CreateWalletRequestDTO();
        dto.setUserId(userId);
        
        Wallet wallet = walletMapper.toDomain(dto);
        
        assertNotNull(wallet);
        assertEquals(userId, wallet.getUserId());
        assertEquals(BigDecimal.ZERO, wallet.getBalance());
    }

    @Test
    void toDomainFromDepositRequestDTO_shouldMapFields() {
        DepositRequestDTO dto = new DepositRequestDTO();
        dto.setWalletId(id);
        
        Wallet wallet = walletMapper.toDomain(dto);
        
        assertNotNull(wallet);
        assertEquals(id, wallet.getId());
    }

    @Test
    void toDomainFromWithdrawRequestDTO_shouldMapFields() {
        WithdrawRequestDTO dto = new WithdrawRequestDTO();
        dto.setWalletId(id);
        
        Wallet wallet = walletMapper.toDomain(dto);
        
        assertNotNull(wallet);
        assertEquals(id, wallet.getId());
    }

    @Test
    void toDTO_shouldMapFields() {
        Wallet domain = new Wallet();
        domain.setId(id);
        domain.setUserId(userId);
        domain.setBalance(balance);
        
        WalletDTO dto = walletMapper.toDTO(domain);
        
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
        List<WalletDTO> dtos = walletMapper.toDTOList(wallets);
        
        assertEquals(2, dtos.size());
        assertEquals(wallet1.getId(), dtos.get(0).getId());
        assertEquals(wallet2.getId(), dtos.get(1).getId());
    }

    @Test
    void toDTOList_withNull_shouldReturnEmptyList() {
        List<WalletDTO> dtos = walletMapper.toDTOList(null);
        assertNotNull(dtos);
        assertTrue(dtos.isEmpty());
    }
    
    @Test
    void toDomain_withNullEntity_shouldReturnNull() {
        assertNull(walletMapper.toDomain((WalletEntity) null));
    }
    
    @Test
    void toEntity_withNullDomain_shouldReturnNull() {
        assertNull(walletMapper.toEntity(null));
    }
    
    @Test
    void toDTO_withNullDomain_shouldReturnNull() {
        assertNull(walletMapper.toDTO(null));
    }
}

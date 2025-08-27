package com.digital.wallet.adapter.converters;

import com.digital.wallet.adapter.dtos.TransactionDTO;
import com.digital.wallet.adapter.entities.TransactionEntity;
import com.digital.wallet.adapter.entities.WalletEntity;
import com.digital.wallet.core.domain.Transaction;
import com.digital.wallet.core.domain.TransactionType;
import com.digital.wallet.core.domain.User;
import com.digital.wallet.core.ports.out.UserRepository;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class TransactionMapperTest {

    @Mock
    private UserRepository userRepository;
    
    private TransactionMapper transactionMapper;
    
    private UUID walletId;
    private UUID relatedUserId;
    private User relatedUser;
    
    @BeforeEach
    void setUp() {
        relatedUserId = UUID.randomUUID();
        walletId = UUID.randomUUID();
        
        relatedUser = new User();
        relatedUser.setId(relatedUserId);
        relatedUser.setName("Jane Smith");
        
        transactionMapper = new TransactionMapper(userRepository);
        
        when(userRepository.findById(relatedUserId)).thenReturn(Optional.of(relatedUser));
    }
    
    @Test
    void toDomain_shouldMapFields() {
        // Arrange
        TransactionEntity entity = new TransactionEntity();
        entity.setId(UUID.randomUUID());
        entity.setAmount(new BigDecimal("100.00"));
        entity.setTimestamp(LocalDateTime.now());
        entity.setType(TransactionType.DEPOSIT);
        
        WalletEntity walletEntity = new WalletEntity();
        walletEntity.setId(walletId);
        entity.setWallet(walletEntity);
        
        entity.setRelatedUserId(relatedUserId);
        
        // Act
        Transaction transaction = transactionMapper.toDomain(entity);
        
        // Assert
        assertNotNull(transaction);
        assertEquals(entity.getId(), transaction.getId());
        assertEquals(entity.getAmount(), transaction.getAmount());
        assertEquals(walletId, transaction.getWalletId());
        assertEquals(entity.getType(), transaction.getType());
        assertEquals(entity.getTimestamp(), transaction.getTimestamp());
        assertEquals(entity.getRelatedUserId(), transaction.getRelatedUserId());
    }
    
    @Test
    void toDTO_shouldMapFields() {
        // Arrange
        Transaction transaction = new Transaction();
        transaction.setId(UUID.randomUUID());
        transaction.setAmount(new BigDecimal("100.00"));
        transaction.setTimestamp(LocalDateTime.now());
        transaction.setType(TransactionType.DEPOSIT);
        transaction.setWalletId(walletId);
        transaction.setRelatedUserId(relatedUserId);
        
        // Act
        TransactionDTO dto = transactionMapper.toDTO(transaction);
        
        // Assert
        assertNotNull(dto);
        assertEquals(transaction.getId(), dto.getId());
        assertEquals(transaction.getAmount(), dto.getAmount());
        assertEquals(transaction.getTimestamp(), dto.getTimestamp());
        assertEquals(transaction.getType().toString(), dto.getType());
        assertEquals(transaction.getWalletId(), dto.getWalletId());
        assertEquals(transaction.getRelatedUserId(), dto.getRelatedUserId());
        assertEquals(relatedUser.getName(), dto.getRelatedUserName());
    }
    
    @Test
    void toDTO_withNullDomain_shouldReturnNull() {
        assertNull(transactionMapper.toDTO(null));
    }
    
    @Test
    void toDomain_withNullEntity_shouldReturnNull() {
        assertNull(transactionMapper.toDomain(null));
    }
    
    @Test
    void toDTOList_shouldMapList() {
        // Arrange
        Transaction tx1 = new Transaction();
        tx1.setId(UUID.randomUUID());
        tx1.setWalletId(walletId);
        tx1.setAmount(BigDecimal.ONE);
        tx1.setType(TransactionType.DEPOSIT);
        tx1.setTimestamp(LocalDateTime.now());
        tx1.setRelatedUserId(relatedUserId);
        
        Transaction tx2 = new Transaction();
        tx2.setId(UUID.randomUUID());
        tx2.setWalletId(walletId);
        tx2.setAmount(BigDecimal.TEN);
        tx2.setType(TransactionType.WITHDRAW);
        tx2.setTimestamp(LocalDateTime.now());
        tx2.setRelatedUserId(relatedUserId);
        
        List<Transaction> transactions = List.of(tx1, tx2);
        
        // Act
        List<TransactionDTO> dtos = transactionMapper.toDTOList(transactions);
        
        // Assert
        assertEquals(2, dtos.size());
        assertEquals(tx1.getId(), dtos.get(0).getId());
        assertEquals(tx2.getId(), dtos.get(1).getId());
    }
    
    @Test
    void toDTOList_withNull_shouldReturnEmptyList() {
        List<TransactionDTO> dtos = transactionMapper.toDTOList(null);
        assertNotNull(dtos);
        assertTrue(dtos.isEmpty());
    }
}

package com.recargapay.wallet.adapter.converters;

import com.recargapay.wallet.adapter.dtos.WalletDTO;
import com.recargapay.wallet.adapter.dtos.CreateWalletRequestDTO;
import com.recargapay.wallet.adapter.dtos.DepositRequestDTO;
import com.recargapay.wallet.adapter.dtos.WithdrawRequestDTO;

import com.recargapay.wallet.adapter.entities.WalletEntity;
import com.recargapay.wallet.adapter.entities.UserEntity;
import com.recargapay.wallet.core.domain.User;
import com.recargapay.wallet.core.domain.Wallet;
import com.recargapay.wallet.core.ports.out.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@Component
public class WalletMapper {
    
    private final UserRepository userRepository;
    private static final Logger logger = LoggerFactory.getLogger(WalletMapper.class);

    public WalletMapper(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public Wallet toDomain(WalletEntity entity) {
        if (entity == null) {
            return null;
        }
        
        Wallet wallet = new Wallet();
        wallet.setId(entity.getId());
        wallet.setUserId(entity.getUser() != null ? entity.getUser().getId() : null);
        wallet.setBalance(entity.getBalance());
        wallet.setCreatedAt(entity.getCreatedAt());
        wallet.setUpdatedAt(entity.getUpdatedAt());
        
        return wallet;
    }

    public WalletEntity toEntity(Wallet domain) {
        if (domain == null) {
            return null;
        }
        
        WalletEntity entity = new WalletEntity();
        entity.setId(domain.getId());
        
        // To maintain only the user ID, we need to create a UserEntity with the ID
        if (domain.getUserId() != null) {
            UserEntity userEntity = new UserEntity();
            userEntity.setId(domain.getUserId());
            entity.setUser(userEntity);
        }
        
        entity.setBalance(domain.getBalance());
        entity.setCreatedAt(domain.getCreatedAt());
        entity.setUpdatedAt(domain.getUpdatedAt());
        
        return entity;
    }

    public WalletDTO toDTO(Wallet domain) {
        if (domain == null) {
            return null;
        }
        
        WalletDTO dto = new WalletDTO();
        dto.setId(domain.getId());
        dto.setUserId(domain.getUserId());
        dto.setBalance(domain.getBalance());
        
        // Fetch and add the wallet owner's username
        if (domain.getUserId() != null) {
            try {
                Optional<User> user = userRepository.findById(domain.getUserId());
                user.ifPresent(u -> dto.setUserName(u.getName()));
            } catch (Exception e) {
                logger.warn("Error fetching user information: {}", e.getMessage());
                // In case of error, we continue without the username
            }
        }
        
        return dto;
    }

    public List<WalletDTO> toDTOList(List<Wallet> domains) {
        return Objects.requireNonNullElse(domains, List.<Wallet>of()).stream().map(this::toDTO).toList();
    }

    // Convert CreateWalletRequestDTO to Wallet domain
    public Wallet toDomain(CreateWalletRequestDTO dto) {
        if (dto == null) return null;
        Wallet wallet = new Wallet();
        wallet.setUserId(dto.getUserId());
        wallet.setBalance(java.math.BigDecimal.ZERO); // Default initial balance
        return wallet;
    }

    // Convert DepositRequestDTO to Wallet domain (walletId only)
    public Wallet toDomain(DepositRequestDTO dto) {
        if (dto == null) return null;
        Wallet wallet = new Wallet();
        wallet.setId(dto.getWalletId());
        return wallet;
    }

    // Convert WithdrawRequestDTO to Wallet domain (walletId only)
    public Wallet toDomain(WithdrawRequestDTO dto) {
        if (dto == null) return null;
        Wallet wallet = new Wallet();
        wallet.setId(dto.getWalletId());
        return wallet;
    }
    
    /**
     * Returns the username by ID
     * @param userId User ID
     * @return Username or empty string if not found
     */
    public String getUserName(UUID userId) {
        if (userId == null) {
            return "";
        }
        
        try {
            Optional<User> user = userRepository.findById(userId);
            return user.map(User::getName).orElse("");
        } catch (Exception e) {
            logger.warn("Error fetching user information: {}", e.getMessage());
            return "";
        }
    }
}

package com.recargapay.wallet.adapter.converters;

import com.recargapay.wallet.adapter.dtos.WalletDTO;
import com.recargapay.wallet.adapter.dtos.CreateWalletRequestDTO;
import com.recargapay.wallet.adapter.dtos.DepositRequestDTO;
import com.recargapay.wallet.adapter.dtos.WithdrawRequestDTO;

import com.recargapay.wallet.adapter.entities.WalletEntity;
import com.recargapay.wallet.adapter.entities.UserEntity;
import com.recargapay.wallet.core.domain.Wallet;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;

@Component
public class WalletMapper {

    public WalletMapper() {
        // Construtor default
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
        
        // Para manter apenas o ID do usuário, precisamos criar uma UserEntity com o ID
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
        
        return dto;
    }

    public List<WalletDTO> toDTOList(List<Wallet> domains) {
        return Objects.requireNonNullElse(domains, List.<Wallet>of()).stream().map(this::toDTO).toList();
    }

    // Conversão de CreateWalletRequestDTO para domínio Wallet
    public Wallet toDomain(CreateWalletRequestDTO dto) {
        if (dto == null) return null;
        Wallet wallet = new Wallet();
        wallet.setUserId(dto.getUserId());
        wallet.setBalance(java.math.BigDecimal.ZERO); // Saldo inicial padrão
        return wallet;
    }

    // Conversão de DepositRequestDTO para domínio Wallet (apenas walletId)
    public Wallet toDomain(DepositRequestDTO dto) {
        if (dto == null) return null;
        Wallet wallet = new Wallet();
        wallet.setId(dto.getWalletId());
        return wallet;
    }

    // Conversão de WithdrawRequestDTO para domínio Wallet (apenas walletId)
    public Wallet toDomain(WithdrawRequestDTO dto) {
        if (dto == null) return null;
        Wallet wallet = new Wallet();
        wallet.setId(dto.getWalletId());
        return wallet;
    }
}

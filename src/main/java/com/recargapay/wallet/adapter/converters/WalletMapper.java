package com.recargapay.wallet.adapter.converters;

import com.recargapay.wallet.adapter.dtos.WalletDTO;
import com.recargapay.wallet.adapter.dtos.CreateWalletRequestDTO;
import com.recargapay.wallet.adapter.dtos.DepositRequestDTO;
import com.recargapay.wallet.adapter.dtos.WithdrawRequestDTO;

import com.recargapay.wallet.adapter.entities.WalletEntity;
import com.recargapay.wallet.core.domain.Wallet;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;

@Component
public class WalletMapper {
    private final ModelMapper mapper;

    public WalletMapper(ModelMapper mapper) {
        this.mapper = mapper;
    }

    public Wallet toDomain(WalletEntity entity) {
        return MapperUtils.mapIfNotNull(entity, e -> mapper.map(e, Wallet.class));
    }

    public WalletEntity toEntity(Wallet domain) {
        return MapperUtils.mapIfNotNull(domain, d -> mapper.map(d, WalletEntity.class));
    }

    public WalletDTO toDTO(Wallet domain) {
        return MapperUtils.mapIfNotNull(domain, d -> mapper.map(d, WalletDTO.class));
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


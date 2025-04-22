package com.recargapay.wallet.adapter.converters;

import com.recargapay.wallet.adapter.dtos.WalletDTO;
import com.recargapay.wallet.adapter.entities.WalletEntity;
import com.recargapay.wallet.core.domain.Wallet;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

@Component
public class WalletMapper {
    private final ModelMapper mapper;

    public WalletMapper(ModelMapper mapper) {
        this.mapper = mapper;
    }

    public Wallet toDomain(WalletEntity entity) {
        return mapper.map(entity, Wallet.class);
    }

    public WalletEntity toEntity(Wallet domain) {
        return mapper.map(domain, WalletEntity.class);
    }

    public WalletDTO toDTO(Wallet domain) {
        return mapper.map(domain, WalletDTO.class);
    }
}


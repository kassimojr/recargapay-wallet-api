package io.github.dev.recargapaywalletapi.adapter.converters;

import io.github.dev.recargapaywalletapi.adapter.dtos.WalletDTO;
import io.github.dev.recargapaywalletapi.adapter.entities.WalletEntity;
import io.github.dev.recargapaywalletapi.core.domain.Wallet;
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


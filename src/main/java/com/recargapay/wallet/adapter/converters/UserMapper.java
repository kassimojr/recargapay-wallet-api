package com.recargapay.wallet.adapter.converters;

import com.recargapay.wallet.adapter.dtos.UserDTO;
import com.recargapay.wallet.adapter.entities.UserEntity;
import com.recargapay.wallet.core.domain.User;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {
    private final ModelMapper mapper;

    public UserMapper(ModelMapper mapper) {
        this.mapper = mapper;
    }

    public User toDomain(UserEntity entity) {
        return mapper.map(entity, User.class);
    }

    public UserEntity toEntity(User domain) {
        return mapper.map(domain, UserEntity.class);
    }

    public UserDTO toDTO(User domain) {
        return mapper.map(domain, UserDTO.class);
    }
}

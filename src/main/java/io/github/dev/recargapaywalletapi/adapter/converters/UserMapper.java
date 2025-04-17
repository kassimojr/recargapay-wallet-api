package io.github.dev.recargapaywalletapi.adapter.converters;

import io.github.dev.recargapaywalletapi.adapter.dtos.UserDTO;
import io.github.dev.recargapaywalletapi.adapter.entities.UserEntity;
import io.github.dev.recargapaywalletapi.core.domain.User;
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

package com.recargapay.wallet.adapter.converters;

import com.recargapay.wallet.adapter.dtos.CreateUserRequestDTO;
import com.recargapay.wallet.adapter.dtos.UserDTO;
import com.recargapay.wallet.adapter.entities.UserEntity;
import com.recargapay.wallet.core.domain.User;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;

@Component
public class UserMapper {
    private final ModelMapper mapper;

    public UserMapper(ModelMapper mapper) {
        this.mapper = mapper;
    }

    public User toDomain(UserEntity entity) {
        return MapperUtils.mapIfNotNull(entity, e -> mapper.map(e, User.class));
    }

    public UserEntity toEntity(User domain) {
        return MapperUtils.mapIfNotNull(domain, d -> mapper.map(d, UserEntity.class));
    }

    public UserDTO toDTO(User domain) {
        return MapperUtils.mapIfNotNull(domain, d -> mapper.map(d, UserDTO.class));
    }
    
    public List<UserDTO> toDTOList(List<User> domains) {
        return Objects.requireNonNullElse(domains, List.<User>of()).stream().map(this::toDTO).toList();
    }
    
    // Convert CreateUserRequestDTO to User domain
    public User toDomain(CreateUserRequestDTO dto) {
        if (dto == null) return null;
        User user = new User();
        user.setName(dto.getName());
        user.setEmail(dto.getEmail());
        return user;
    }
}

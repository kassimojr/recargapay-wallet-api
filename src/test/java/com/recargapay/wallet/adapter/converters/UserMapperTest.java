package com.recargapay.wallet.adapter.converters;

import com.recargapay.wallet.adapter.dtos.UserDTO;
import com.recargapay.wallet.adapter.entities.UserEntity;
import com.recargapay.wallet.core.domain.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.modelmapper.ModelMapper;
import java.util.List;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

class UserMapperTest {
    private UserMapper mapper;
    private ModelMapper modelMapper;

    @BeforeEach
    void setUp() {
        modelMapper = new ModelMapper();
        mapper = new UserMapper(modelMapper);
    }

    @Test
    void toDomain_shouldMapFields() {
        UserEntity entity = new UserEntity();
        UUID id = UUID.randomUUID();
        String email = "test@example.com";
        String name = "Test User";
        entity.setId(id);
        entity.setEmail(email);
        entity.setName(name);
        User user = mapper.toDomain(entity);
        assertNotNull(user);
        assertEquals(id, user.getId());
        assertEquals(email, user.getEmail());
        assertEquals(name, user.getName());
    }

    @Test
    void toEntity_shouldMapFields() {
        UUID id = UUID.randomUUID();
        String email = "test@example.com";
        String name = "Test User";
        User user = new User(id, email, name);
        UserEntity entity = mapper.toEntity(user);
        assertNotNull(entity);
        assertEquals(id, entity.getId());
        assertEquals(email, entity.getEmail());
        assertEquals(name, entity.getName());
    }

    @Test
    void toDTO_shouldMapFields() {
        UUID id = UUID.randomUUID();
        String email = "test@example.com";
        String name = "Test User";
        User user = new User(id, email, name);
        UserDTO dto = mapper.toDTO(user);
        assertNotNull(dto);
        assertEquals(id, dto.getId());
        assertEquals(email, dto.getEmail());
        assertEquals(name, dto.getName());
    }

    @Test
    void toDTOList_shouldMapList() {
        UUID id = UUID.randomUUID();
        String email = "test@example.com";
        String name = "Test User";
        User user = new User(id, email, name);
        List<UserDTO> dtos = mapper.toDTOList(List.of(user));
        assertNotNull(dtos);
        assertEquals(1, dtos.size());
        assertEquals(id, dtos.get(0).getId());
        assertEquals(email, dtos.get(0).getEmail());
        assertEquals(name, dtos.get(0).getName());
    }

    @Test
    void toDTOList_shouldReturnEmptyListForNull() {
        List<UserDTO> dtos = mapper.toDTOList(null);
        assertNotNull(dtos);
        assertTrue(dtos.isEmpty());
    }
}

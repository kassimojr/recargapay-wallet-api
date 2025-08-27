package com.digital.wallet.adapter.converters;

import com.digital.wallet.adapter.dtos.CreateUserRequestDTO;
import com.digital.wallet.adapter.dtos.UserDTO;
import com.digital.wallet.adapter.entities.UserEntity;
import com.digital.wallet.core.domain.User;
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
    void toDomain_shouldReturnNullForNullEntity() {
        User user = mapper.toDomain((UserEntity) null);
        assertNull(user);
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
    void toEntity_shouldReturnNullForNullDomain() {
        UserEntity entity = mapper.toEntity(null);
        assertNull(entity);
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
    void toDTO_shouldReturnNullForNullDomain() {
        UserDTO dto = mapper.toDTO(null);
        assertNull(dto);
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
    
    @Test
    void toDomain_ShouldMapCreateRequestDTOToUser_WhenValidRequestProvided() {
        // Arrange
        String name = "Test User";
        String email = "test@example.com";
        CreateUserRequestDTO dto = new CreateUserRequestDTO(name, email);
        
        // Act
        User user = mapper.toDomain(dto);
        
        // Assert
        assertNotNull(user);
        assertEquals(name, user.getName());
        assertEquals(email, user.getEmail());
        assertNull(user.getId()); // ID should be null as it's a new user
    }
    
    @Test
    void toDomain_ShouldReturnNull_WhenCreateRequestDTOIsNull() {
        // Act
        User user = mapper.toDomain((CreateUserRequestDTO) null);
        
        // Assert
        assertNull(user);
    }
}

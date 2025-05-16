package com.recargapay.wallet.adapter.controllers.v1;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.recargapay.wallet.adapter.converters.UserMapper;
import com.recargapay.wallet.adapter.dtos.CreateUserRequestDTO;
import com.recargapay.wallet.adapter.dtos.UserDTO;
import com.recargapay.wallet.core.domain.User;
import com.recargapay.wallet.core.exceptions.DuplicatedResourceException;
import com.recargapay.wallet.core.ports.in.CreateUserUseCase;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
@Import({TestSecurityConfig.class, UserControllerTest.TestConfig.class})
class UserControllerTest {

    // Config interna para fornecer os mocks necessários
    static class TestConfig {
        @Bean
        public CreateUserUseCase createUserUseCase() {
            return mock(CreateUserUseCase.class);
        }

        @Bean
        public UserMapper userMapper() {
            return mock(UserMapper.class);
        }
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CreateUserUseCase createUserUseCase;

    @Autowired
    private UserMapper userMapper;

    @Test
    @WithMockUser(roles = "ADMIN")
    void create_ShouldReturnCreatedUser_WhenValidRequestProvided() throws Exception {
        // Arrange
        CreateUserRequestDTO requestDTO = new CreateUserRequestDTO("Test User", "test@example.com");
        
        User user = new User();
        user.setName("Test User");
        user.setEmail("test@example.com");
        
        User createdUser = new User();
        createdUser.setId(UUID.randomUUID());
        createdUser.setName("Test User");
        createdUser.setEmail("test@example.com");
        
        UserDTO userDTO = new UserDTO(createdUser.getId(), createdUser.getEmail(), createdUser.getName());

        when(userMapper.toDomain(any(CreateUserRequestDTO.class))).thenReturn(user);
        when(createUserUseCase.create(any(User.class))).thenReturn(createdUser);
        when(userMapper.toDTO(any(User.class))).thenReturn(userDTO);

        // Act & Assert
        mockMvc.perform(post("/api/v1/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(createdUser.getId().toString()))
                .andExpect(jsonPath("$.name").value("Test User"))
                .andExpect(jsonPath("$.email").value("test@example.com"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void create_ShouldReturnBadRequest_WhenInvalidRequestProvided() throws Exception {
        // Arrange
        CreateUserRequestDTO requestDTO = new CreateUserRequestDTO("", ""); // Nome e email vazios

        // Act & Assert
        mockMvc.perform(post("/api/v1/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void create_ShouldReturnConflict_WhenEmailAlreadyExists() throws Exception {
        // Arrange
        CreateUserRequestDTO requestDTO = new CreateUserRequestDTO("Test User", "existing@example.com");
        
        User user = new User();
        user.setName("Test User");
        user.setEmail("existing@example.com");

        when(userMapper.toDomain(any(CreateUserRequestDTO.class))).thenReturn(user);
        when(createUserUseCase.create(any(User.class))).thenThrow(new DuplicatedResourceException("Email já está em uso"));

        // Act & Assert
        mockMvc.perform(post("/api/v1/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isConflict());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void findById_ShouldReturnUser_WhenUserExists() throws Exception {
        // Arrange
        UUID userId = UUID.randomUUID();
        
        User user = new User();
        user.setId(userId);
        user.setName("Test User");
        user.setEmail("test@example.com");
        
        UserDTO userDTO = new UserDTO(userId, "test@example.com", "Test User");

        when(createUserUseCase.findById(userId)).thenReturn(Optional.of(user));
        when(userMapper.toDTO(user)).thenReturn(userDTO);

        // Act & Assert
        mockMvc.perform(get("/api/v1/users/" + userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userId.toString()))
                .andExpect(jsonPath("$.name").value("Test User"))
                .andExpect(jsonPath("$.email").value("test@example.com"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void findById_ShouldReturnNotFound_WhenUserDoesNotExist() throws Exception {
        // Arrange
        UUID userId = UUID.randomUUID();
        when(createUserUseCase.findById(userId)).thenReturn(Optional.empty());

        // Act & Assert
        mockMvc.perform(get("/api/v1/users/" + userId))
                .andExpect(status().isNotFound());
    }
}

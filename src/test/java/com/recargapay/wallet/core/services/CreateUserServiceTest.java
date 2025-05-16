package com.recargapay.wallet.core.services;

import com.recargapay.wallet.core.domain.User;
import com.recargapay.wallet.core.exceptions.DuplicatedResourceException;
import com.recargapay.wallet.core.ports.out.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CreateUserServiceTest {

    @Mock
    private UserRepository userRepository;

    private CreateUserService createUserService;

    @BeforeEach
    void setUp() {
        createUserService = new CreateUserService(userRepository);
    }

    @Test
    void create_ShouldReturnCreatedUser_WhenValidUserProvided() {
        // Arrange
        User userToCreate = new User();
        userToCreate.setName("Test User");
        userToCreate.setEmail("test@example.com");

        User createdUser = new User();
        createdUser.setId(UUID.randomUUID());
        createdUser.setName("Test User");
        createdUser.setEmail("test@example.com");

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenReturn(createdUser);

        // Act
        User result = createUserService.create(userToCreate);

        // Assert
        assertNotNull(result);
        assertEquals(createdUser.getId(), result.getId());
        assertEquals("Test User", result.getName());
        assertEquals("test@example.com", result.getEmail());
        verify(userRepository).findByEmail("test@example.com");
        verify(userRepository).save(userToCreate);
    }

    @Test
    void create_ShouldThrowDuplicatedResourceException_WhenEmailAlreadyExists() {
        // Arrange
        User existingUser = new User();
        existingUser.setId(UUID.randomUUID());
        existingUser.setName("Existing User");
        existingUser.setEmail("existing@example.com");

        User userToCreate = new User();
        userToCreate.setName("New User");
        userToCreate.setEmail("existing@example.com");

        when(userRepository.findByEmail("existing@example.com")).thenReturn(Optional.of(existingUser));

        // Act & Assert
        assertThrows(DuplicatedResourceException.class, () -> {
            createUserService.create(userToCreate);
        });
        verify(userRepository).findByEmail("existing@example.com");
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void findById_ShouldReturnUser_WhenUserExists() {
        // Arrange
        UUID userId = UUID.randomUUID();
        User existingUser = new User();
        existingUser.setId(userId);
        existingUser.setName("Test User");
        existingUser.setEmail("test@example.com");

        when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));

        // Act
        Optional<User> result = createUserService.findById(userId);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(userId, result.get().getId());
        assertEquals("Test User", result.get().getName());
        assertEquals("test@example.com", result.get().getEmail());
        verify(userRepository).findById(userId);
    }

    @Test
    void findById_ShouldReturnEmpty_WhenUserDoesNotExist() {
        // Arrange
        UUID userId = UUID.randomUUID();
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // Act
        Optional<User> result = createUserService.findById(userId);

        // Assert
        assertTrue(result.isEmpty());
        verify(userRepository).findById(userId);
    }

    @Test
    void findByEmail_ShouldReturnUser_WhenUserExists() {
        // Arrange
        String email = "test@example.com";
        User existingUser = new User();
        existingUser.setId(UUID.randomUUID());
        existingUser.setName("Test User");
        existingUser.setEmail(email);

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(existingUser));

        // Act
        Optional<User> result = createUserService.findByEmail(email);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(email, result.get().getEmail());
        assertEquals("Test User", result.get().getName());
        verify(userRepository).findByEmail(email);
    }

    @Test
    void findByEmail_ShouldReturnEmpty_WhenUserDoesNotExist() {
        // Arrange
        String email = "nonexistent@example.com";
        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        // Act
        Optional<User> result = createUserService.findByEmail(email);

        // Assert
        assertTrue(result.isEmpty());
        verify(userRepository).findByEmail(email);
    }
}

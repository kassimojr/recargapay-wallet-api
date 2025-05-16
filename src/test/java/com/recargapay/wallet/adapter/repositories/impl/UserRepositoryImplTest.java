package com.recargapay.wallet.adapter.repositories.impl;

import com.recargapay.wallet.adapter.converters.UserMapper;
import com.recargapay.wallet.adapter.entities.UserEntity;
import com.recargapay.wallet.adapter.repositories.UserJpaRepository;
import com.recargapay.wallet.core.domain.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserRepositoryImplTest {

    @Mock
    private UserJpaRepository jpaRepository;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private UserRepositoryImpl userRepository;

    private UUID userId;
    private User user;
    private UserEntity userEntity;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();

        user = new User();
        user.setId(userId);
        user.setName("Test User");
        user.setEmail("test@example.com");

        userEntity = new UserEntity();
        userEntity.setId(userId);
        userEntity.setName("Test User");
        userEntity.setEmail("test@example.com");
    }

    @Test
    void findById_ShouldReturnUser_WhenUserExists() {
        // Arrange
        when(jpaRepository.findById(userId)).thenReturn(Optional.of(userEntity));
        when(userMapper.toDomain(userEntity)).thenReturn(user);

        // Act
        Optional<User> result = userRepository.findById(userId);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(user, result.get());
        verify(jpaRepository).findById(userId);
        verify(userMapper).toDomain(userEntity);
    }

    @Test
    void findById_ShouldReturnEmptyOptional_WhenUserDoesNotExist() {
        // Arrange
        when(jpaRepository.findById(userId)).thenReturn(Optional.empty());

        // Act
        Optional<User> result = userRepository.findById(userId);

        // Assert
        assertTrue(result.isEmpty());
        verify(jpaRepository).findById(userId);
        verify(userMapper, never()).toDomain(any(UserEntity.class));
    }

    @Test
    void findByEmail_ShouldReturnUser_WhenUserExists() {
        // Arrange
        String email = "test@example.com";
        when(jpaRepository.findByEmail(email)).thenReturn(Optional.of(userEntity));
        when(userMapper.toDomain(userEntity)).thenReturn(user);

        // Act
        Optional<User> result = userRepository.findByEmail(email);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(user, result.get());
        verify(jpaRepository).findByEmail(email);
        verify(userMapper).toDomain(userEntity);
    }

    @Test
    void findByEmail_ShouldReturnEmptyOptional_WhenUserDoesNotExist() {
        // Arrange
        String email = "nonexistent@example.com";
        when(jpaRepository.findByEmail(email)).thenReturn(Optional.empty());

        // Act
        Optional<User> result = userRepository.findByEmail(email);

        // Assert
        assertTrue(result.isEmpty());
        verify(jpaRepository).findByEmail(email);
        verify(userMapper, never()).toDomain(any(UserEntity.class));
    }

    @Test
    void save_ShouldReturnSavedUser() {
        // Arrange
        when(userMapper.toEntity(user)).thenReturn(userEntity);
        when(jpaRepository.save(userEntity)).thenReturn(userEntity);
        when(userMapper.toDomain(userEntity)).thenReturn(user);

        // Act
        User result = userRepository.save(user);

        // Assert
        assertEquals(user, result);
        verify(userMapper).toEntity(user);
        verify(jpaRepository).save(userEntity);
        verify(userMapper).toDomain(userEntity);
    }

    @Test
    void update_ShouldCallSaveMethod() {
        // Arrange
        when(userMapper.toEntity(user)).thenReturn(userEntity);

        // Act
        userRepository.update(user);

        // Assert
        verify(userMapper).toEntity(user);
        verify(jpaRepository).save(userEntity);
    }

    @Test
    void delete_ShouldCallDeleteByIdMethod() {
        // Act
        userRepository.delete(userId);

        // Assert
        verify(jpaRepository).deleteById(userId);
    }

    @Test
    void findAll_ShouldReturnAllUsers() {
        // Arrange
        UserEntity userEntity2 = new UserEntity();
        userEntity2.setId(UUID.randomUUID());
        userEntity2.setName("Another User");
        userEntity2.setEmail("another@example.com");

        User user2 = new User();
        user2.setId(userEntity2.getId());
        user2.setName(userEntity2.getName());
        user2.setEmail(userEntity2.getEmail());

        List<UserEntity> userEntities = Arrays.asList(userEntity, userEntity2);
        when(jpaRepository.findAll()).thenReturn(userEntities);
        when(userMapper.toDomain(userEntity)).thenReturn(user);
        when(userMapper.toDomain(userEntity2)).thenReturn(user2);

        // Act
        List<User> result = userRepository.findAll();

        // Assert
        assertEquals(2, result.size());
        assertEquals(user, result.get(0));
        assertEquals(user2, result.get(1));
        verify(jpaRepository).findAll();
        verify(userMapper, times(2)).toDomain(any(UserEntity.class));
    }

    @Test
    void findAll_ShouldReturnEmptyList_WhenNoUsersExist() {
        // Arrange
        when(jpaRepository.findAll()).thenReturn(List.of());

        // Act
        List<User> result = userRepository.findAll();

        // Assert
        assertTrue(result.isEmpty());
        verify(jpaRepository).findAll();
    }
}

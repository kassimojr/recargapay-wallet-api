package com.recargapay.wallet.core.services;

import com.recargapay.wallet.core.domain.User;
import com.recargapay.wallet.core.domain.Wallet;
import com.recargapay.wallet.core.exceptions.UserNotFoundException;
import com.recargapay.wallet.core.exceptions.WalletAlreadyExistsException;
import com.recargapay.wallet.core.ports.out.UserRepository;
import com.recargapay.wallet.core.ports.out.WalletRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CreateWalletServiceTest {
    private WalletRepository walletRepository;
    private UserRepository userRepository;
    private CreateWalletService service;

    @BeforeEach
    void setUp() {
        walletRepository = mock(WalletRepository.class);
        userRepository = mock(UserRepository.class);
        service = new CreateWalletService(walletRepository, userRepository);
    }

    @Test
    void shouldCreateWalletSuccessfully() {
        // Arrange
        UUID userId = UUID.randomUUID();
        Wallet wallet = new Wallet(UUID.randomUUID(), userId, null);
        User user = new User();
        user.setId(userId);
        
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(walletRepository.findByUserId(userId)).thenReturn(Optional.empty());
        when(walletRepository.save(wallet)).thenReturn(wallet);
        
        // Act
        Wallet created = service.create(wallet);
        
        // Assert
        assertEquals(wallet, created);
        verify(userRepository).findById(userId);
        verify(walletRepository).findByUserId(userId);
        verify(walletRepository).save(wallet);
    }
    
    @Test
    void shouldThrowExceptionWhenUserNotFound() {
        // Arrange
        UUID userId = UUID.randomUUID();
        Wallet wallet = new Wallet(UUID.randomUUID(), userId, null);
        
        when(userRepository.findById(userId)).thenReturn(Optional.empty());
        
        // Act & Assert
        UserNotFoundException exception = assertThrows(
            UserNotFoundException.class,
            () -> service.create(wallet)
        );
        
        assertTrue(exception.getMessage().contains(userId.toString()));
        verify(userRepository).findById(userId);
        verify(walletRepository, never()).save(any(Wallet.class));
    }
    
    @Test
    void shouldThrowExceptionWhenWalletAlreadyExists() {
        // Arrange
        UUID userId = UUID.randomUUID();
        Wallet existingWallet = new Wallet(UUID.randomUUID(), userId, null);
        Wallet newWallet = new Wallet(null, userId, null);
        User user = new User();
        user.setId(userId);
        
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(walletRepository.findByUserId(userId)).thenReturn(Optional.of(existingWallet));
        
        // Act & Assert
        WalletAlreadyExistsException exception = assertThrows(
            WalletAlreadyExistsException.class,
            () -> service.create(newWallet)
        );
        
        assertTrue(exception.getMessage().contains(userId.toString()));
        verify(userRepository).findById(userId);
        verify(walletRepository).findByUserId(userId);
        verify(walletRepository, never()).save(any(Wallet.class));
    }
}

package com.recargapay.wallet.core.services;

import com.recargapay.wallet.core.domain.User;
import com.recargapay.wallet.core.domain.Wallet;
import com.recargapay.wallet.core.exceptions.UserNotFoundException;
import com.recargapay.wallet.core.exceptions.WalletAlreadyExistsException;
import com.recargapay.wallet.core.exceptions.WalletNotFoundException;
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
    
    @Test
    void findById_shouldReturnWalletWhenExists() {
        // Arrange
        UUID walletId = UUID.randomUUID();
        Wallet wallet = new Wallet(walletId, UUID.randomUUID(), null);
        
        when(walletRepository.findById(walletId)).thenReturn(Optional.of(wallet));
        
        // Act
        Wallet found = service.findById(walletId);
        
        // Assert
        assertEquals(wallet, found);
        verify(walletRepository).findById(walletId);
    }
    
    @Test
    void findById_shouldThrowExceptionWhenWalletNotFound() {
        // Arrange
        UUID walletId = UUID.randomUUID();
        
        when(walletRepository.findById(walletId)).thenReturn(Optional.empty());
        
        // Act & Assert
        assertThrows(
            WalletNotFoundException.class,
            () -> service.findById(walletId)
        );
        
        verify(walletRepository).findById(walletId);
    }
    
    @Test
    void findBalanceAt_shouldReturnWalletWhenExists() {
        // Arrange
        UUID walletId = UUID.randomUUID();
        Wallet wallet = new Wallet(walletId, UUID.randomUUID(), null);
        String timestamp = "2023-01-01T12:00:00";
        
        when(walletRepository.findById(walletId)).thenReturn(Optional.of(wallet));
        
        // Act
        Wallet found = service.findBalanceAt(walletId, timestamp);
        
        // Assert
        assertEquals(wallet, found);
        verify(walletRepository).findById(walletId);
    }
    
    @Test
    void findBalanceAt_shouldThrowExceptionWhenWalletNotFound() {
        // Arrange
        UUID walletId = UUID.randomUUID();
        String timestamp = "2023-01-01T12:00:00";
        
        when(walletRepository.findById(walletId)).thenReturn(Optional.empty());
        
        // Act & Assert
        assertThrows(
            WalletNotFoundException.class,
            () -> service.findBalanceAt(walletId, timestamp)
        );
        
        verify(walletRepository).findById(walletId);
    }
}

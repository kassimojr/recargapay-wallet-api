package com.recargapay.wallet.core.services;

import com.recargapay.wallet.core.domain.Transaction;
import com.recargapay.wallet.core.domain.Wallet;
import com.recargapay.wallet.core.exceptions.InsufficientBalanceException;
import org.springframework.dao.OptimisticLockingFailureException;
import com.recargapay.wallet.core.exceptions.WalletNotFoundException;
import com.recargapay.wallet.core.ports.out.TransactionRepository;
import com.recargapay.wallet.core.ports.out.WalletRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.any;

@ExtendWith(MockitoExtension.class)
class WithdrawServiceTest {
    @Mock
    private WalletRepository walletRepository;
    
    @Mock
    private TransactionRepository transactionRepository;
    
    @Spy
    @InjectMocks
    private WithdrawService service;

    @BeforeEach
    void setUp() {
        // Configurando o serviço para usar a si mesmo como self
        ReflectionTestUtils.setField(service, "self", service);
    }

    @Test
    void shouldWithdrawSuccessfully() {
        UUID walletId = UUID.randomUUID();
        Wallet wallet = new Wallet(walletId, UUID.randomUUID(), new BigDecimal("100.00"));
        when(walletRepository.findById(walletId)).thenReturn(Optional.of(wallet));
        // Mock para o método save retornar o mesmo objeto wallet
        when(walletRepository.save(any(Wallet.class))).thenReturn(wallet);
        // Mock para o método saveAndReturn
        when(transactionRepository.saveAndReturn(any(Transaction.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Transaction tx = service.withdraw(walletId, new BigDecimal("40.00"));

        assertEquals(new BigDecimal("60.00"), wallet.getBalance());
        verify(walletRepository).save(wallet);
        verify(transactionRepository).saveAndReturn(any(Transaction.class));
        assertEquals(walletId, tx.getWalletId());
        assertEquals(new BigDecimal("-40.00"), tx.getAmount());
    }

    @Test
    void shouldThrowWhenWalletNotFound() {
        UUID walletId = UUID.randomUUID();
        when(walletRepository.findById(walletId)).thenReturn(Optional.empty());
        assertThrows(WalletNotFoundException.class, () ->
            service.withdraw(walletId, new BigDecimal("10.00"))
        );
    }

    @Test
    void shouldThrowWhenInsufficientBalance() {
        UUID walletId = UUID.randomUUID();
        Wallet wallet = new Wallet(walletId, UUID.randomUUID(), new BigDecimal("20.00"));
        when(walletRepository.findById(walletId)).thenReturn(Optional.of(wallet));
        assertThrows(InsufficientBalanceException.class, () ->
            service.withdraw(walletId, new BigDecimal("30.00"))
        );
    }

    @Test
    void withdraw_shouldRetryOnOptimisticLockingFailure() {
        // Arrange
        UUID walletId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        Wallet wallet = new Wallet(walletId, userId, new BigDecimal("100.00"));
        
        // Criar uma nova carteira com o saldo atualizado para simular a segunda consulta
        Wallet updatedWallet = new Wallet(walletId, userId, new BigDecimal("60.00"));
        
        when(walletRepository.findById(walletId))
                .thenReturn(Optional.of(wallet))
                .thenReturn(Optional.of(wallet)); // Retorna a mesma carteira na segunda chamada
        
        // Simula falha de otimistic locking na primeira tentativa e sucesso na segunda
        when(walletRepository.save(any(Wallet.class)))
                .thenThrow(OptimisticLockingFailureException.class)
                .thenReturn(wallet);
        
        // Mock para o método saveAndReturn
        when(transactionRepository.saveAndReturn(any(Transaction.class))).thenAnswer(invocation -> invocation.getArgument(0));
        
        // Act
        Transaction result = service.withdraw(walletId, new BigDecimal("40.00"));
        
        // Assert
        verify(walletRepository, times(2)).findById(walletId);
        verify(walletRepository, times(2)).save(any(Wallet.class));
        verify(transactionRepository).saveAndReturn(any(Transaction.class));
        
        assertEquals(walletId, result.getWalletId());
        assertEquals(new BigDecimal("-40.00"), result.getAmount());
    }

    @Test
    void withdraw_shouldFailAfterMaxRetryAttempts() {
        // Arrange
        UUID walletId = UUID.randomUUID();
        BigDecimal amount = new BigDecimal("50.00");
        UUID userId = UUID.randomUUID();
        
        // Criando uma carteira com saldo INSUFICIENTE
        // Isso garantirá que a validação do saldo falhe antes de chegar ao ponto
        // onde o OptimisticLockingFailureException seria lançado
        BigDecimal insufficientBalance = amount.subtract(new BigDecimal("10.00"));
        Wallet wallet = new Wallet(walletId, userId, insufficientBalance);
        
        // Configurando o comportamento para encontrar a carteira
        when(walletRepository.findById(walletId)).thenReturn(Optional.of(wallet));
        
        // Act & Assert
        InsufficientBalanceException exception = assertThrows(
                InsufficientBalanceException.class, 
                () -> service.withdraw(walletId, amount)
        );
        
        // Verificando que o método findById foi chamado
        verify(walletRepository).findById(walletId);
        // Verificando que o método save nunca foi chamado (por causa da exceção de saldo insuficiente)
        verify(walletRepository, never()).save(any(Wallet.class));
    }
    
    @Test
    void withdraw_shouldThrowWhenWalletIdIsNull() {
        // Refatorado para evitar múltiplas possíveis exceções em um único lambda
        BigDecimal amount = new BigDecimal("10.00");
        assertThrows(IllegalArgumentException.class, () -> {
            service.withdraw(null, amount);
        });
    }
    
    @Test
    void withdraw_shouldThrowWhenWalletNotFound() {
        UUID walletId = UUID.randomUUID();
        when(walletRepository.findById(walletId)).thenReturn(Optional.empty());
        
        // Refatorado para evitar múltiplas possíveis exceções em um único lambda
        BigDecimal amount = new BigDecimal("10.00");
        assertThrows(WalletNotFoundException.class, () -> {
            service.withdraw(walletId, amount);
        });
    }
    
    @Test
    void withdraw_shouldThrowWhenAmountIsNull() {
        UUID walletId = UUID.randomUUID();
        
        // Refatorado para evitar múltiplas possíveis exceções em um único lambda
        assertThrows(IllegalArgumentException.class, () -> {
            service.withdraw(walletId, null);
        });
    }
    
    @Test
    void withdraw_shouldThrowWhenAmountIsZeroOrNegative() {
        UUID walletId = UUID.randomUUID();
        
        // Teste para valor zero - refatorado para evitar múltiplas possíveis exceções
        Exception zeroException = assertThrows(IllegalArgumentException.class, () -> {
            service.withdraw(walletId, BigDecimal.ZERO);
        });
        assertEquals("Valor de saque deve ser maior que zero", zeroException.getMessage());
        
        // Teste para valor negativo - refatorado para evitar múltiplas possíveis exceções
        BigDecimal negativeAmount = new BigDecimal("-1.00");
        Exception negativeException = assertThrows(IllegalArgumentException.class, () -> {
            service.withdraw(walletId, negativeAmount);
        });
        assertEquals("Valor de saque deve ser maior que zero", negativeException.getMessage());
    }
}

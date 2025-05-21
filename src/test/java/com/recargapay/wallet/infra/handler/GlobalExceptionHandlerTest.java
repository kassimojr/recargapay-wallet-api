package com.recargapay.wallet.infra.handler;

import com.recargapay.wallet.core.exceptions.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.context.request.WebRequest;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    @DisplayName("Deve retornar BAD_REQUEST para MethodArgumentNotValidException")
    void testHandleValidationException() {
        // Arrange
        MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = mock(BindingResult.class);
        
        List<FieldError> fieldErrors = new ArrayList<>();
        FieldError error = mock(FieldError.class);
        when(error.getField()).thenReturn("nome");
        when(error.getDefaultMessage()).thenReturn("não pode ser vazio");
        fieldErrors.add(error);
        
        when(bindingResult.getFieldErrors()).thenReturn(fieldErrors);
        when(ex.getBindingResult()).thenReturn(bindingResult);
        
        // Act
        ResponseEntity<ProblemDetail> response = handler.handleValidationException(ex);
        
        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        ProblemDetail problem = response.getBody();
        assertNotNull(problem);
        assertEquals("Dados de entrada inválidos", problem.getTitle());
        assertEquals("Erro de validação dos dados de entrada", problem.getDetail());
        
        @SuppressWarnings("unchecked")
        Map<String, String> validationErrors = (Map<String, String>) problem.getProperties().get("validationErrors");
        assertNotNull(validationErrors);
        assertEquals("não pode ser vazio", validationErrors.get("nome"));
    }

    @Test
    @DisplayName("Deve retornar BAD_REQUEST para IllegalArgumentException")
    void testHandleIllegalArgumentException() {
        // Arrange
        IllegalArgumentException ex = new IllegalArgumentException("Parâmetro inválido");
        
        // Act
        ResponseEntity<ProblemDetail> response = handler.handleIllegalArgumentException(ex);
        
        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        ProblemDetail problem = response.getBody();
        assertNotNull(problem);
        assertEquals("Parâmetro inválido", problem.getTitle());
        assertEquals("Parâmetro inválido", problem.getDetail());
    }

    @Test
    @DisplayName("Deve retornar CONFLICT para IllegalStateException")
    void testHandleIllegalStateException() {
        // Arrange
        IllegalStateException ex = new IllegalStateException("Estado inválido");
        
        // Act
        ResponseEntity<ProblemDetail> response = handler.handleIllegalStateException(ex);
        
        // Assert
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        ProblemDetail problem = response.getBody();
        assertNotNull(problem);
        assertEquals("Estado de operação inválido", problem.getTitle());
        assertEquals("Estado inválido", problem.getDetail());
    }

    @Test
    @DisplayName("Deve retornar CONFLICT para WalletAlreadyExistsException")
    void testHandleWalletAlreadyExistsException() {
        // Arrange
        WalletAlreadyExistsException ex = new WalletAlreadyExistsException("Carteira já existe");
        
        // Act
        ResponseEntity<ProblemDetail> response = handler.handleWalletAlreadyExistsException(ex);
        
        // Assert
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        ProblemDetail problem = response.getBody();
        assertNotNull(problem);
        assertEquals("Carteira já existe", problem.getTitle());
        assertEquals("Carteira já existe", problem.getDetail());
    }

    @Test
    @DisplayName("Deve retornar BAD_REQUEST para InsufficientBalanceException")
    void testHandleInsufficientBalance() {
        // Arrange
        InsufficientBalanceException ex = new InsufficientBalanceException("Saldo insuficiente");
        
        // Act
        ResponseEntity<ProblemDetail> response = handler.handleInsufficientBalance(ex);
        
        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        ProblemDetail problem = response.getBody();
        assertNotNull(problem);
        assertEquals("Saldo insuficiente", problem.getTitle());
        assertEquals("Saldo insuficiente", problem.getDetail());
    }

    @Test
    @DisplayName("Deve retornar NOT_FOUND para WalletNotFoundException")
    void testHandleWalletNotFound() {
        // Arrange
        WalletNotFoundException ex = new WalletNotFoundException("Carteira não encontrada");
        
        // Act
        ResponseEntity<ProblemDetail> response = handler.handleWalletNotFound(ex);
        
        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        ProblemDetail problem = response.getBody();
        assertNotNull(problem);
        assertEquals("Carteira não encontrada", problem.getTitle());
        assertEquals("Carteira não encontrada", problem.getDetail());
    }

    @Test
    @DisplayName("Deve retornar NOT_FOUND para UserNotFoundException")
    void testHandleUserNotFound() {
        // Arrange
        UserNotFoundException ex = new UserNotFoundException("Usuário não encontrado");
        
        // Act
        ResponseEntity<ProblemDetail> response = handler.handleUserNotFound(ex);
        
        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        ProblemDetail problem = response.getBody();
        assertNotNull(problem);
        assertEquals("Usuário não encontrado", problem.getTitle());
        assertEquals("Usuário não encontrado", problem.getDetail());
    }

    @Test
    @DisplayName("Deve retornar INTERNAL_SERVER_ERROR para Exception genérica")
    void testHandleGenericException() {
        // Arrange
        Exception ex = new Exception("Erro interno");
        WebRequest request = mock(WebRequest.class);
        
        // Act
        ResponseEntity<ProblemDetail> response = handler.handleGenericException(ex, request);
        
        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        ProblemDetail problem = response.getBody();
        assertNotNull(problem);
        assertEquals("Erro interno do servidor", problem.getTitle());
        assertEquals("Ocorreu um erro inesperado. Por favor, tente novamente mais tarde.", problem.getDetail());
        assertEquals(Exception.class.getName(), problem.getProperties().get("exceptionType"));
    }
    
    @Test
    @DisplayName("Deve retornar CONFLICT quando DataIntegrityViolationException contém erro de versão não inicializada")
    void testHandleDataIntegrityViolation_WithVersionError() {
        // Arrange
        DataIntegrityViolationException ex = new DataIntegrityViolationException(
            "Detached entity with generated id '123' has an uninitialized version value 'null': com.example.Entity");
        
        // Act
        ResponseEntity<ProblemDetail> response = handler.handleDataIntegrityViolation(ex);
        
        // Assert
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        ProblemDetail problem = response.getBody();
        assertNotNull(problem);
        assertEquals("Conflito de concorrência", problem.getTitle());
        assertTrue(problem.getDetail().contains("Os dados da carteira foram modificados"));
        assertNotNull(problem.getProperties().get("timestamp"));
    }
    
    @Test
    @DisplayName("Deve retornar BAD_REQUEST para outros tipos de DataIntegrityViolationException")
    void testHandleDataIntegrityViolation_WithOtherError() {
        // Arrange
        DataIntegrityViolationException ex = new DataIntegrityViolationException(
            "Violação de chave única: constraint_name");
        
        // Act
        ResponseEntity<ProblemDetail> response = handler.handleDataIntegrityViolation(ex);
        
        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        ProblemDetail problem = response.getBody();
        assertNotNull(problem);
        assertEquals("Erro de integridade de dados", problem.getTitle());
        assertEquals("Violação de chave única: constraint_name", problem.getDetail());
    }
    
    @Test
    @DisplayName("Deve retornar CONFLICT para OptimisticLockingFailureException")
    void testHandleOptimisticLocking() {
        // Arrange
        OptimisticLockingFailureException ex = new OptimisticLockingFailureException(
            "Outra transação modificou os mesmos dados");
        
        // Act
        ResponseEntity<ProblemDetail> response = handler.handleOptimisticLocking(ex);
        
        // Assert
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        ProblemDetail problem = response.getBody();
        assertNotNull(problem);
        assertEquals("Conflito de concorrência", problem.getTitle());
        assertTrue(problem.getDetail().contains("Os dados foram alterados por outro processo"));
        assertNotNull(problem.getProperties().get("cause"));
    }
}

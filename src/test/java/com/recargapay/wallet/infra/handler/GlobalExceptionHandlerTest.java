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
    @DisplayName("Should return BAD_REQUEST for MethodArgumentNotValidException")
    void testHandleValidationException() {
        // Arrange
        MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = mock(BindingResult.class);
        
        List<FieldError> fieldErrors = new ArrayList<>();
        FieldError error = mock(FieldError.class);
        when(error.getField()).thenReturn("name");
        when(error.getDefaultMessage()).thenReturn("cannot be empty");
        fieldErrors.add(error);
        
        when(bindingResult.getFieldErrors()).thenReturn(fieldErrors);
        when(ex.getBindingResult()).thenReturn(bindingResult);
        
        // Act
        ResponseEntity<ProblemDetail> response = handler.handleValidationException(ex);
        
        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        ProblemDetail problem = response.getBody();
        assertNotNull(problem);
        assertEquals("Invalid input data", problem.getTitle());
        assertEquals("Input data validation error", problem.getDetail());
        
        @SuppressWarnings("unchecked")
        Map<String, String> validationErrors = (Map<String, String>) problem.getProperties().get("validationErrors");
        assertNotNull(validationErrors);
        assertEquals("cannot be empty", validationErrors.get("name"));
    }

    @Test
    @DisplayName("Should return BAD_REQUEST for IllegalArgumentException")
    void testHandleIllegalArgumentException() {
        // Arrange
        IllegalArgumentException ex = new IllegalArgumentException("Invalid parameter");
        
        // Act
        ResponseEntity<ProblemDetail> response = handler.handleIllegalArgumentException(ex);
        
        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        ProblemDetail problem = response.getBody();
        assertNotNull(problem);
        assertEquals("Invalid parameter", problem.getTitle());
        assertEquals("Invalid parameter", problem.getDetail());
    }

    @Test
    @DisplayName("Should return CONFLICT for IllegalStateException")
    void testHandleIllegalStateException() {
        // Arrange
        IllegalStateException ex = new IllegalStateException("Invalid state");
        
        // Act
        ResponseEntity<ProblemDetail> response = handler.handleIllegalStateException(ex);
        
        // Assert
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        ProblemDetail problem = response.getBody();
        assertNotNull(problem);
        assertEquals("Invalid operation state", problem.getTitle());
        assertEquals("Invalid state", problem.getDetail());
    }

    @Test
    @DisplayName("Should return CONFLICT for WalletAlreadyExistsException")
    void testHandleWalletAlreadyExistsException() {
        // Arrange
        WalletAlreadyExistsException ex = new WalletAlreadyExistsException("Wallet already exists");
        
        // Act
        ResponseEntity<ProblemDetail> response = handler.handleWalletAlreadyExistsException(ex);
        
        // Assert
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        ProblemDetail problem = response.getBody();
        assertNotNull(problem);
        assertEquals("Wallet already exists", problem.getTitle());
        assertEquals("Wallet already exists", problem.getDetail());
    }

    @Test
    @DisplayName("Should return BAD_REQUEST for InsufficientBalanceException")
    void testHandleInsufficientBalance() {
        // Arrange
        InsufficientBalanceException ex = new InsufficientBalanceException("Insufficient balance");
        
        // Act
        ResponseEntity<ProblemDetail> response = handler.handleInsufficientBalance(ex);
        
        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        ProblemDetail problem = response.getBody();
        assertNotNull(problem);
        assertEquals("Insufficient balance", problem.getTitle());
        assertEquals("Insufficient balance", problem.getDetail());
    }

    @Test
    @DisplayName("Should return NOT_FOUND for WalletNotFoundException")
    void testHandleWalletNotFound() {
        // Arrange
        WalletNotFoundException ex = new WalletNotFoundException("Wallet not found");
        
        // Act
        ResponseEntity<ProblemDetail> response = handler.handleWalletNotFound(ex);
        
        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        ProblemDetail problem = response.getBody();
        assertNotNull(problem);
        assertEquals("Wallet not found", problem.getTitle());
        assertEquals("Wallet not found", problem.getDetail());
    }

    @Test
    @DisplayName("Should return NOT_FOUND for UserNotFoundException")
    void testHandleUserNotFound() {
        // Arrange
        UserNotFoundException ex = new UserNotFoundException("User not found");
        
        // Act
        ResponseEntity<ProblemDetail> response = handler.handleUserNotFound(ex);
        
        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        ProblemDetail problem = response.getBody();
        assertNotNull(problem);
        assertEquals("User not found", problem.getTitle());
        assertEquals("User not found", problem.getDetail());
    }

    @Test
    @DisplayName("Should return INTERNAL_SERVER_ERROR for generic Exception")
    void testHandleGenericException() {
        // Arrange
        Exception ex = new Exception("Internal error");
        WebRequest request = mock(WebRequest.class);
        
        // Act
        ResponseEntity<ProblemDetail> response = handler.handleGenericException(ex, request);
        
        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        ProblemDetail problem = response.getBody();
        assertNotNull(problem);
        assertEquals("Internal server error", problem.getTitle());
        assertEquals("An unexpected error occurred. Please try again later.", problem.getDetail());
        assertEquals(Exception.class.getName(), problem.getProperties().get("exceptionType"));
    }
    
    @Test
    @DisplayName("Should return CONFLICT when DataIntegrityViolationException contains uninitialized version error")
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
        assertEquals("Concurrency conflict", problem.getTitle());
        assertTrue(problem.getDetail().contains("The wallet data has been modified"));
        assertNotNull(problem.getProperties().get("timestamp"));
    }
    
    @Test
    @DisplayName("Should return BAD_REQUEST for other types of DataIntegrityViolationException")
    void testHandleDataIntegrityViolation_WithOtherError() {
        // Arrange
        DataIntegrityViolationException ex = new DataIntegrityViolationException("Database constraint violation");
        
        // Act
        ResponseEntity<ProblemDetail> response = handler.handleDataIntegrityViolation(ex);
        
        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        ProblemDetail problem = response.getBody();
        assertNotNull(problem);
        assertEquals("Data integrity violation", problem.getTitle());
        assertEquals("Database constraint violation", problem.getDetail());
    }
    
    @Test
    @DisplayName("Should return CONFLICT for OptimisticLockingFailureException")
    void testHandleOptimisticLocking() {
        // Arrange
        OptimisticLockingFailureException ex = new OptimisticLockingFailureException(
            "Another user has modified the data");
        
        // Act
        ResponseEntity<ProblemDetail> response = handler.handleOptimisticLocking(ex);
        
        // Assert
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        ProblemDetail problem = response.getBody();
        assertNotNull(problem);
        assertEquals("Concurrency conflict", problem.getTitle());
        assertTrue(problem.getDetail().contains("Another user has modified the data"));
        assertNotNull(problem.getProperties().get("cause"));
    }
}

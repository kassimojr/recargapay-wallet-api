package com.recargapay.wallet.infra.handler;

import com.recargapay.wallet.core.exceptions.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.context.request.WebRequest;
import java.util.Collections;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    @DisplayName("Deve retornar BAD_REQUEST para MethodArgumentNotValidException")
    void testHandleValidationException() {
        MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = mock(BindingResult.class);
        when(bindingResult.getFieldErrors()).thenReturn(Collections.emptyList());
        when(ex.getBindingResult()).thenReturn(bindingResult);
        ResponseEntity<Object> response = handler.handleValidationException(ex);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    @DisplayName("Deve retornar BAD_REQUEST para IllegalArgumentException")
    void testHandleIllegalArgumentException() {
        IllegalArgumentException ex = new IllegalArgumentException("erro");
        ResponseEntity<Object> response = handler.handleIllegalArgumentException(ex);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertTrue(response.getBody().toString().contains("erro"));
    }

    @Test
    @DisplayName("Deve retornar CONFLICT para IllegalStateException")
    void testHandleIllegalStateException() {
        IllegalStateException ex = new IllegalStateException("conflito");
        ResponseEntity<Object> response = handler.handleIllegalStateException(ex);
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertTrue(response.getBody().toString().contains("conflito"));
    }

    @Test
    @DisplayName("Deve retornar CONFLICT para WalletAlreadyExistsException")
    void testHandleWalletAlreadyExistsException() {
        WalletAlreadyExistsException ex = new WalletAlreadyExistsException("existe");
        ResponseEntity<Object> response = handler.handleWalletAlreadyExistsException(ex);
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertTrue(response.getBody().toString().contains("existe"));
    }

    @Test
    @DisplayName("Deve retornar BAD_REQUEST para InsufficientBalanceException")
    void testHandleInsufficientBalance() {
        InsufficientBalanceException ex = new InsufficientBalanceException("saldo insuficiente");
        ResponseEntity<String> response = handler.handleInsufficientBalance(ex);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertTrue(response.getBody().contains("saldo insuficiente"));
    }

    @Test
    @DisplayName("Deve retornar NOT_FOUND para WalletNotFoundException")
    void testHandleWalletNotFound() {
        WalletNotFoundException ex = new WalletNotFoundException("não encontrada");
        ResponseEntity<String> response = handler.handleWalletNotFound(ex);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertTrue(response.getBody().contains("não encontrada"));
    }

    @Test
    @DisplayName("Deve retornar NOT_FOUND para UserNotFoundException")
    void testHandleUserNotFound() {
        UserNotFoundException ex = new UserNotFoundException("usuário não encontrado");
        ResponseEntity<String> response = handler.handleUserNotFound(ex);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertTrue(response.getBody().contains("usuário não encontrado"));
    }

    @Test
    @DisplayName("Deve retornar INTERNAL_SERVER_ERROR para Exception genérica")
    void testHandleGenericException() {
        Exception ex = new Exception("erro interno");
        WebRequest request = mock(WebRequest.class);
        ResponseEntity<Object> response = handler.handleGenericException(ex, request);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertTrue(response.getBody().toString().contains("Internal server error"));
    }
}

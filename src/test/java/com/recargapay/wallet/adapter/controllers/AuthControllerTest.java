package com.recargapay.wallet.adapter.controllers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.servlet.mvc.method.annotation.ExceptionHandlerExceptionResolver;

import com.fasterxml.jackson.databind.ObjectMapper;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

class AuthControllerTest {

    private MockMvc mockMvc;
    
    private ObjectMapper objectMapper = new ObjectMapper();
    
    @Mock
    private AuthenticationManager authenticationManager;
    
    @InjectMocks
    private AuthController authController;
    
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        
        // Injetar manualmente o valor do JWT secret
        try {
            java.lang.reflect.Field field = AuthController.class.getDeclaredField("jwtSecret");
            field.setAccessible(true);
            field.set(authController, "testsecret");
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        ExceptionHandlerExceptionResolver exceptionResolver = new ExceptionHandlerExceptionResolver();
        exceptionResolver.afterPropertiesSet();
        
        mockMvc = MockMvcBuilders
                .standaloneSetup(authController)
                .setHandlerExceptionResolvers(exceptionResolver)
                .build();
    }

    @Test
    void login_withValidCredentials_returnsToken() throws Exception {
        // Arrange
        Map<String, String> credentials = new HashMap<>();
        credentials.put("username", "admin");
        credentials.put("password", "admin123");
        
        Authentication auth = new UsernamePasswordAuthenticationToken(
            "admin", 
            null, 
            Collections.singletonList(new SimpleGrantedAuthority("ROLE_ADMIN"))
        );
        
        when(authenticationManager.authenticate(any())).thenReturn(auth);
        
        // Act & Assert
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(credentials)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.token").exists());
    }

    @Test
    void login_withInvalidCredentials_returns401() throws Exception {
        // Arrange
        Map<String, String> credentials = new HashMap<>();
        credentials.put("username", "wrong");
        credentials.put("password", "wrong");
        
        when(authenticationManager.authenticate(any()))
            .thenThrow(new BadCredentialsException("Bad credentials"));
        
        // Act & Assert
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(credentials)))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.error").value("Invalid username or password"));
    }
    
    @Test
    void login_withAuthenticationException_returns401() throws Exception {
        // Arrange
        Map<String, String> credentials = new HashMap<>();
        credentials.put("username", "locked");
        credentials.put("password", "password");
        
        // Uma exceção de autenticação diferente de BadCredentialsException
        when(authenticationManager.authenticate(any()))
            .thenThrow(new TestAuthenticationException("Account locked"));
        
        // Act & Assert
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(credentials)))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.error").value("Authentication error: Account locked"));
    }
    
    @Test
    void login_withGenericException_returns500() throws Exception {
        // Arrange
        Map<String, String> credentials = new HashMap<>();
        credentials.put("username", "admin");
        credentials.put("password", "admin123");
        
        // Uma exceção genérica que não é de autenticação
        when(authenticationManager.authenticate(any()))
            .thenThrow(new RuntimeException("Internal server error"));
        
        // Act & Assert
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(credentials)))
            .andExpect(status().isInternalServerError())
            .andExpect(jsonPath("$.error").value("Internal error processing authentication"));
    }
    
    @Test
    void login_withMissingCredentials_returns401() throws Exception {
        // Arrange
        Map<String, String> credentials = new HashMap<>();
        // Não incluindo username e password
        
        // Act & Assert
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(credentials)))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.error").value("Username and password are required"));
    }
    
    // Classe auxiliar para teste de exceção de autenticação personalizada
    private static class TestAuthenticationException extends AuthenticationException {
        public TestAuthenticationException(String msg) {
            super(msg);
        }
    }
}

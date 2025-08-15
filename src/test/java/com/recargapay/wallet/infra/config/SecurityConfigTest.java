package com.recargapay.wallet.infra.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("SecurityConfig Tests")
class SecurityConfigTest {

    @Test
    @DisplayName("Should create BCrypt password encoder bean")
    void shouldCreateBCryptPasswordEncoderBean() {
        // Given
        SecurityConfig config = new SecurityConfig();
        
        // When
        PasswordEncoder encoder = config.passwordEncoder();
        
        // Then
        assertNotNull(encoder);
        assertTrue(encoder instanceof BCryptPasswordEncoder);
    }

    @Test
    @DisplayName("Should encode password correctly")
    void shouldEncodePasswordCorrectly() {
        // Given
        SecurityConfig config = new SecurityConfig();
        PasswordEncoder encoder = config.passwordEncoder();
        String rawPassword = "testPassword123";
        
        // When
        String encodedPassword = encoder.encode(rawPassword);
        
        // Then
        assertNotNull(encodedPassword);
        assertTrue(encoder.matches(rawPassword, encodedPassword));
    }
}

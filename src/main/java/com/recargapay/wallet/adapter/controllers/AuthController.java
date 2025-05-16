package com.recargapay.wallet.adapter.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Autenticação", description = "Geração de token JWT para testes")
@RequiredArgsConstructor
public class AuthController {

    private static final String ERROR_KEY = "error";
    
    @Value("${spring.security.oauth2.resourceserver.jwt.secret}")
    private String jwtSecret;
    
    private final AuthenticationManager authenticationManager;

    @Operation(summary = "Realiza login e retorna um JWT para testes")
    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> login(@RequestBody Map<String, String> credentials) {
        String username = credentials.get("username");
        String password = credentials.get("password");
        
        // Verifica se as credenciais foram fornecidas
        if (username == null || password == null || username.isEmpty() || password.isEmpty()) {
            Map<String, String> response = new HashMap<>();
            response.put(ERROR_KEY, "Username e password são obrigatórios");
            return ResponseEntity.status(401).body(response);
        }
        
        try {
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(username, password)
            );
            
            String token = generateToken(authentication);
            Map<String, String> response = new HashMap<>();
            response.put("token", token);
            return ResponseEntity.ok(response);
        } catch (BadCredentialsException e) {
            Map<String, String> response = new HashMap<>();
            response.put(ERROR_KEY, "Usuário ou senha inválidos");
            return ResponseEntity.status(401).body(response);
        } catch (AuthenticationException e) {
            Map<String, String> response = new HashMap<>();
            response.put(ERROR_KEY, "Erro de autenticação: " + e.getMessage());
            return ResponseEntity.status(401).body(response);
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put(ERROR_KEY, "Erro interno ao processar a autenticação");
            return ResponseEntity.status(500).body(response);
        }
    }

    private String generateToken(Authentication authentication) {
        Instant now = Instant.now();
        Instant exp = now.plusSeconds(60L * 60); // 1 hora
        
        String authorities = authentication.getAuthorities().stream()
            .map(GrantedAuthority::getAuthority)
            .collect(Collectors.joining(" "));
            
        return Jwts.builder()
                .setSubject(authentication.getName())
                .claim("scope", authorities)
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(exp))
                .signWith(SignatureAlgorithm.HS256, jwtSecret.getBytes(StandardCharsets.UTF_8))
                .compact();
    }
}

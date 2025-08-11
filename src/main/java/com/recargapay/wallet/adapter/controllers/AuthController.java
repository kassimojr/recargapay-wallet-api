package com.recargapay.wallet.adapter.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
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

/**
 * Controller responsible for authentication and JWT token generation.
 * This controller offers endpoints for login and access token generation.
 */
@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication", description = "API for authentication and JWT token generation")
@RequiredArgsConstructor
public class AuthController {

    private static final String ERROR_KEY = "error";
    
    @Value("${spring.security.oauth2.resourceserver.jwt.secret}")
    private String jwtSecret;
    
    private final AuthenticationManager authenticationManager;

    /**
     * Endpoint to perform login and obtain a JWT token.
     * 
     * @param credentials Access credentials (username and password)
     * @return JWT token on success or error message
     */
    @Operation(
        summary = "Perform login",
        description = "Authenticates the user with username and password and returns a valid JWT token for 1 hour"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", 
            description = "Login successful",
            content = @Content(mediaType = "application/json", 
                schema = @Schema(example = "{\"token\": \"eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...\"}"))
        ),
        @ApiResponse(
            responseCode = "401", 
            description = "Invalid credentials",
            content = @Content(mediaType = "application/json", 
                schema = @Schema(example = "{\"error\": \"Invalid username or password\"}"))
        ),
        @ApiResponse(
            responseCode = "500", 
            description = "Internal server error",
            content = @Content(mediaType = "application/json", 
                schema = @Schema(example = "{\"error\": \"Internal error processing authentication\"}"))
        )
    })
    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> login(
            @org.springframework.web.bind.annotation.RequestBody 
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                description = "Access credentials",
                required = true,
                content = @Content(mediaType = "application/json", 
                    schema = @Schema(example = "{\"username\": \"your_username\", \"password\": \"your_password\"}")))
            Map<String, String> credentials) {
            
        String username = credentials.get("username");
        String password = credentials.get("password");
        
        // Check if credentials were provided
        if (username == null || password == null || username.isEmpty() || password.isEmpty()) {
            Map<String, String> response = new HashMap<>();
            response.put(ERROR_KEY, "Username and password are required");
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
            response.put(ERROR_KEY, "Invalid username or password");
            return ResponseEntity.status(401).body(response);
        } catch (AuthenticationException e) {
            Map<String, String> response = new HashMap<>();
            response.put(ERROR_KEY, "Authentication error: " + e.getMessage());
            return ResponseEntity.status(401).body(response);
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put(ERROR_KEY, "Internal error processing authentication");
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * Generates a JWT token based on the authentication performed.
     * 
     * @param authentication Authentication object
     * @return JWT token
     */
    private String generateToken(Authentication authentication) {
        Instant now = Instant.now();
        Instant exp = now.plusSeconds(60L * 60); // 1 hour
        
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

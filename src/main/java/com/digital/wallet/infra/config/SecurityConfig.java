package com.digital.wallet.infra.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.config.Customizer;

@Configuration
@EnableMethodSecurity
@EnableWebSecurity
public class SecurityConfig {

    @Value("${app.user.username}")
    private String username;

    @Value("${app.user.password}")
    private String password;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .headers(headers -> headers
                // Prevent clickjacking attacks
                .frameOptions(frameOptions -> frameOptions.deny())
                // Prevent MIME type sniffing
                .contentTypeOptions(Customizer.withDefaults())
                // Enable HSTS (HTTP Strict Transport Security)
                .httpStrictTransportSecurity(hsts -> hsts
                    .maxAgeInSeconds(31536000) // 1 year
                    .includeSubDomains(true)
                )
                // Add custom security headers via header writer
                .addHeaderWriter((request, response) -> {
                    // X-XSS-Protection
                    response.setHeader("X-XSS-Protection", "1; mode=block");
                    // Referrer Policy
                    response.setHeader("Referrer-Policy", "strict-origin-when-cross-origin");
                    // Content Security Policy (basic)
                    response.setHeader("Content-Security-Policy", 
                        "default-src 'self'; " +
                        "script-src 'self' 'unsafe-inline' 'unsafe-eval'; " +
                        "style-src 'self' 'unsafe-inline'; " +
                        "img-src 'self' data: https:; " +
                        "font-src 'self' data:; " +
                        "connect-src 'self'");
                    // Permissions Policy (formerly Feature Policy)
                    response.setHeader("Permissions-Policy", 
                        "geolocation=(), microphone=(), camera=()");
                })
            )
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(authz -> authz
                .requestMatchers(
                    "/v3/api-docs/**",
                    "/swagger-ui/**",
                    "/swagger-ui.html",
                    "/actuator/**",
                    "/api/public/**",
                    "/api/v1/auth/login",
                    "/*.html",
                    "/*.js",
                    "/*.css",
                    "/*.ico",
                    "/static/**",
                    "/resources/**"
                ).permitAll()
                .anyRequest().authenticated()
            )
            .oauth2ResourceServer(oauth2 -> oauth2
                .jwt(Customizer.withDefaults())
            );
        return http.build();
    }
    
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
    
    @Bean
    public UserDetailsService userDetailsService(PasswordEncoder passwordEncoder) {
        UserDetails admin = User.builder()
            .username(username)
            .password(passwordEncoder.encode(password))
            .roles("ADMIN")
            .build();
            
        return new InMemoryUserDetailsManager(admin);
    }
    
    @Bean
    public AuthenticationManager authenticationManager(
            UserDetailsService userDetailsService,
            PasswordEncoder passwordEncoder) {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder);
        return new ProviderManager(authProvider);
    }
}

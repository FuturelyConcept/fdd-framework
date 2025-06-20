package com.fdd.core.security;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Spring Security configuration for FDD
 * Disables default security since we handle JWT validation manually
 */
@Configuration
@EnableWebSecurity
@ConditionalOnProperty(prefix = "fdd.security", name = "enabled", havingValue = "true", matchIfMissing = true)
public class FddSecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(authz -> authz
                        .requestMatchers("/auth/**", "/functions/**", "/demo/test").permitAll()
                        .anyRequest().permitAll() // We handle security via interceptors
                );

        return http.build();
    }
}
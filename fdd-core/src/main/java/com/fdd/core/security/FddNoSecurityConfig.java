package com.fdd.core.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Configuration when FDD security is disabled
 * Ensures Spring Security doesn't interfere with the application
 */
@Configuration
@EnableWebSecurity
@ConditionalOnClass(name = "org.springframework.security.web.SecurityFilterChain")
@ConditionalOnProperty(prefix = "fdd.security", name = "enabled", havingValue = "false", matchIfMissing = true)
public class FddNoSecurityConfig {

    /**
     * Permissive security filter chain when FDD security is disabled
     * This prevents Spring Boot's default security from blocking endpoints
     */
    @Bean
    @Order(1)
    public SecurityFilterChain noSecurityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(authz -> authz
                        .anyRequest().permitAll()
                )
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .httpBasic(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable);

        return http.build();
    }
}
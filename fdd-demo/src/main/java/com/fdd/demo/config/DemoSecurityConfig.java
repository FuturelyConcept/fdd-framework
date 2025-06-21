package com.fdd.demo.config;

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
 * Demo Application Security Configuration
 * This configures security specifically for the demo application endpoints
 * It works alongside the FDD framework security configuration
 */
@Configuration
@EnableWebSecurity
@ConditionalOnProperty(prefix = "fdd.security", name = "enabled", havingValue = "true", matchIfMissing = false)
public class DemoSecurityConfig {

    /**
     * Demo application security filter chain
     * Order(2) = lower priority than framework endpoints
     */
    @Bean
    @Order(2)
    public SecurityFilterChain demoApplicationFilterChain(HttpSecurity http) throws Exception {
        http
                .securityMatcher("/demo/**", "/fdd-showcase/**") // Only demo endpoints
                .authorizeHttpRequests(authz -> authz
                        .requestMatchers("/demo/test", "/demo/sample-data").permitAll() // Public demo endpoints
                        .requestMatchers("/fdd-showcase/**").permitAll() // Public showcase endpoints
                        .anyRequest().authenticated() // All other demo endpoints need auth
                )
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .httpBasic(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable);

        return http.build();
    }
}
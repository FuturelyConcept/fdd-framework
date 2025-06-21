package com.fdd.core.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * FDD Framework Web Configuration
 * This is reserved for framework-level web configuration only
 * Application-specific web configuration should be in the application layer
 */
@Configuration
@ConditionalOnProperty(prefix = "fdd.function", name = "enabled", havingValue = "true", matchIfMissing = true)
public class FddWebConfig implements WebMvcConfigurer {

    // Framework-level web configuration goes here
    // Currently empty - no framework-level interceptors needed

    // Note: Application-specific interceptors should be configured
    // in the application layer, not here
}
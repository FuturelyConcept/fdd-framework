package com.fdd.core.config;

import com.fdd.core.security.JwtSecurityInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web configuration to register security interceptor
 */
@Configuration
@ConditionalOnProperty(prefix = "fdd.security", name = "enabled", havingValue = "true", matchIfMissing = true)
public class FddWebConfig implements WebMvcConfigurer {

    @Autowired
    private JwtSecurityInterceptor jwtSecurityInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(jwtSecurityInterceptor)
                .addPathPatterns("/demo/**") // Apply security to demo endpoints
                .excludePathPatterns(
                        "/demo/test",           // Public test endpoint
                        "/functions/**",        // Function discovery (public)
                        "/auth/**"             // Authentication endpoints (public)
                );
    }
}
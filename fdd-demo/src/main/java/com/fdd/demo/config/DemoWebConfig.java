package com.fdd.demo.config;

import com.fdd.core.security.JwtSecurityInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Demo Web Configuration
 * Adds JWT interceptor for demo endpoints when security is enabled
 */
@Configuration
@ConditionalOnProperty(prefix = "fdd.security", name = "enabled", havingValue = "true", matchIfMissing = false)
public class DemoWebConfig implements WebMvcConfigurer {

    @Autowired(required = false)
    private JwtSecurityInterceptor jwtSecurityInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        if (jwtSecurityInterceptor != null) {
            registry.addInterceptor(jwtSecurityInterceptor)
                    .addPathPatterns("/demo/**") // Only apply to demo endpoints
                    .excludePathPatterns(
                            "/demo/test",           // Public test endpoint
                            "/demo/sample-data"     // Public sample data endpoint
                    );
        }
    }
}
package com.fdd.core.config;

import com.fdd.core.registry.FunctionRegistry;
import com.fdd.core.monitoring.FunctionMonitoringInterceptor;
import com.fdd.core.security.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

import java.util.function.Function;
import java.util.Map;

/**
 * Simplified auto-configuration focusing on core FDD functionality
 */
@AutoConfiguration
@EnableAspectJAutoProxy
@ConditionalOnProperty(prefix = "fdd.function", name = "enabled", havingValue = "true", matchIfMissing = true)
public class FddAutoConfiguration implements ApplicationListener<ApplicationReadyEvent> {

    private static final Logger logger = LoggerFactory.getLogger(FddAutoConfiguration.class);

    /**
     * Create FunctionRegistry bean - this is the core of FDD
     */
    @Bean
    public FunctionRegistry functionRegistry() {
        logger.info("Creating FunctionRegistry bean");
        return new FunctionRegistry();
    }

    /**
     * Create ServerlessConfigLoader bean
     */
    @Bean
    public ServerlessConfigLoader serverlessConfigLoader() {
        logger.debug("Creating ServerlessConfigLoader bean");
        return new ServerlessConfigLoader();
    }

    /**
     * Create FunctionDiscoveryController bean for function introspection
     */
    @Bean
    @ConditionalOnProperty(prefix = "fdd.function.discovery", name = "enabled", havingValue = "true", matchIfMissing = true)
    public FunctionDiscoveryController functionDiscoveryController() {
        logger.debug("Creating FunctionDiscoveryController bean");
        return new FunctionDiscoveryController();
    }

    /**
     * Create FunctionMonitoringInterceptor for logging (optional)
     */
    @Bean
    @ConditionalOnProperty(prefix = "fdd.function.monitoring", name = "enabled", havingValue = "true", matchIfMissing = false)
    public FunctionMonitoringInterceptor functionMonitoringInterceptor() {
        logger.debug("Creating FunctionMonitoringInterceptor bean");
        return new FunctionMonitoringInterceptor();
    }

    /**
     * Create JWT security components
     */
    @Bean
    @ConditionalOnProperty(prefix = "fdd.security", name = "enabled", havingValue = "true", matchIfMissing = true)
    public JwtValidator jwtValidator() {
        logger.debug("Creating JwtValidator bean");
        return new JwtValidator();
    }

    @Bean
    @ConditionalOnProperty(prefix = "fdd.security", name = "enabled", havingValue = "true", matchIfMissing = true)
    public JwtSecurityInterceptor jwtSecurityInterceptor() {
        logger.debug("Creating JwtSecurityInterceptor bean");
        return new JwtSecurityInterceptor();
    }

    @Bean
    @ConditionalOnProperty(prefix = "fdd.security", name = "enabled", havingValue = "true", matchIfMissing = true)
    public FunctionSecurityAspect functionSecurityAspect() {
        logger.debug("Creating FunctionSecurityAspect bean");
        return new FunctionSecurityAspect();
    }

    @Bean
    @ConditionalOnProperty(prefix = "fdd.security", name = "enabled", havingValue = "true", matchIfMissing = true)
    public AuthController authController() {
        logger.debug("Creating AuthController bean");
        return new AuthController();
    }

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        ApplicationContext applicationContext = event.getApplicationContext();

        try {
            // Get beans - they should exist by now
            FunctionRegistry functionRegistry = applicationContext.getBean(FunctionRegistry.class);
            ServerlessConfigLoader configLoader = applicationContext.getBean(ServerlessConfigLoader.class);

            if (functionRegistry == null || configLoader == null) {
                logger.warn("FDD Framework dependencies not available, skipping initialization");
                return;
            }

            logger.info("FDD Framework starting up - scanning for functions...");

            // Load serverless.yml configuration
            ServerlessConfig config = configLoader.loadConfig();
            Map<String, com.fdd.core.registry.FunctionMetadata> metadataMap = configLoader.createMetadataMap(config);

            // Scan for Function beans in the application context
            Map<String, Function> functionBeans = applicationContext.getBeansOfType(Function.class);

            logger.info("Found {} Function beans and {} metadata entries",
                    functionBeans.size(), metadataMap.size());

            // Register functions with their metadata
            functionBeans.forEach((beanName, function) -> {
                com.fdd.core.registry.FunctionMetadata metadata = metadataMap.get(beanName);
                if (metadata == null) {
                    // Create basic metadata if not found in serverless.yml
                    metadata = new com.fdd.core.registry.FunctionMetadata();
                    metadata.setComponent(beanName);
                    metadata.setName("com.fdd.function." + beanName);
                    logger.debug("Created basic metadata for function: {}", beanName);
                }

                functionRegistry.registerFunction(beanName, function, metadata);
            });

            logger.info("FDD Framework initialization complete - {} functions registered",
                    functionRegistry.size());

        } catch (Exception e) {
            logger.error("Failed to initialize FDD Framework", e);
        }
    }
}
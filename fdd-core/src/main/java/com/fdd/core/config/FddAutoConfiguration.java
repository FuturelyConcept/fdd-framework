package com.fdd.core.config;

import com.fdd.core.registry.FunctionRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;

import java.util.function.Function;
import java.util.Map;

/**
 * Auto-configuration for FDD framework
 */
@AutoConfiguration
@ConditionalOnProperty(prefix = "fdd.function", name = "enabled", havingValue = "true", matchIfMissing = true)
public class FddAutoConfiguration implements ApplicationListener<ApplicationReadyEvent> {

    private static final Logger logger = LoggerFactory.getLogger(FddAutoConfiguration.class);

    @Autowired(required = false)
    private ApplicationContext applicationContext;

    @Autowired(required = false)
    private FunctionRegistry functionRegistry;

    @Autowired(required = false)
    private ServerlessConfigLoader configLoader;

    /**
     * Create FunctionRegistry bean
     */
    @Bean
    public FunctionRegistry functionRegistry() {
        logger.debug("Creating FunctionRegistry bean");
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
     * Create FunctionDiscoveryController bean if discovery is enabled
     */
    @Bean
    @ConditionalOnProperty(prefix = "fdd.function.discovery", name = "enabled", havingValue = "true", matchIfMissing = true)
    public FunctionDiscoveryController functionDiscoveryController(FunctionRegistry functionRegistry) {
        logger.debug("Creating FunctionDiscoveryController bean");
        return new FunctionDiscoveryController(functionRegistry);
    }

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        if (applicationContext == null || functionRegistry == null || configLoader == null) {
            logger.warn("FDD Framework dependencies not available, skipping initialization");
            return;
        }

        logger.info("FDD Framework starting up - scanning for functions...");

        try {
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
package com.fdd.core.config;

import com.fdd.core.registry.FunctionRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;

import java.util.function.Function;
import java.util.Map;

/**
 * Fixed auto-configuration - removes FunctionDiscoveryController to avoid dependency issues
 */
@AutoConfiguration
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

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        ApplicationContext applicationContext = event.getApplicationContext();

        try {
            FunctionRegistry functionRegistry = applicationContext.getBean(FunctionRegistry.class);
            ServerlessConfigLoader configLoader = applicationContext.getBean(ServerlessConfigLoader.class);

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
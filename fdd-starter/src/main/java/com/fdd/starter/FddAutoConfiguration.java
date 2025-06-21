package com.fdd.starter;

import com.fdd.core.config.FunctionDiscoveryController;
import com.fdd.core.config.ServerlessConfig;
import com.fdd.core.config.ServerlessConfigLoader;
import com.fdd.core.registry.FunctionRegistry;
import com.fdd.core.monitoring.FunctionMonitoringInterceptor;
import com.fdd.core.discovery.*;
import com.fdd.core.security.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

import java.util.function.Function;
import java.util.Map;

/**
 * FDD Framework Auto-Configuration
 * Core framework configuration - no application-specific settings
 */
@AutoConfiguration
@EnableAspectJAutoProxy
public class FddAutoConfiguration implements ApplicationListener<ApplicationReadyEvent> {

    private static final Logger logger = LoggerFactory.getLogger(FddAutoConfiguration.class);

    public FddAutoConfiguration() {
        logger.info("🚀 FDD Framework Auto-Configuration starting...");
    }

    // =====================================================
    // CORE FRAMEWORK BEANS - Always Available
    // =====================================================

    @Bean
    @ConditionalOnMissingBean
    public FunctionRegistry functionRegistry() {
        logger.info("✅ Creating FunctionRegistry bean");
        return new FunctionRegistry();
    }

    @Bean
    @ConditionalOnMissingBean
    public ServerlessConfigLoader serverlessConfigLoader() {
        logger.info("✅ Creating ServerlessConfigLoader bean");
        return new ServerlessConfigLoader();
    }

    // =====================================================
    // DISCOVERY & MONITORING BEANS
    // =====================================================

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = "fdd.function.discovery", name = "enabled", havingValue = "true", matchIfMissing = true)
    public FunctionDiscoveryController functionDiscoveryController() {
        logger.info("✅ Creating FunctionDiscoveryController bean");
        return new FunctionDiscoveryController();
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = "fdd.function.discovery", name = "enabled", havingValue = "true", matchIfMissing = true)
    public FunctionSchemaGenerator functionSchemaGenerator() {
        logger.info("✅ Creating FunctionSchemaGenerator bean");
        return new FunctionSchemaGenerator();
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = "fdd.function.monitoring", name = "enabled", havingValue = "true", matchIfMissing = true)
    public FunctionMetricsTracker functionMetricsTracker() {
        logger.info("✅ Creating FunctionMetricsTracker bean");
        return new FunctionMetricsTracker();
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = "fdd.function.monitoring", name = "enabled", havingValue = "true", matchIfMissing = true)
    public FunctionDependencyAnalyzer functionDependencyAnalyzer() {
        logger.info("✅ Creating FunctionDependencyAnalyzer bean");
        return new FunctionDependencyAnalyzer();
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = "fdd.function.monitoring", name = "enabled", havingValue = "true", matchIfMissing = false)
    public FunctionMonitoringInterceptor functionMonitoringInterceptor() {
        logger.info("✅ Creating FunctionMonitoringInterceptor bean");
        return new FunctionMonitoringInterceptor();
    }

    // =====================================================
    // SECURITY FRAMEWORK BEANS - Only when Spring Security is available
    // =====================================================

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnClass(name = "org.springframework.security.web.SecurityFilterChain")
    @ConditionalOnProperty(prefix = "fdd.security", name = "enabled", havingValue = "true", matchIfMissing = false)
    public JwtValidator jwtValidator() {
        logger.info("✅ Creating JwtValidator bean");
        return new JwtValidator();
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnClass(name = "org.springframework.security.web.SecurityFilterChain")
    @ConditionalOnProperty(prefix = "fdd.security", name = "enabled", havingValue = "true", matchIfMissing = false)
    public JwtSecurityInterceptor jwtSecurityInterceptor() {
        logger.info("✅ Creating JwtSecurityInterceptor bean");
        return new JwtSecurityInterceptor();
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnClass(name = "org.springframework.security.web.SecurityFilterChain")
    @ConditionalOnProperty(prefix = "fdd.security", name = "enabled", havingValue = "true", matchIfMissing = false)
    public FunctionSecurityAspect functionSecurityAspect() {
        logger.info("✅ Creating FunctionSecurityAspect bean");
        return new FunctionSecurityAspect();
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnClass(name = "org.springframework.security.web.SecurityFilterChain")
    @ConditionalOnProperty(prefix = "fdd.security", name = "enabled", havingValue = "true", matchIfMissing = false)
    public AuthController authController() {
        logger.info("✅ Creating AuthController bean");
        return new AuthController();
    }

    // =====================================================
    // FUNCTION REGISTRATION - Application Ready Event
    // =====================================================

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        ApplicationContext applicationContext = event.getApplicationContext();

        try {
            logger.info("🔄 FDD Framework ApplicationReadyEvent triggered");

            FunctionRegistry functionRegistry = applicationContext.getBean(FunctionRegistry.class);
            ServerlessConfigLoader configLoader = applicationContext.getBean(ServerlessConfigLoader.class);

            logger.info("🔍 Scanning for Function<T,R> implementations...");

            // Load serverless.yml configuration
            ServerlessConfig config = configLoader.loadConfig();
            Map<String, com.fdd.core.registry.FunctionMetadata> metadataMap = configLoader.createMetadataMap(config);

            // Scan for Function beans in the application context
            Map<String, Function> functionBeans = applicationContext.getBeansOfType(Function.class);

            logger.info("📊 Found {} Function beans and {} metadata entries",
                    functionBeans.size(), metadataMap.size());

            // Register functions with their metadata
            int registeredCount = 0;
            for (Map.Entry<String, Function> entry : functionBeans.entrySet()) {
                String beanName = entry.getKey();
                Function function = entry.getValue();

                com.fdd.core.registry.FunctionMetadata metadata = metadataMap.get(beanName);
                if (metadata == null) {
                    // Create basic metadata if not found in serverless.yml
                    metadata = new com.fdd.core.registry.FunctionMetadata();
                    metadata.setComponent(beanName);
                    metadata.setName("com.fdd.function." + beanName);
                    logger.debug("📝 Created basic metadata for function: {}", beanName);
                }

                functionRegistry.registerFunction(beanName, function, metadata);
                registeredCount++;
                logger.debug("✅ Registered function: {}", beanName);
            }

            logger.info("🎉 FDD Framework initialization complete - {} functions registered", registeredCount);

            // Log framework status
            logFrameworkStatus(applicationContext);

        } catch (Exception e) {
            logger.error("❌ Failed to initialize FDD Framework", e);
        }
    }

    private void logFrameworkStatus(ApplicationContext applicationContext) {
        try {
            boolean discoveryEnabled = applicationContext.getEnvironment()
                    .getProperty("fdd.function.discovery.enabled", Boolean.class, true);
            boolean monitoringEnabled = applicationContext.getEnvironment()
                    .getProperty("fdd.function.monitoring.enabled", Boolean.class, true);
            boolean securityEnabled = applicationContext.getEnvironment()
                    .getProperty("fdd.security.enabled", Boolean.class, false);

            logger.info("📈 FDD Framework Status:");
            logger.info("   📡 Discovery: {}", discoveryEnabled ? "ENABLED" : "DISABLED");
            logger.info("   📊 Monitoring: {}", monitoringEnabled ? "ENABLED" : "DISABLED");
            logger.info("   🔒 Security Framework: {}", securityEnabled ? "ENABLED" : "DISABLED");

        } catch (Exception e) {
            logger.debug("Could not log framework status: {}", e.getMessage());
        }
    }
}
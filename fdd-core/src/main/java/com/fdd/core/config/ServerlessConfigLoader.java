package com.fdd.core.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fdd.core.registry.FunctionMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * Loads serverless.yml configuration and creates function metadata
 */
public class ServerlessConfigLoader {
    private static final Logger logger = LoggerFactory.getLogger(ServerlessConfigLoader.class);

    private final ObjectMapper yamlMapper = new ObjectMapper(new YAMLFactory());

    /**
     * Load serverless configuration from classpath
     */
    public ServerlessConfig loadConfig() {
        return loadConfig("serverless.yml");
    }

    /**
     * Load serverless configuration from specified file
     */
    public ServerlessConfig loadConfig(String configPath) {
        logger.debug("Loading serverless configuration from: {}", configPath);

        try {
            Resource resource = new ClassPathResource(configPath);
            if (!resource.exists()) {
                logger.warn("Serverless configuration not found at: {}", configPath);
                return createEmptyConfig(); // Return empty config
            }

            try (InputStream inputStream = resource.getInputStream()) {
                ServerlessConfig config = yamlMapper.readValue(inputStream, ServerlessConfig.class);
                logger.info("Successfully loaded serverless configuration with {} functions",
                        config.getServerless() != null && config.getServerless().getFunctions() != null ?
                                config.getServerless().getFunctions().size() : 0);
                return config;
            }
        } catch (IOException e) {
            logger.error("Failed to load serverless configuration from: {}", configPath, e);
            return createEmptyConfig();
        }
    }

    /**
     * Create empty configuration
     */
    private ServerlessConfig createEmptyConfig() {
        ServerlessConfig config = new ServerlessConfig();
        ServerlessConfig.ServerlessDefinition definition = new ServerlessConfig.ServerlessDefinition();
        definition.setFunctions(new HashMap<>());
        config.setServerless(definition);
        return config;
    }

    /**
     * Convert serverless configuration to function metadata map
     */
    public Map<String, FunctionMetadata> createMetadataMap(ServerlessConfig config) {
        Map<String, FunctionMetadata> metadataMap = new HashMap<>();

        if (config.getServerless() == null || config.getServerless().getFunctions() == null) {
            logger.warn("No functions found in serverless configuration");
            return metadataMap;
        }

        config.getServerless().getFunctions().forEach((componentName, functionConfig) -> {
            try {
                FunctionMetadata metadata = createMetadata(componentName, functionConfig);
                metadataMap.put(componentName, metadata);
                logger.debug("Created metadata for function: {}", componentName);
            } catch (Exception e) {
                logger.error("Failed to create metadata for function: {}", componentName, e);
            }
        });

        logger.info("Created metadata for {} functions", metadataMap.size());
        return metadataMap;
    }

    /**
     * Create function metadata from configuration
     */
    private FunctionMetadata createMetadata(String componentName, ServerlessConfig.FunctionConfig config) {
        FunctionMetadata metadata = new FunctionMetadata();
        metadata.setName(config.getName());
        metadata.setComponent(componentName);

        // Set input/output types (for now as strings, later we'll resolve to actual classes)
        try {
            if (config.getInput() != null && !config.getInput().trim().isEmpty()) {
                metadata.setInputType(Class.forName(config.getInput()));
            }
            if (config.getOutput() != null && !config.getOutput().trim().isEmpty()) {
                metadata.setOutputType(Class.forName(config.getOutput()));
            }
        } catch (ClassNotFoundException e) {
            logger.warn("Could not resolve class types for function {}: {}", componentName, e.getMessage());
            // Continue without types - we'll handle this better later
        }

        // Security metadata
        if (config.getSecurity() != null) {
            FunctionMetadata.SecurityMetadata security = new FunctionMetadata.SecurityMetadata();
            security.setGroup(config.getSecurity().getGroup());
            security.setRoles(config.getSecurity().getRoles());
            security.setAuthentication(config.getSecurity().getAuthentication());
            security.setElevated(config.getSecurity().isElevated());
            metadata.setSecurity(security);
        }

        // Deployment metadata
        if (config.getDeployment() != null) {
            FunctionMetadata.DeploymentMetadata deployment = new FunctionMetadata.DeploymentMetadata();
            deployment.setCloud(config.getDeployment().getCloud());
            deployment.setMemory(config.getDeployment().getMemory());
            deployment.setTimeout(config.getDeployment().getTimeout());
            metadata.setDeployment(deployment);
        }

        return metadata;
    }
}
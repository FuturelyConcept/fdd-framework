package com.fdd.core.registry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Function;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Collection;
import java.util.Optional;

/**
 * Registry for Function<T,R> instances with metadata from serverless.yml
 */
public class FunctionRegistry {
    private static final Logger logger = LoggerFactory.getLogger(FunctionRegistry.class);

    private final Map<String, Function<?, ?>> functions = new ConcurrentHashMap<>();
    private final Map<String, FunctionMetadata> metadata = new ConcurrentHashMap<>();

    /**
     * Register a function with its metadata
     */
    public void registerFunction(String componentName, Function<?, ?> function, FunctionMetadata metadata) {
        logger.debug("Registering function: {} with metadata: {}", componentName, metadata);

        this.functions.put(componentName, function);
        this.metadata.put(componentName, metadata);

        logger.info("Successfully registered function: {}", componentName);
    }

    /**
     * Get a function by component name
     */
    @SuppressWarnings("unchecked")
    public <T, R> Optional<Function<T, R>> getFunction(String componentName) {
        Function<?, ?> function = functions.get(componentName);
        return Optional.ofNullable((Function<T, R>) function);
    }

    /**
     * Get metadata for a function
     */
    public Optional<FunctionMetadata> getMetadata(String componentName) {
        return Optional.ofNullable(metadata.get(componentName));
    }

    /**
     * Get all registered function names
     */
    public Collection<String> getFunctionNames() {
        return functions.keySet();
    }

    /**
     * Get all metadata
     */
    public Collection<FunctionMetadata> getAllMetadata() {
        return metadata.values();
    }

    /**
     * Check if a function is registered
     */
    public boolean isRegistered(String componentName) {
        return functions.containsKey(componentName);
    }

    /**
     * Get the count of registered functions
     */
    public int size() {
        return functions.size();
    }

    /**
     * Clear all registered functions (mainly for testing)
     */
    public void clear() {
        functions.clear();
        metadata.clear();
        logger.debug("Cleared all registered functions");
    }
}
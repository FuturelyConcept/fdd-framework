package com.fdd.core.registry;

import java.util.function.Function;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Registry for Function<T,R> instances with metadata from serverless.yml
 */
public class FunctionRegistry {
    private final Map<String, Function<?, ?>> functions = new ConcurrentHashMap<>();
    private final Map<String, FunctionMetadata> metadata = new ConcurrentHashMap<>();
    
    // TODO: Implement function registration and discovery
}

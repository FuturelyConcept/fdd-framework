package com.fdd.core.config;

import com.fdd.core.registry.FunctionRegistry;
import com.fdd.core.registry.FunctionMetadata;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * REST controller for function discovery
 */
@RestController
public class FunctionDiscoveryController {

    private final FunctionRegistry functionRegistry;

    public FunctionDiscoveryController(FunctionRegistry functionRegistry) {
        this.functionRegistry = functionRegistry;
    }

    /**
     * Get all registered functions with their metadata
     */
    @GetMapping("/functions")
    public Map<String, Object> getAllFunctions() {
        Map<String, Object> response = new HashMap<>();

        Collection<FunctionMetadata> allMetadata = functionRegistry.getAllMetadata();
        response.put("count", allMetadata.size());
        response.put("functions", allMetadata);

        return response;
    }

    /**
     * Get specific function metadata
     */
    @GetMapping("/functions/{componentName}")
    public Map<String, Object> getFunction(@PathVariable String componentName) {
        Optional<FunctionMetadata> metadata = functionRegistry.getMetadata(componentName);

        Map<String, Object> response = new HashMap<>();
        if (metadata.isPresent()) {
            response.put("found", true);
            response.put("metadata", metadata.get());
        } else {
            response.put("found", false);
            response.put("error", "Function not found: " + componentName);
        }

        return response;
    }

    /**
     * Health check for the function registry
     */
    @GetMapping("/functions/health")
    public Map<String, Object> health() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("registeredFunctions", functionRegistry.size());
        response.put("functionNames", functionRegistry.getFunctionNames());

        return response;
    }
}
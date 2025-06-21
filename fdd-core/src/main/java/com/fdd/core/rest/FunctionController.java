package com.fdd.core.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fdd.core.registry.FunctionRegistry;
import com.fdd.core.registry.FunctionMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.function.Function;
import java.util.Optional;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Enhanced FDD Function Controller with proper type conversion
 */
@RestController
@RequestMapping("/functions")
public class FunctionController {

    private static final Logger logger = LoggerFactory.getLogger(FunctionController.class);

    @Autowired
    private FunctionRegistry functionRegistry;

    @Autowired
    private ObjectMapper objectMapper;

    /**
     * Execute a function via REST call with proper type conversion
     * POST /functions/{functionName}
     */
    @PostMapping("/{functionName}")
    @SuppressWarnings("unchecked")
    public ResponseEntity<?> executeFunction(
            @PathVariable String functionName,
            @RequestBody(required = false) Object input) {

        logger.debug("🔧 Executing function: {} with input type: {}",
                functionName, input != null ? input.getClass().getSimpleName() : "null");

        try {
            // Get the function from registry
            Optional<Function<Object, Object>> functionOpt = functionRegistry.getFunction(functionName);
            if (functionOpt.isEmpty()) {
                logger.warn("❌ Function not found: {}", functionName);
                return ResponseEntity.notFound().build();
            }

            // Get function metadata to determine input type
            Optional<FunctionMetadata> metadataOpt = functionRegistry.getMetadata(functionName);
            if (metadataOpt.isEmpty()) {
                logger.warn("❌ Function metadata not found: {}", functionName);
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Function metadata not available", "function", functionName));
            }

            Function<Object, Object> function = functionOpt.get();
            FunctionMetadata metadata = metadataOpt.get();

            // Convert input to the correct type if needed
            Object typedInput = convertInputToCorrectType(input, metadata, functionName);

            // Execute the function with properly typed input
            Object result = function.apply(typedInput);

            logger.debug("✅ Function '{}' executed successfully, result type: {}",
                    functionName, result != null ? result.getClass().getSimpleName() : "null");

            return ResponseEntity.ok(result);

        } catch (Exception e) {
            logger.error("❌ Function '{}' execution failed: {}", functionName, e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(Map.of(
                            "error", "Function execution failed",
                            "message", e.getMessage(),
                            "function", functionName,
                            "details", e.getClass().getSimpleName()
                    ));
        }
    }

    /**
     * Convert generic input object to the function's expected input type
     */
    private Object convertInputToCorrectType(Object input, FunctionMetadata metadata, String functionName) {
        try {
            // If input is null, return null
            if (input == null) {
                return null;
            }

            // If no input type specified in metadata, return as-is
            Class<?> inputType = metadata.getInputType();
            if (inputType == null) {
                logger.debug("No input type specified for function {}, using input as-is", functionName);
                return input;
            }

            // If input is already the correct type, return as-is
            if (inputType.isAssignableFrom(input.getClass())) {
                logger.debug("Input already correct type for function {}", functionName);
                return input;
            }

            // Convert using Jackson ObjectMapper
            logger.debug("Converting input from {} to {} for function {}",
                    input.getClass().getSimpleName(), inputType.getSimpleName(), functionName);

            return objectMapper.convertValue(input, inputType);

        } catch (Exception e) {
            logger.error("Failed to convert input type for function {}: {}", functionName, e.getMessage());
            throw new RuntimeException("Input type conversion failed: " + e.getMessage(), e);
        }
    }

    /**
     * Get list of available functions
     * GET /functions
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> listFunctions() {
        try {
            var functionNames = functionRegistry.getFunctionNames();
            var allMetadata = functionRegistry.getAllMetadata();

            List<Map<String, Object>> functions = allMetadata.stream()
                    .map(this::createFunctionInfo)
                    .collect(Collectors.toList());

            Map<String, Object> response = new HashMap<>();
            response.put("functions", functions);
            response.put("count", functions.size());
            response.put("usage", "POST /functions/{functionName} to execute");
            response.put("note", "Each Function<T,R> component becomes a REST endpoint");

            logger.debug("📋 Listed {} available functions", functions.size());
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("❌ Failed to list functions: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Failed to list functions", "message", e.getMessage()));
        }
    }

    /**
     * Get information about a specific function
     * GET /functions/{functionName}
     */
    @GetMapping("/{functionName}")
    public ResponseEntity<Map<String, Object>> getFunctionInfo(@PathVariable String functionName) {
        try {
            Optional<FunctionMetadata> metadata = functionRegistry.getMetadata(functionName);

            if (metadata.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            Map<String, Object> info = createDetailedFunctionInfo(metadata.get());
            return ResponseEntity.ok(info);

        } catch (Exception e) {
            logger.error("❌ Failed to get function info for {}: {}", functionName, e.getMessage());
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Failed to get function info", "message", e.getMessage()));
        }
    }

    /**
     * Health check for the function system
     * GET /functions/health
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        try {
            var functionCount = functionRegistry.size();
            var functionNames = functionRegistry.getFunctionNames();

            Map<String, Object> health = new HashMap<>();
            health.put("status", "UP");
            health.put("functionCount", functionCount);
            health.put("functions", functionNames);
            health.put("message", "FDD Framework operational");
            health.put("timestamp", System.currentTimeMillis());

            return ResponseEntity.ok(health);

        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("status", "DOWN", "error", e.getMessage()));
        }
    }

    /**
     * Create simple function info for listing
     */
    private Map<String, Object> createFunctionInfo(FunctionMetadata metadata) {
        Map<String, Object> info = new HashMap<>();
        info.put("name", metadata.getName() != null ? metadata.getName() : metadata.getComponent());
        info.put("component", metadata.getComponent());
        info.put("endpoint", "/functions/" + metadata.getComponent());

        // Add input/output type info if available
        if (metadata.getInputType() != null) {
            info.put("inputType", metadata.getInputType().getSimpleName());
            info.put("inputTypeFullName", metadata.getInputType().getName());
        }
        if (metadata.getOutputType() != null) {
            info.put("outputType", metadata.getOutputType().getSimpleName());
            info.put("outputTypeFullName", metadata.getOutputType().getName());
        }

        return info;
    }

    /**
     * Create detailed function info including security and deployment metadata
     */
    private Map<String, Object> createDetailedFunctionInfo(FunctionMetadata metadata) {
        Map<String, Object> info = createFunctionInfo(metadata);

        // Add security metadata if available
        if (metadata.getSecurity() != null) {
            Map<String, Object> security = new HashMap<>();
            security.put("group", metadata.getSecurity().getGroup());
            security.put("roles", metadata.getSecurity().getRoles());
            security.put("authentication", metadata.getSecurity().getAuthentication());
            security.put("elevated", metadata.getSecurity().isElevated());
            info.put("security", security);
        }

        // Add deployment metadata if available
        if (metadata.getDeployment() != null) {
            Map<String, Object> deployment = new HashMap<>();
            deployment.put("cloud", metadata.getDeployment().getCloud());
            deployment.put("memory", metadata.getDeployment().getMemory());
            deployment.put("timeout", metadata.getDeployment().getTimeout());
            info.put("deployment", deployment);
        }

        return info;
    }
}
package com.fdd.core.rest;

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
 * Core FDD Function Controller
 *
 * This is the heart of FDD - it exposes every Function<T,R> component as a REST endpoint.
 *
 * Purpose:
 * - Turn Function<T,R> into REST endpoints: POST /functions/{functionName}
 * - Enable function-to-function calls via REST in serverless environment
 * - Provide function discovery: GET /functions
 *
 * In serverless deployment:
 * - Each function becomes a separate Lambda/Azure Function
 * - This controller handles the REST → Function<T,R> mapping
 * - Functions call each other via HTTP instead of direct injection
 */
@RestController
@RequestMapping("/functions")
public class FunctionController {

    private static final Logger logger = LoggerFactory.getLogger(FunctionController.class);

    @Autowired
    private FunctionRegistry functionRegistry;

    /**
     * Execute a function via REST call
     * POST /functions/{functionName}
     *
     * This is the core FDD functionality:
     * Function<T,R> + REST = Serverless-ready endpoint
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

            Function<Object, Object> function = functionOpt.get();

            // Execute the function - this is the magic moment!
            // Function<T,R>.apply(input) → output
            Object result = function.apply(input);

            logger.debug("✅ Function '{}' executed successfully, result type: {}",
                    functionName, result != null ? result.getClass().getSimpleName() : "null");

            return ResponseEntity.ok(result);

        } catch (ClassCastException e) {
            logger.error("❌ Function '{}' type mismatch: {}", functionName, e.getMessage());
            return ResponseEntity.badRequest()
                    .body(Map.of(
                            "error", "Type mismatch",
                            "message", "Input type doesn't match function signature",
                            "function", functionName
                    ));
        } catch (Exception e) {
            logger.error("❌ Function '{}' execution failed: {}", functionName, e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(Map.of(
                            "error", "Function execution failed",
                            "message", e.getMessage(),
                            "function", functionName
                    ));
        }
    }

    /**
     * Get list of available functions
     * GET /functions
     *
     * Simple function discovery - shows what Function<T,R> components are available
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> listFunctions() {
        try {
            var functionNames = functionRegistry.getFunctionNames();
            var allMetadata = functionRegistry.getAllMetadata();

            // Create simple function list with metadata
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
                    .body(Map.of(
                            "error", "Failed to list functions",
                            "message", e.getMessage()
                    ));
        }
    }

    /**
     * Get information about a specific function
     * GET /functions/{functionName}
     *
     * Returns metadata about the function from serverless.yml
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
                    .body(Map.of(
                            "error", "Failed to get function info",
                            "message", e.getMessage()
                    ));
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
                    .body(Map.of(
                            "status", "DOWN",
                            "error", e.getMessage()
                    ));
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
        }
        if (metadata.getOutputType() != null) {
            info.put("outputType", metadata.getOutputType().getSimpleName());
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
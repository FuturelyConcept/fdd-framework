package com.fdd.core.config;

import com.fdd.core.discovery.FunctionDependencyAnalyzer;
import com.fdd.core.discovery.FunctionMetricsTracker;
import com.fdd.core.discovery.FunctionSchemaGenerator;
import com.fdd.core.registry.FunctionRegistry;
import com.fdd.core.registry.FunctionMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;

/**
 * Enhanced REST controller for function discovery with schemas, metrics, and dependencies
 */
@RestController
public class FunctionDiscoveryController {
    private static final Logger logger = LoggerFactory.getLogger(FunctionDiscoveryController.class);

    @Autowired
    private FunctionRegistry functionRegistry;

    @Autowired(required = false)
    private FunctionSchemaGenerator schemaGenerator;

    @Autowired(required = false)
    private FunctionMetricsTracker metricsTracker;

    @Autowired(required = false)
    private FunctionDependencyAnalyzer dependencyAnalyzer;

    /**
     * Get all registered functions with enhanced metadata
     */
    @GetMapping("/functions")
    public Map<String, Object> getAllFunctions() {
        logger.debug("Getting all functions with enhanced metadata");

        Collection<FunctionMetadata> allMetadata = functionRegistry.getAllMetadata();

        List<Map<String, Object>> enhancedFunctions = new ArrayList<>();

        for (FunctionMetadata metadata : allMetadata) {
            Map<String, Object> functionInfo = createEnhancedFunctionInfo(metadata);
            enhancedFunctions.add(functionInfo);
        }

        Map<String, Object> response = new HashMap<>();
        response.put("count", enhancedFunctions.size());
        response.put("functions", enhancedFunctions);
        response.put("summary", createSummary(enhancedFunctions));
        response.put("features", getEnabledFeatures());

        return response;
    }

    /**
     * Get detailed information about a specific function
     */
    @GetMapping("/functions/{componentName}")
    public ResponseEntity<Map<String, Object>> getFunction(@PathVariable String componentName) {
        logger.debug("Getting function details for: {}", componentName);

        Optional<FunctionMetadata> metadata = functionRegistry.getMetadata(componentName);

        Map<String, Object> response = new HashMap<>();
        if (metadata.isPresent()) {
            response.put("found", true);
            response.put("function", createDetailedFunctionInfo(metadata.get()));
            return ResponseEntity.ok(response);
        } else {
            response.put("found", false);
            response.put("error", "Function not found: " + componentName);
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Get function schema information
     */
    @GetMapping("/functions/{componentName}/schema")
    public ResponseEntity<Map<String, Object>> getFunctionSchema(@PathVariable String componentName) {
        logger.debug("Getting schema for function: {}", componentName);

        if (schemaGenerator == null) {
            return ResponseEntity.ok(Map.of("error", "Schema generation not enabled"));
        }

        Optional<FunctionMetadata> metadata = functionRegistry.getMetadata(componentName);

        if (metadata.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        try {
            Map<String, Object> schemaInfo = createSchemaInfo(metadata.get());
            return ResponseEntity.ok(schemaInfo);
        } catch (Exception e) {
            logger.error("Error generating schema for function {}: {}", componentName, e.getMessage());
            return ResponseEntity.ok(Map.of("error", "Schema generation failed: " + e.getMessage()));
        }
    }

    /**
     * Get function metrics
     */
    @GetMapping("/functions/{componentName}/metrics")
    public ResponseEntity<Map<String, Object>> getFunctionMetrics(@PathVariable String componentName) {
        logger.debug("Getting metrics for function: {}", componentName);

        if (metricsTracker == null) {
            return ResponseEntity.ok(Map.of("error", "Metrics tracking not enabled"));
        }

        try {
            FunctionMetricsTracker.FunctionMetrics metrics = metricsTracker.getMetrics(componentName);
            if (metrics == null) {
                return ResponseEntity.ok(Map.of(
                        "error", "No metrics available for function: " + componentName,
                        "note", "Metrics are collected when functions are called"
                ));
            }

            return ResponseEntity.ok(metrics.toMap());
        } catch (Exception e) {
            logger.error("Error getting metrics for function {}: {}", componentName, e.getMessage());
            return ResponseEntity.ok(Map.of("error", "Metrics retrieval failed: " + e.getMessage()));
        }
    }

    /**
     * Get function dependencies
     */
    @GetMapping("/functions/{componentName}/dependencies")
    public ResponseEntity<Map<String, Object>> getFunctionDependencies(@PathVariable String componentName) {
        logger.debug("Getting dependencies for function: {}", componentName);

        if (dependencyAnalyzer == null) {
            return ResponseEntity.ok(Map.of("error", "Dependency analysis not enabled"));
        }

        try {
            FunctionDependencyAnalyzer.FunctionDependencyInfo depInfo =
                    dependencyAnalyzer.getDependencyInfo(componentName);
            return ResponseEntity.ok(depInfo.toMap());
        } catch (Exception e) {
            logger.error("Error getting dependencies for function {}: {}", componentName, e.getMessage());
            return ResponseEntity.ok(Map.of("error", "Dependency analysis failed: " + e.getMessage()));
        }
    }

    /**
     * Get all function dependencies as a graph
     */
    @GetMapping("/functions/dependencies")
    public ResponseEntity<Map<String, Object>> getAllDependencies() {
        logger.debug("Getting all function dependencies");

        if (dependencyAnalyzer == null) {
            return ResponseEntity.ok(Map.of("error", "Dependency analysis not enabled"));
        }

        try {
            Map<String, Object> response = new HashMap<>();
            response.put("dependencies", dependencyAnalyzer.getAllDependencies());
            response.put("cycles", dependencyAnalyzer.findCircularDependencies());
            response.put("leafFunctions", dependencyAnalyzer.getLeafFunctions());
            response.put("rootFunctions", dependencyAnalyzer.getRootFunctions());
            response.put("dotGraph", dependencyAnalyzer.generateDotGraph());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error getting all dependencies: {}", e.getMessage());
            return ResponseEntity.ok(Map.of("error", "Dependency analysis failed: " + e.getMessage()));
        }
    }

    /**
     * Get metrics for all functions
     */
    @GetMapping("/functions/metrics")
    public ResponseEntity<Map<String, Object>> getAllMetrics() {
        logger.debug("Getting metrics for all functions");

        if (metricsTracker == null) {
            return ResponseEntity.ok(Map.of("error", "Metrics tracking not enabled"));
        }

        try {
            Map<String, FunctionMetricsTracker.FunctionMetrics> allMetrics = metricsTracker.getAllMetrics();
            Map<String, Object> response = new HashMap<>();

            Map<String, Object> metricsData = new HashMap<>();
            for (Map.Entry<String, FunctionMetricsTracker.FunctionMetrics> entry : allMetrics.entrySet()) {
                metricsData.put(entry.getKey(), entry.getValue().toMap());
            }

            response.put("metrics", metricsData);
            response.put("summary", createMetricsSummary(allMetrics.values()));
            response.put("count", allMetrics.size());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error getting all metrics: {}", e.getMessage());
            return ResponseEntity.ok(Map.of("error", "Metrics retrieval failed: " + e.getMessage()));
        }
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
        response.put("features", getEnabledFeatures());
        response.put("timestamp", System.currentTimeMillis());

        return response;
    }

    /**
     * Create enhanced function information
     */
    private Map<String, Object> createEnhancedFunctionInfo(FunctionMetadata metadata) {
        Map<String, Object> info = new HashMap<>();
        info.put("name", metadata.getName());
        info.put("component", metadata.getComponent());
        info.put("inputType", metadata.getInputType() != null ? metadata.getInputType().getSimpleName() : null);
        info.put("outputType", metadata.getOutputType() != null ? metadata.getOutputType().getSimpleName() : null);

        // Add security info
        if (metadata.getSecurity() != null) {
            Map<String, Object> security = new HashMap<>();
            security.put("group", metadata.getSecurity().getGroup());
            security.put("roles", metadata.getSecurity().getRoles());
            security.put("authentication", metadata.getSecurity().getAuthentication());
            security.put("elevated", metadata.getSecurity().isElevated());
            info.put("security", security);
        }

        // Add metrics if available
        if (metricsTracker != null) {
            try {
                FunctionMetricsTracker.FunctionMetrics metrics = metricsTracker.getMetrics(metadata.getComponent());
                if (metrics != null) {
                    Map<String, Object> metricsInfo = new HashMap<>();
                    metricsInfo.put("totalCalls", metrics.getTotalCalls());
                    metricsInfo.put("successRate", Math.round(metrics.getSuccessRate() * 100.0) / 100.0);
                    metricsInfo.put("avgDuration", Math.round(metrics.getAverageDuration() * 100.0) / 100.0);
                    info.put("metrics", metricsInfo);
                }
            } catch (Exception e) {
                logger.warn("Error getting metrics for function {}: {}", metadata.getComponent(), e.getMessage());
            }
        }

        // Add dependency info
        if (dependencyAnalyzer != null) {
            try {
                FunctionDependencyAnalyzer.FunctionDependencyInfo depInfo =
                        dependencyAnalyzer.getDependencyInfo(metadata.getComponent());
                Map<String, Object> dependencies = new HashMap<>();
                dependencies.put("callsTo", depInfo.getCallsTo());
                dependencies.put("calledBy", depInfo.getCalledBy());
                dependencies.put("isLeaf", depInfo.isLeafFunction());
                dependencies.put("isRoot", depInfo.isRootFunction());
                info.put("dependencies", dependencies);
            } catch (Exception e) {
                logger.warn("Error getting dependencies for function {}: {}", metadata.getComponent(), e.getMessage());
            }
        }

        return info;
    }

    /**
     * Create detailed function information
     */
    private Map<String, Object> createDetailedFunctionInfo(FunctionMetadata metadata) {
        Map<String, Object> info = createEnhancedFunctionInfo(metadata);

        // Add schema information if available
        if (schemaGenerator != null) {
            try {
                info.put("schema", createSchemaInfo(metadata));
            } catch (Exception e) {
                logger.warn("Error generating schema for function {}: {}", metadata.getComponent(), e.getMessage());
                info.put("schema", Map.of("error", "Schema generation failed"));
            }
        }

        return info;
    }

    /**
     * Create schema information for a function
     */
    private Map<String, Object> createSchemaInfo(FunctionMetadata metadata) {
        Map<String, Object> schemaInfo = new HashMap<>();

        // Generate input schema
        if (metadata.getInputType() != null) {
            schemaInfo.put("input", schemaGenerator.generateSchema(metadata.getInputType()));
            schemaInfo.put("inputExample", schemaGenerator.generateExample(metadata.getInputType()));
        }

        // Generate output schema
        if (metadata.getOutputType() != null) {
            schemaInfo.put("output", schemaGenerator.generateSchema(metadata.getOutputType()));
            schemaInfo.put("outputExample", schemaGenerator.generateExample(metadata.getOutputType()));
        }

        return schemaInfo;
    }

    /**
     * Create summary statistics
     */
    private Map<String, Object> createSummary(List<Map<String, Object>> functions) {
        Map<String, Object> summary = new HashMap<>();
        summary.put("totalFunctions", functions.size());

        // Count by security group
        Map<String, Integer> securityGroups = new HashMap<>();
        for (Map<String, Object> function : functions) {
            if (function.containsKey("security")) {
                Map<String, Object> security = (Map<String, Object>) function.get("security");
                String group = (String) security.get("group");
                if (group != null) {
                    securityGroups.put(group, securityGroups.getOrDefault(group, 0) + 1);
                }
            }
        }
        summary.put("securityGroups", securityGroups);

        return summary;
    }

    /**
     * Create metrics summary
     */
    private Map<String, Object> createMetricsSummary(Collection<FunctionMetricsTracker.FunctionMetrics> metrics) {
        Map<String, Object> summary = new HashMap<>();

        if (metrics.isEmpty()) {
            summary.put("note", "No metrics available - metrics are collected when functions are called");
            return summary;
        }

        int totalCalls = metrics.stream().mapToInt(FunctionMetricsTracker.FunctionMetrics::getTotalCalls).sum();
        double avgSuccessRate = metrics.stream().mapToDouble(FunctionMetricsTracker.FunctionMetrics::getSuccessRate).average().orElse(0.0);
        double avgDuration = metrics.stream().mapToDouble(FunctionMetricsTracker.FunctionMetrics::getAverageDuration).average().orElse(0.0);

        summary.put("totalCalls", totalCalls);
        summary.put("averageSuccessRate", Math.round(avgSuccessRate * 100.0) / 100.0);
        summary.put("averageDuration", Math.round(avgDuration * 100.0) / 100.0);
        summary.put("functionsWithMetrics", metrics.size());

        return summary;
    }

    /**
     * Get enabled features
     */
    private Map<String, Boolean> getEnabledFeatures() {
        Map<String, Boolean> features = new HashMap<>();
        features.put("schemaGeneration", schemaGenerator != null);
        features.put("metricsTracking", metricsTracker != null);
        features.put("dependencyAnalysis", dependencyAnalyzer != null);
        return features;
    }
}
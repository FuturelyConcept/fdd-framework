package com.fdd.core.monitoring;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * REST controller for function metrics and monitoring
 */
@RestController
@RequestMapping("/metrics")
public class FunctionMetricsController {

    @Autowired
    private MetricsCollector metricsCollector;

    /**
     * Get aggregated metrics for all functions
     */
    @GetMapping
    public Map<String, Object> getAllMetrics() {
        Map<String, Object> response = new HashMap<>();
        ConcurrentHashMap<String, MetricsCollector.FunctionStats> allStats = metricsCollector.getAllStats();

        response.put("totalFunctions", allStats.size());
        response.put("functions", allStats);

        // Calculate overall statistics
        long totalCalls = allStats.values().stream()
                .mapToLong(MetricsCollector.FunctionStats::getTotalCalls)
                .sum();

        long totalSuccessful = allStats.values().stream()
                .mapToLong(MetricsCollector.FunctionStats::getSuccessfulCalls)
                .sum();

        response.put("totalCalls", totalCalls);
        response.put("totalSuccessful", totalSuccessful);
        response.put("overallSuccessRate", totalCalls > 0 ? (double) totalSuccessful / totalCalls : 0.0);

        return response;
    }

    /**
     * Get metrics for a specific function
     */
    @GetMapping("/{component}")
    public Map<String, Object> getFunctionMetrics(@PathVariable String component) {
        MetricsCollector.FunctionStats stats = metricsCollector.getStats(component);

        Map<String, Object> response = new HashMap<>();
        if (stats != null) {
            response.put("found", true);
            response.put("component", stats.getComponent());
            response.put("totalCalls", stats.getTotalCalls());
            response.put("successfulCalls", stats.getSuccessfulCalls());
            response.put("failedCalls", stats.getFailedCalls());
            response.put("successRate", stats.getSuccessRate());
            response.put("averageDuration", stats.getAverageDuration());
            response.put("minDuration", stats.getMinDuration());
            response.put("maxDuration", stats.getMaxDuration());
        } else {
            response.put("found", false);
            response.put("error", "No metrics found for function: " + component);
        }

        return response;
    }

    /**
     * Health check for the metrics system
     */
    @GetMapping("/health")
    public Map<String, Object> health() {
        Map<String, Object> response = new HashMap<>();
        ConcurrentHashMap<String, MetricsCollector.FunctionStats> allStats = metricsCollector.getAllStats();

        response.put("status", "UP");
        response.put("trackedFunctions", allStats.size());
        response.put("functionNames", allStats.keySet());

        return response;
    }
}
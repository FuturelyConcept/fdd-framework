package com.fdd.core.discovery;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Tracks performance metrics for function calls
 */
@Component
public class FunctionMetricsTracker {
    private static final Logger logger = LoggerFactory.getLogger(FunctionMetricsTracker.class);

    private final Map<String, FunctionMetrics> metricsMap = new ConcurrentHashMap<>();

    /**
     * Record a successful function call
     */
    public void recordSuccess(String functionName, long durationMs) {
        FunctionMetrics metrics = getOrCreateMetrics(functionName);
        metrics.recordSuccess(durationMs);

        logger.debug("Recorded successful call to {} in {}ms", functionName, durationMs);
    }

    /**
     * Record a failed function call
     */
    public void recordFailure(String functionName, long durationMs, String errorType) {
        FunctionMetrics metrics = getOrCreateMetrics(functionName);
        metrics.recordFailure(durationMs, errorType);

        logger.debug("Recorded failed call to {} in {}ms: {}", functionName, durationMs, errorType);
    }

    /**
     * Get metrics for a specific function
     */
    public FunctionMetrics getMetrics(String functionName) {
        return metricsMap.get(functionName);
    }

    /**
     * Get metrics for all functions
     */
    public Map<String, FunctionMetrics> getAllMetrics() {
        return Map.copyOf(metricsMap);
    }

    /**
     * Get or create metrics for a function
     */
    private FunctionMetrics getOrCreateMetrics(String functionName) {
        return metricsMap.computeIfAbsent(functionName, k -> new FunctionMetrics(functionName));
    }

    /**
     * Reset metrics for a function
     */
    public void resetMetrics(String functionName) {
        metricsMap.remove(functionName);
        logger.info("Reset metrics for function: {}", functionName);
    }

    /**
     * Reset all metrics
     */
    public void resetAllMetrics() {
        metricsMap.clear();
        logger.info("Reset all function metrics");
    }

    /**
     * Metrics for a single function
     */
    public static class FunctionMetrics {
        private final String functionName;
        private final AtomicInteger totalCalls = new AtomicInteger(0);
        private final AtomicInteger successfulCalls = new AtomicInteger(0);
        private final AtomicInteger failedCalls = new AtomicInteger(0);
        private final AtomicLong totalDuration = new AtomicLong(0);
        private final AtomicLong minDuration = new AtomicLong(Long.MAX_VALUE);
        private final AtomicLong maxDuration = new AtomicLong(0);
        private final Map<String, AtomicInteger> errorCounts = new ConcurrentHashMap<>();
        private volatile long firstCallTime = 0;
        private volatile long lastCallTime = 0;

        public FunctionMetrics(String functionName) {
            this.functionName = functionName;
        }

        public void recordSuccess(long durationMs) {
            updateCallTime();
            totalCalls.incrementAndGet();
            successfulCalls.incrementAndGet();
            updateDurationStats(durationMs);
        }

        public void recordFailure(long durationMs, String errorType) {
            updateCallTime();
            totalCalls.incrementAndGet();
            failedCalls.incrementAndGet();
            updateDurationStats(durationMs);

            errorCounts.computeIfAbsent(errorType, k -> new AtomicInteger(0)).incrementAndGet();
        }

        private void updateCallTime() {
            long now = System.currentTimeMillis();
            if (firstCallTime == 0) {
                firstCallTime = now;
            }
            lastCallTime = now;
        }

        private void updateDurationStats(long durationMs) {
            totalDuration.addAndGet(durationMs);

            // Update min duration
            long currentMin = minDuration.get();
            while (durationMs < currentMin && !minDuration.compareAndSet(currentMin, durationMs)) {
                currentMin = minDuration.get();
            }

            // Update max duration
            long currentMax = maxDuration.get();
            while (durationMs > currentMax && !maxDuration.compareAndSet(currentMax, durationMs)) {
                currentMax = maxDuration.get();
            }
        }

        // Getters for metrics
        public String getFunctionName() { return functionName; }
        public int getTotalCalls() { return totalCalls.get(); }
        public int getSuccessfulCalls() { return successfulCalls.get(); }
        public int getFailedCalls() { return failedCalls.get(); }

        public double getSuccessRate() {
            int total = getTotalCalls();
            return total > 0 ? (double) getSuccessfulCalls() / total * 100.0 : 0.0;
        }

        public double getAverageDuration() {
            int total = getTotalCalls();
            return total > 0 ? (double) totalDuration.get() / total : 0.0;
        }

        public long getMinDuration() {
            return minDuration.get() == Long.MAX_VALUE ? 0 : minDuration.get();
        }

        public long getMaxDuration() { return maxDuration.get(); }
        public long getFirstCallTime() { return firstCallTime; }
        public long getLastCallTime() { return lastCallTime; }

        public Map<String, Integer> getErrorCounts() {
            Map<String, Integer> result = new ConcurrentHashMap<>();
            errorCounts.forEach((k, v) -> result.put(k, v.get()));
            return result;
        }

        /**
         * Convert to map for JSON serialization
         */
        public Map<String, Object> toMap() {
            Map<String, Object> map = new ConcurrentHashMap<>();
            map.put("functionName", functionName);
            map.put("totalCalls", getTotalCalls());
            map.put("successfulCalls", getSuccessfulCalls());
            map.put("failedCalls", getFailedCalls());
            map.put("successRate", Math.round(getSuccessRate() * 100.0) / 100.0);
            map.put("averageDuration", Math.round(getAverageDuration() * 100.0) / 100.0);
            map.put("minDuration", getMinDuration());
            map.put("maxDuration", getMaxDuration());
            map.put("firstCallTime", firstCallTime);
            map.put("lastCallTime", lastCallTime);
            map.put("errors", getErrorCounts());
            return map;
        }
    }
}
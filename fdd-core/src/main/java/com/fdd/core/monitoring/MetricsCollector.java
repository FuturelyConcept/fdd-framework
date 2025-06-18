package com.fdd.core.monitoring;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Collects and aggregates function call metrics
 */
@Component
public class MetricsCollector {
    private static final Logger logger = LoggerFactory.getLogger(MetricsCollector.class);

    private final ConcurrentHashMap<String, FunctionStats> functionStats = new ConcurrentHashMap<>();

    public void recordMetrics(FunctionCallMetrics metrics) {
        String key = metrics.getComponent();

        functionStats.computeIfAbsent(key, k -> new FunctionStats(k))
                .recordCall(metrics);

        logger.trace("Recorded metrics for function: {} - {}ms, success: {}",
                key, metrics.getDurationMs(), metrics.isSuccess());
    }

    public FunctionStats getStats(String component) {
        return functionStats.get(component);
    }

    public ConcurrentHashMap<String, FunctionStats> getAllStats() {
        return new ConcurrentHashMap<>(functionStats);
    }

    /**
     * Statistics for a specific function
     */
    public static class FunctionStats {
        private final String component;
        private final AtomicLong totalCalls = new AtomicLong(0);
        private final AtomicLong successfulCalls = new AtomicLong(0);
        private final AtomicLong failedCalls = new AtomicLong(0);
        private final AtomicLong totalDuration = new AtomicLong(0);
        private final AtomicReference<Long> minDuration = new AtomicReference<>(Long.MAX_VALUE);
        private final AtomicReference<Long> maxDuration = new AtomicReference<>(0L);

        public FunctionStats(String component) {
            this.component = component;
        }

        public void recordCall(FunctionCallMetrics metrics) {
            totalCalls.incrementAndGet();
            totalDuration.addAndGet(metrics.getDurationMs());

            if (metrics.isSuccess()) {
                successfulCalls.incrementAndGet();
            } else {
                failedCalls.incrementAndGet();
            }

            // Update min/max duration
            minDuration.updateAndGet(current -> Math.min(current, metrics.getDurationMs()));
            maxDuration.updateAndGet(current -> Math.max(current, metrics.getDurationMs()));
        }

        // Getters
        public String getComponent() { return component; }
        public long getTotalCalls() { return totalCalls.get(); }
        public long getSuccessfulCalls() { return successfulCalls.get(); }
        public long getFailedCalls() { return failedCalls.get(); }
        public double getSuccessRate() {
            long total = totalCalls.get();
            return total > 0 ? (double) successfulCalls.get() / total : 0.0;
        }
        public double getAverageDuration() {
            long total = totalCalls.get();
            return total > 0 ? (double) totalDuration.get() / total : 0.0;
        }
        public long getMinDuration() {
            Long min = minDuration.get();
            return min == Long.MAX_VALUE ? 0 : min;
        }
        public long getMaxDuration() { return maxDuration.get(); }
    }
}
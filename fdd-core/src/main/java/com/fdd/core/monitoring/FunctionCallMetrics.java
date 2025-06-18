// FunctionCallMetrics.java
package com.fdd.core.monitoring;

import java.time.Instant;
import java.util.Map;

/**
 * Metrics data for function calls
 */
public class FunctionCallMetrics {
    private String functionName;
    private String component;
    private Instant startTime;
    private Instant endTime;
    private long durationMs;
    private boolean success;
    private String errorMessage;
    private String userId;
    private Map<String, Object> metadata;

    public FunctionCallMetrics(String functionName, String component) {
        this.functionName = functionName;
        this.component = component;
        this.startTime = Instant.now();
    }

    public void markComplete(boolean success, String errorMessage) {
        this.endTime = Instant.now();
        this.durationMs = endTime.toEpochMilli() - startTime.toEpochMilli();
        this.success = success;
        this.errorMessage = errorMessage;
    }

    // Getters and setters
    public String getFunctionName() { return functionName; }
    public String getComponent() { return component; }
    public Instant getStartTime() { return startTime; }
    public Instant getEndTime() { return endTime; }
    public long getDurationMs() { return durationMs; }
    public boolean isSuccess() { return success; }
    public String getErrorMessage() { return errorMessage; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public Map<String, Object> getMetadata() { return metadata; }
    public void setMetadata(Map<String, Object> metadata) { this.metadata = metadata; }
}

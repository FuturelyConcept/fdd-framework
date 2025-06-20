package com.fdd.core.error;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Global error handler for FDD functions with trace IDs and context
 */
@ControllerAdvice
public class FddGlobalErrorHandler {
    private static final Logger logger = LoggerFactory.getLogger(FddGlobalErrorHandler.class);

    @ExceptionHandler(SecurityException.class)
    public ResponseEntity<Map<String, Object>> handleSecurityException(SecurityException ex) {
        String traceId = UUID.randomUUID().toString().substring(0, 8);

        logger.warn("Security violation [{}]: {}", traceId, ex.getMessage());

        Map<String, Object> response = new HashMap<>();
        response.put("error", "SECURITY_VIOLATION");
        response.put("message", "Access denied");
        response.put("traceId", traceId);
        response.put("timestamp", System.currentTimeMillis());

        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
    }

    @ExceptionHandler(FunctionExecutionException.class)
    public ResponseEntity<Map<String, Object>> handleFunctionException(FunctionExecutionException ex) {
        String traceId = UUID.randomUUID().toString().substring(0, 8);

        logger.error("Function execution failed [{}]: {}", traceId, ex.getMessage(), ex);

        Map<String, Object> response = new HashMap<>();
        response.put("error", "FUNCTION_EXECUTION_FAILED");
        response.put("message", ex.getMessage());
        response.put("functionName", ex.getFunctionName());
        response.put("traceId", traceId);
        response.put("timestamp", System.currentTimeMillis());

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGenericException(Exception ex) {
        String traceId = UUID.randomUUID().toString().substring(0, 8);

        logger.error("Unexpected error [{}]: {}", traceId, ex.getMessage(), ex);

        Map<String, Object> response = new HashMap<>();
        response.put("error", "INTERNAL_ERROR");
        response.put("message", "An unexpected error occurred");
        response.put("traceId", traceId);
        response.put("timestamp", System.currentTimeMillis());

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}

/**
 * Custom exception for function execution failures
 */
class FunctionExecutionException extends RuntimeException {
    private final String functionName;

    public FunctionExecutionException(String functionName, String message, Throwable cause) {
        super(message, cause);
        this.functionName = functionName;
    }

    public String getFunctionName() {
        return functionName;
    }
}
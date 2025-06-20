package com.fdd.core.monitoring;

import com.fdd.core.discovery.FunctionDependencyAnalyzer;
import com.fdd.core.discovery.FunctionMetricsTracker;
import com.fdd.core.registry.FunctionMetadata;
import com.fdd.core.registry.FunctionRegistry;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

/**
 * Enhanced AOP interceptor for function call logging, metrics, and dependency tracking
 */
@Aspect
@Component
public class FunctionMonitoringInterceptor {
    private static final Logger logger = LoggerFactory.getLogger(FunctionMonitoringInterceptor.class);

    @Autowired
    private FunctionRegistry functionRegistry;

    @Autowired
    private FunctionMetricsTracker metricsTracker;

    @Autowired
    private FunctionDependencyAnalyzer dependencyAnalyzer;

    // Thread-local to track the current function call chain
    private static final ThreadLocal<String> currentFunctionContext = new ThreadLocal<>();

    @Around("execution(* java.util.function.Function.apply(..))")
    public Object logFunctionCall(ProceedingJoinPoint joinPoint) throws Throwable {
        String beanName = getBeanName(joinPoint);
        Optional<FunctionMetadata> metadata = functionRegistry.getMetadata(beanName);
        String functionName = metadata.map(FunctionMetadata::getName).orElse("unknown");

        // Track function dependencies
        String callerFunction = currentFunctionContext.get();
        if (callerFunction != null && !callerFunction.equals(beanName)) {
            dependencyAnalyzer.recordFunctionCall(callerFunction, beanName);
        }

        // Set current function context for nested calls
        String previousFunction = currentFunctionContext.get();
        currentFunctionContext.set(beanName);

        // Generate trace ID for this function call
        String traceId = MDC.get("trace.id");
        if (traceId == null) {
            traceId = UUID.randomUUID().toString().substring(0, 8);
        }

        // Add to MDC for structured logging
        MDC.put("function.name", functionName);
        MDC.put("function.component", beanName);
        MDC.put("trace.id", traceId);
        if (callerFunction != null) {
            MDC.put("caller.function", callerFunction);
        }

        long startTime = System.currentTimeMillis();

        logger.info("Function call started: {} (caller: {})", functionName,
                callerFunction != null ? callerFunction : "external");

        try {
            Object result = joinPoint.proceed();
            long duration = System.currentTimeMillis() - startTime;

            // Record successful metrics
            metricsTracker.recordSuccess(beanName, duration);

            logger.info("Function call completed successfully: {} in {}ms", functionName, duration);

            return result;
        } catch (Throwable ex) {
            long duration = System.currentTimeMillis() - startTime;

            // Record failure metrics
            String errorType = ex.getClass().getSimpleName();
            metricsTracker.recordFailure(beanName, duration, errorType);

            logger.error("Function call failed: {} in {}ms - {}: {}",
                    functionName, duration, errorType, ex.getMessage());

            throw ex;
        } finally {
            // Restore previous function context
            if (previousFunction != null) {
                currentFunctionContext.set(previousFunction);
            } else {
                currentFunctionContext.remove();
            }

            // Clean up MDC
            MDC.remove("function.name");
            MDC.remove("function.component");
            MDC.remove("caller.function");
            if (previousFunction == null) {
                MDC.remove("trace.id");
            }
        }
    }

    private String getBeanName(ProceedingJoinPoint joinPoint) {
        // Try to get the actual bean name from Spring context
        String className = joinPoint.getTarget().getClass().getSimpleName();

        // Convert from class name to likely bean name
        if (className.endsWith("Function")) {
            className = className.substring(0, className.length() - 8);
        }

        return Character.toLowerCase(className.charAt(0)) + className.substring(1);
    }
}
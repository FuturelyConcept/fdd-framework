package com.fdd.core.monitoring;

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
 * AOP interceptor for function call logging and observability
 * Simplified to focus on structured logging instead of metrics collection
 */
@Aspect
@Component
public class FunctionMonitoringInterceptor {
    private static final Logger logger = LoggerFactory.getLogger(FunctionMonitoringInterceptor.class);

    @Autowired
    private FunctionRegistry functionRegistry;

    @Around("execution(* java.util.function.Function.apply(..))")
    public Object logFunctionCall(ProceedingJoinPoint joinPoint) throws Throwable {
        String beanName = getBeanName(joinPoint);
        Optional<FunctionMetadata> metadata = functionRegistry.getMetadata(beanName);
        String functionName = metadata.map(FunctionMetadata::getName).orElse("unknown");

        // Generate trace ID for this function call
        String traceId = UUID.randomUUID().toString().substring(0, 8);

        // Add to MDC for structured logging
        MDC.put("function.name", functionName);
        MDC.put("function.component", beanName);
        MDC.put("trace.id", traceId);

        long startTime = System.currentTimeMillis();

        logger.info("Function call started: {}", functionName);

        try {
            Object result = joinPoint.proceed();
            long duration = System.currentTimeMillis() - startTime;

            logger.info("Function call completed successfully: {} in {}ms", functionName, duration);

            return result;
        } catch (Throwable ex) {
            long duration = System.currentTimeMillis() - startTime;

            logger.error("Function call failed: {} in {}ms - {}", functionName, duration, ex.getMessage());

            throw ex;
        } finally {
            // Clean up MDC
            MDC.remove("function.name");
            MDC.remove("function.component");
            MDC.remove("trace.id");
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
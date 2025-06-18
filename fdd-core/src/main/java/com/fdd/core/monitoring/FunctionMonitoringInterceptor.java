// FunctionMonitoringInterceptor.java
package com.fdd.core.monitoring;

import com.fdd.core.registry.FunctionMetadata;
import com.fdd.core.registry.FunctionRegistry;
import com.fdd.core.security.SecurityContextHolder;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * AOP interceptor for function call monitoring and metrics
 */
@Aspect
@Component
public class FunctionMonitoringInterceptor {
    private static final Logger logger = LoggerFactory.getLogger(FunctionMonitoringInterceptor.class);

    @Autowired
    private FunctionRegistry functionRegistry;

    @Autowired
    private MetricsCollector metricsCollector;

    @Around("execution(* java.util.function.Function.apply(..))")
    public Object monitorFunctionCall(ProceedingJoinPoint joinPoint) throws Throwable {
        String beanName = getBeanName(joinPoint);

        Optional<FunctionMetadata> metadata = functionRegistry.getMetadata(beanName);
        String functionName = metadata.map(FunctionMetadata::getName).orElse("unknown");

        FunctionCallMetrics metrics = new FunctionCallMetrics(functionName, beanName);
        metrics.setUserId(SecurityContextHolder.getContext().getUserId());

        logger.debug("Starting function call: {} ({})", functionName, beanName);

        try {
            Object result = joinPoint.proceed();
            metrics.markComplete(true, null);

            logger.debug("Function call completed successfully: {} in {}ms",
                    functionName, metrics.getDurationMs());

            return result;
        } catch (Throwable ex) {
            metrics.markComplete(false, ex.getMessage());

            logger.error("Function call failed: {} in {}ms - {}",
                    functionName, metrics.getDurationMs(), ex.getMessage());

            throw ex;
        } finally {
            metricsCollector.recordMetrics(metrics);
        }
    }

    private String getBeanName(ProceedingJoinPoint joinPoint) {
        return joinPoint.getTarget().getClass().getSimpleName().toLowerCase();
    }
}
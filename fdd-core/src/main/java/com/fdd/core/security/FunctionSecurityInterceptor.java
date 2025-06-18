// FunctionSecurityInterceptor.java
package com.fdd.core.security;

import com.fdd.core.registry.FunctionMetadata;
import com.fdd.core.registry.FunctionRegistry;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * AOP interceptor for function security validation
 */
@Aspect
@Component
public class FunctionSecurityInterceptor {
    private static final Logger logger = LoggerFactory.getLogger(FunctionSecurityInterceptor.class);

    @Autowired
    private FunctionRegistry functionRegistry;

    @Around("execution(* java.util.function.Function.apply(..))")
    public Object validateSecurity(ProceedingJoinPoint joinPoint) throws Throwable {
        String beanName = getBeanName(joinPoint);
        logger.debug("Intercepting function call: {}", beanName);

        Optional<FunctionMetadata> metadata = functionRegistry.getMetadata(beanName);
        if (metadata.isPresent() && metadata.get().getSecurity() != null) {
            validateFunctionAccess(metadata.get());
        }

        return joinPoint.proceed();
    }

    private void validateFunctionAccess(FunctionMetadata metadata) {
        FunctionSecurityContext context = SecurityContextHolder.getContext();
        FunctionMetadata.SecurityMetadata security = metadata.getSecurity();

        // Validate security group access
        if (security.getGroup() != null && !context.canAccessGroup(security.getGroup())) {
            throw new SecurityException("Access denied to security group: " + security.getGroup());
        }

        // Validate required roles
        if (security.getRoles() != null && !security.getRoles().isEmpty()) {
            boolean hasRequiredRole = security.getRoles().stream()
                    .anyMatch(context::hasRole);
            if (!hasRequiredRole) {
                throw new SecurityException("Insufficient roles. Required: " + security.getRoles());
            }
        }

        logger.debug("Security validation passed for function: {}", metadata.getComponent());
    }

    private String getBeanName(ProceedingJoinPoint joinPoint) {
        // Extract bean name from the target object
        return joinPoint.getTarget().getClass().getSimpleName().toLowerCase();
    }
}
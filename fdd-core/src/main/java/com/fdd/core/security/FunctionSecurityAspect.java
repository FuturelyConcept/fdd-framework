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
 * AOP aspect for function-level security validation
 */
@Aspect
@Component
public class FunctionSecurityAspect {
    private static final Logger logger = LoggerFactory.getLogger(FunctionSecurityAspect.class);

    @Autowired
    private FunctionRegistry functionRegistry;

    @Around("execution(* java.util.function.Function.apply(..))")
    public Object validateFunctionAccess(ProceedingJoinPoint joinPoint) throws Throwable {
        String beanName = getBeanName(joinPoint);
        Optional<FunctionMetadata> metadata = functionRegistry.getMetadata(beanName);

        if (metadata.isEmpty()) {
            logger.debug("No metadata found for function: {}, allowing access", beanName);
            return joinPoint.proceed();
        }

        FunctionMetadata.SecurityMetadata security = metadata.get().getSecurity();
        if (security == null) {
            logger.debug("No security configuration for function: {}, allowing access", beanName);
            return joinPoint.proceed();
        }

        // Get current security context
        FunctionSecurityContext securityContext = SecurityContextHolder.getContext();
        if (securityContext.getUserId() == null) {
            logger.debug("No security context found for function: {}, allowing access (may be internal call)", beanName);
            return joinPoint.proceed();
        }

        // Validate security group access
        if (security.getGroup() != null && !security.getGroup().equals(securityContext.getSecurityGroup())) {
            // Check if user has ADMIN role which can access any group
            if (!securityContext.hasRole("ADMIN")) {
                String error = String.format("Access denied: User %s cannot access security group %s",
                        securityContext.getUserId(), security.getGroup());
                logger.warn(error);
                throw new SecurityException(error);
            }
        }

        // Validate role access
        if (security.getRoles() != null && !security.getRoles().isEmpty()) {
            boolean hasRequiredRole = security.getRoles().stream()
                    .anyMatch(securityContext::hasRole);

            // ADMIN role can access any function
            if (!hasRequiredRole && !securityContext.hasRole("ADMIN")) {
                String error = String.format("Access denied: User %s lacks required roles %s for function %s",
                        securityContext.getUserId(), security.getRoles(), beanName);
                logger.warn(error);
                throw new SecurityException(error);
            }
        }

        logger.debug("Security validation passed for user {} accessing function {}",
                securityContext.getUserId(), beanName);

        return joinPoint.proceed();
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
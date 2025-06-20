package com.fdd.core.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Arrays;
import java.util.List;

/**
 * HTTP interceptor for JWT validation and security context setup
 */
@Component
public class JwtSecurityInterceptor implements HandlerInterceptor {
    private static final Logger logger = LoggerFactory.getLogger(JwtSecurityInterceptor.class);

    @Autowired
    private JwtValidator jwtValidator;

    // Paths that don't require authentication
    private final List<String> publicPaths = Arrays.asList(
            "/demo/test",
            "/functions",
            "/functions/health",
            "/auth/generate-token" // For testing
    );

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {

        String path = request.getRequestURI();

        // Skip authentication for public paths
        if (isPublicPath(path)) {
            logger.debug("Skipping authentication for public path: {}", path);
            return true;
        }

        // Extract JWT token from Authorization header
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || authHeader.trim().isEmpty()) {
            logger.warn("Missing Authorization header for protected path: {}", path);
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("{\"error\":\"Missing Authorization header\"}");
            return false;
        }

        // Validate JWT token
        JwtValidator.JwtValidationResult validationResult = jwtValidator.validateToken(authHeader);
        if (!validationResult.isValid()) {
            logger.warn("JWT validation failed for path {}: {}", path, validationResult.getErrorMessage());
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("{\"error\":\"" + validationResult.getErrorMessage() + "\"}");
            return false;
        }

        // Set up security context for this request
        FunctionSecurityContext securityContext = new FunctionSecurityContext(
                validationResult.getUserId(),
                validationResult.getRoles(),
                validationResult.getSecurityGroup()
        );
        securityContext.setClaims(validationResult.getClaims());

        SecurityContextHolder.setContext(securityContext);

        logger.info("Authentication successful for user: {} accessing: {}",
                validationResult.getUserId(), path);

        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
                                Object handler, Exception ex) throws Exception {
        // Clean up security context
        SecurityContextHolder.clearContext();
    }

    private boolean isPublicPath(String path) {
        return publicPaths.stream().anyMatch(path::startsWith);
    }
}
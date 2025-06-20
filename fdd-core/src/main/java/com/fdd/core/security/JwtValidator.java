package com.fdd.core.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * JWT token validator and generator for FDD functions
 */
@Component
public class JwtValidator {
    private static final Logger logger = LoggerFactory.getLogger(JwtValidator.class);

    // For demo purposes - in production this would come from configuration
    private final SecretKey secretKey = Keys.secretKeyFor(SignatureAlgorithm.HS256);
    private final String issuer = "fdd-framework";
    private final String audience = "fdd-functions";

    /**
     * Validate JWT token and extract claims
     */
    public JwtValidationResult validateToken(String token) {
        try {
            if (token == null || token.trim().isEmpty()) {
                return JwtValidationResult.invalid("Token is empty");
            }

            // Remove "Bearer " prefix if present
            if (token.startsWith("Bearer ")) {
                token = token.substring(7);
            }

            Claims claims = Jwts.parser()
                    .setSigningKey(secretKey)
                    .requireIssuer(issuer)
                    .requireAudience(audience)
                    .parseClaimsJws(token)
                    .getBody();

            // Extract user information
            String userId = claims.getSubject();
            List<String> roles = claims.get("roles", List.class);
            String securityGroup = claims.get("group", String.class);

            logger.debug("Successfully validated JWT for user: {}", userId);

            return JwtValidationResult.valid(userId, roles, securityGroup, claims);

        } catch (Exception e) {
            logger.warn("JWT validation failed: {}", e.getMessage());
            return JwtValidationResult.invalid("Invalid token: " + e.getMessage());
        }
    }

    /**
     * Generate JWT token for testing purposes
     */
    public String generateToken(String userId, List<String> roles, String securityGroup) {
        Instant now = Instant.now();

        return Jwts.builder()
                .setIssuer(issuer)
                .setAudience(audience)
                .setSubject(userId)
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(now.plus(1, ChronoUnit.HOURS)))
                .claim("roles", roles)
                .claim("group", securityGroup)
                .signWith(secretKey, SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * JWT validation result
     */
    public static class JwtValidationResult {
        private final boolean valid;
        private final String userId;
        private final List<String> roles;
        private final String securityGroup;
        private final Map<String, Object> claims;
        private final String errorMessage;

        private JwtValidationResult(boolean valid, String userId, List<String> roles,
                                    String securityGroup, Map<String, Object> claims, String errorMessage) {
            this.valid = valid;
            this.userId = userId;
            this.roles = roles;
            this.securityGroup = securityGroup;
            this.claims = claims;
            this.errorMessage = errorMessage;
        }

        public static JwtValidationResult valid(String userId, List<String> roles,
                                                String securityGroup, Claims claims) {
            return new JwtValidationResult(true, userId, roles, securityGroup,
                    Map.copyOf(claims), null);
        }

        public static JwtValidationResult invalid(String errorMessage) {
            return new JwtValidationResult(false, null, null, null, null, errorMessage);
        }

        // Getters
        public boolean isValid() { return valid; }
        public String getUserId() { return userId; }
        public List<String> getRoles() { return roles; }
        public String getSecurityGroup() { return securityGroup; }
        public Map<String, Object> getClaims() { return claims; }
        public String getErrorMessage() { return errorMessage; }
    }
}
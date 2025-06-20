package com.fdd.core.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Controller for generating test JWT tokens
 */
@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private JwtValidator jwtValidator;

    /**
     * Generate JWT token for testing purposes
     */
    @PostMapping("/generate-token")
    public Map<String, Object> generateToken(@RequestBody TokenRequest request) {
        String token = jwtValidator.generateToken(
                request.getUserId(),
                request.getRoles(),
                request.getSecurityGroup()
        );

        Map<String, Object> response = new HashMap<>();
        response.put("token", token);
        response.put("userId", request.getUserId());
        response.put("roles", request.getRoles());
        response.put("securityGroup", request.getSecurityGroup());
        response.put("usage", "Include this token in Authorization header: Bearer " + token);

        return response;
    }

    /**
     * Get predefined test tokens for common scenarios
     */
    @GetMapping("/test-tokens")
    public Map<String, Object> getTestTokens() {
        Map<String, Object> tokens = new HashMap<>();

        // Admin token - can access everything
        String adminToken = jwtValidator.generateToken(
                "admin-user",
                Arrays.asList("ADMIN", "USER_VALIDATOR", "PAYMENT_PROCESSOR", "INVENTORY_CHECKER"),
                "admin"
        );
        tokens.put("admin", Map.of(
                "token", adminToken,
                "description", "Admin user with access to all functions",
                "header", "Authorization: Bearer " + adminToken
        ));

        // Regular user token - limited access
        String userToken = jwtValidator.generateToken(
                "john-doe",
                Arrays.asList("USER_VALIDATOR"),
                "user-management"
        );
        tokens.put("user", Map.of(
                "token", userToken,
                "description", "Regular user with user validation access only",
                "header", "Authorization: Bearer " + userToken
        ));

        // Payment processor token
        String paymentToken = jwtValidator.generateToken(
                "payment-service",
                Arrays.asList("PAYMENT_PROCESSOR", "USER_VALIDATOR"),
                "financial-operations"
        );
        tokens.put("payment", Map.of(
                "token", paymentToken,
                "description", "Payment service with financial operations access",
                "header", "Authorization: Bearer " + paymentToken
        ));

        return tokens;
    }

    /**
     * Validate a token (for testing)
     */
    @PostMapping("/validate-token")
    public Map<String, Object> validateToken(@RequestBody Map<String, String> request) {
        String token = request.get("token");
        JwtValidator.JwtValidationResult result = jwtValidator.validateToken(token);

        Map<String, Object> response = new HashMap<>();
        response.put("valid", result.isValid());

        if (result.isValid()) {
            response.put("userId", result.getUserId());
            response.put("roles", result.getRoles());
            response.put("securityGroup", result.getSecurityGroup());
        } else {
            response.put("error", result.getErrorMessage());
        }

        return response;
    }

    /**
     * Token generation request
     */
    public static class TokenRequest {
        private String userId;
        private List<String> roles;
        private String securityGroup;

        public TokenRequest() {}

        public String getUserId() { return userId; }
        public void setUserId(String userId) { this.userId = userId; }

        public List<String> getRoles() { return roles; }
        public void setRoles(List<String> roles) { this.roles = roles; }

        public String getSecurityGroup() { return securityGroup; }
        public void setSecurityGroup(String securityGroup) { this.securityGroup = securityGroup; }
    }
}
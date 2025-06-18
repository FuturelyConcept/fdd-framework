// FunctionSecurityContext.java
package com.fdd.core.security;

import java.util.List;
import java.util.Map;

/**
 * Security context that flows between function calls
 */
public class FunctionSecurityContext {
    private String userId;
    private String sessionId;
    private List<String> roles;
    private String securityGroup;
    private Map<String, Object> claims;
    private long timestamp;

    public FunctionSecurityContext() {
        this.timestamp = System.currentTimeMillis();
    }

    public FunctionSecurityContext(String userId, List<String> roles, String securityGroup) {
        this();
        this.userId = userId;
        this.roles = roles;
        this.securityGroup = securityGroup;
    }

    public boolean hasRole(String role) {
        return roles != null && roles.contains(role);
    }

    public boolean canAccessGroup(String group) {
        return securityGroup != null && securityGroup.equals(group);
    }

    // Getters and setters
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getSessionId() { return sessionId; }
    public void setSessionId(String sessionId) { this.sessionId = sessionId; }

    public List<String> getRoles() { return roles; }
    public void setRoles(List<String> roles) { this.roles = roles; }

    public String getSecurityGroup() { return securityGroup; }
    public void setSecurityGroup(String securityGroup) { this.securityGroup = securityGroup; }

    public Map<String, Object> getClaims() { return claims; }
    public void setClaims(Map<String, Object> claims) { this.claims = claims; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
}
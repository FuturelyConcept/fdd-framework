// SecurityContextHolder.java
package com.fdd.core.security;

/**
 * Thread-local holder for security context
 */
public class SecurityContextHolder {
    private static final ThreadLocal<FunctionSecurityContext> contextHolder = new ThreadLocal<>();

    public static void setContext(FunctionSecurityContext context) {
        contextHolder.set(context);
    }

    public static FunctionSecurityContext getContext() {
        FunctionSecurityContext context = contextHolder.get();
        if (context == null) {
            context = new FunctionSecurityContext();
            contextHolder.set(context);
        }
        return context;
    }

    public static void clearContext() {
        contextHolder.remove();
    }
}
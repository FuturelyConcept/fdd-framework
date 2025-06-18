package com.fdd.demo.domain;

/**
 * Result of order processing
 */
public class OrderResult {
    private boolean success;
    private String orderId;
    private String message;

    private OrderResult(boolean success, String orderId, String message) {
        this.success = success;
        this.orderId = orderId;
        this.message = message;
    }

    public static OrderResult success(String orderId) {
        return new OrderResult(true, orderId, "Order created successfully");
    }

    public static OrderResult failed(String message) {
        return new OrderResult(false, null, message);
    }

    // Getters
    public boolean isSuccess() { return success; }
    public String getOrderId() { return orderId; }
    public String getMessage() { return message; }
}
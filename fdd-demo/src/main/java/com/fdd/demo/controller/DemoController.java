package com.fdd.demo.controller;

import com.fdd.demo.domain.*;
import com.fdd.demo.functions.OrderProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.function.Function;

/**
 * Enhanced demo controller showing comprehensive FDD functionality
 * Demonstrates individual function testing and function composition
 */
@RestController
@RequestMapping("/demo")
public class DemoController {

    @Autowired
    private OrderProcessor orderProcessor;

    // Direct function injection for individual testing
    @Autowired
    @Qualifier("userValidator")
    private Function<UserData, ValidationResult> userValidator;

    @Autowired
    @Qualifier("inventoryChecker")
    private Function<InventoryCheckRequest, InventoryResult> inventoryChecker;

    @Autowired
    @Qualifier("paymentProcessor")
    private Function<PaymentRequest, PaymentResult> paymentProcessor;

    /**
     * Test user validation function directly
     */
    @PostMapping("/validate-user")
    public ValidationResult validateUser(@RequestBody UserData userData) {
        return userValidator.apply(userData);
    }

    /**
     * Test inventory checking function directly
     */
    @PostMapping("/check-inventory")
    public InventoryResult checkInventory(@RequestBody InventoryCheckRequest request) {
        return inventoryChecker.apply(request);
    }

    /**
     * Test payment processing function directly
     */
    @PostMapping("/process-payment")
    public PaymentResult processPayment(@RequestBody PaymentRequest request) {
        return paymentProcessor.apply(request);
    }

    /**
     * Test complete order processing with function composition
     * This demonstrates the power of FDD: complex workflows built from simple, reusable functions
     */
    @PostMapping("/create-order")
    public OrderResult createOrder(@RequestBody CreateOrderRequest request) {
        return orderProcessor.createOrder(request);
    }

    /**
     * Quick test endpoint
     */
    @GetMapping("/test")
    public String test() {
        return "FDD Demo is running! Available endpoints:\n" +
                "- POST /demo/validate-user\n" +
                "- POST /demo/check-inventory\n" +
                "- POST /demo/process-payment\n" +
                "- POST /demo/create-order\n" +
                "- GET /functions (function discovery)\n";
    }

    /**
     * Demonstrate error handling in function composition
     */
    @PostMapping("/test-error-handling")
    public OrderResult testErrorHandling() {
        // This will fail at user validation step
        UserData invalidUser = new UserData("", "invalid-email", 15);
        CreateOrderRequest request = new CreateOrderRequest(invalidUser, "product-123", 50);

        return orderProcessor.createOrder(request);
    }

    /**
     * Get sample data for testing
     */
    @GetMapping("/sample-data")
    public SampleData getSampleData() {
        return new SampleData();
    }

    /**
     * Test different scenarios with pre-built requests
     */
    @PostMapping("/test-scenarios/{scenario}")
    public Object testScenario(@PathVariable String scenario) {
        SampleData samples = new SampleData();

        switch (scenario.toLowerCase()) {
            case "valid-user":
                return userValidator.apply(samples.validUser);

            case "invalid-user":
                return userValidator.apply(samples.invalidUser);

            case "available-inventory":
                return inventoryChecker.apply(samples.inventoryRequest);

            case "excessive-inventory":
                return inventoryChecker.apply(samples.excessiveInventoryRequest);

            case "successful-payment":
                return paymentProcessor.apply(samples.paymentRequest);

            case "successful-order":
                return orderProcessor.createOrder(samples.orderRequest);

            case "failed-order-user":
                CreateOrderRequest failedUserOrder = new CreateOrderRequest(
                        samples.invalidUser, "product-123", 50, "CARD");
                return orderProcessor.createOrder(failedUserOrder);

            case "failed-order-inventory":
                CreateOrderRequest failedInventoryOrder = new CreateOrderRequest(
                        samples.validUser, "product-123", 150, "CARD");
                return orderProcessor.createOrder(failedInventoryOrder);

            case "failed-order-payment":
                CreateOrderRequest failedPaymentOrder = new CreateOrderRequest(
                        samples.validUser, "product-123", 2000, "CARD"); // Too expensive
                return orderProcessor.createOrder(failedPaymentOrder);

            default:
                return "Unknown scenario. Available scenarios: " +
                        "valid-user, invalid-user, available-inventory, excessive-inventory, " +
                        "successful-payment, successful-order, failed-order-user, " +
                        "failed-order-inventory, failed-order-payment";
        }
    }

    /**
     * Sample data for easy testing
     */
    public static class SampleData {
        public UserData validUser = new UserData("John Doe", "john@example.com", 25);
        public UserData invalidUser = new UserData("Jane", "invalid-email", 17);
        public InventoryCheckRequest inventoryRequest = new InventoryCheckRequest("product-123", 50);
        public InventoryCheckRequest excessiveInventoryRequest = new InventoryCheckRequest("product-123", 150);
        public PaymentRequest paymentRequest = new PaymentRequest(
                "john-doe",
                new BigDecimal("500.00"),
                "USD",
                "CARD",
                "order-123"
        );
        public CreateOrderRequest orderRequest = new CreateOrderRequest(
                validUser,
                "product-123",
                50,
                "CARD"
        );
    }
}
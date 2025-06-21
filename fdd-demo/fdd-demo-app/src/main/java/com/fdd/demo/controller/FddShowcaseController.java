package com.fdd.demo.controller;

import com.fdd.demo.domain.*;
import com.fdd.demo.functions.OrderProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.*;

import java.util.function.Function;

/**
 * Simple controller to showcase FDD function composition
 */
@RestController
@RequestMapping("/fdd-showcase")
public class FddShowcaseController {

    // THE CORE POWER: Type-safe function autowiring!
    @Autowired @Qualifier("userValidator")
    private Function<UserData, ValidationResult> userValidator;

    @Autowired @Qualifier("inventoryChecker")
    private Function<InventoryCheckRequest, InventoryResult> inventoryChecker;

    @Autowired @Qualifier("paymentProcessor")
    private Function<PaymentRequest, PaymentResult> paymentProcessor;

    @Autowired
    private OrderProcessor orderProcessor;

    /**
     * Showcase individual function calls
     */
    @PostMapping("/test-user-validation")
    public ValidationResult testUserValidation(@RequestBody UserData userData) {
        // Direct function call - type-safe, compile-time checked!
        return userValidator.apply(userData);
    }

    @PostMapping("/test-inventory")
    public InventoryResult testInventory(@RequestBody InventoryCheckRequest request) {
        return inventoryChecker.apply(request);
    }

    @PostMapping("/test-payment")
    public PaymentResult testPayment(@RequestBody PaymentRequest request) {
        return paymentProcessor.apply(request);
    }

    /**
     * Showcase function composition - the real power!
     */
    @PostMapping("/test-complete-order")
    public OrderResult testCompleteOrder(@RequestBody CreateOrderRequest request) {
        // This demonstrates multiple functions working together
        return orderProcessor.createOrder(request);
    }

    /**
     * Get sample test data
     */
    @GetMapping("/sample-data")
    public SampleTestData getSampleData() {
        return new SampleTestData();
    }

    /**
     * Sample data for easy testing
     */
    public static class SampleTestData {
        public UserData validUser = new UserData("John Doe", "john@example.com", 25);
        public UserData invalidUser = new UserData("Jane", "invalid-email", 17);

        public InventoryCheckRequest smallOrder = new InventoryCheckRequest("product-123", 50);
        public InventoryCheckRequest largeOrder = new InventoryCheckRequest("product-123", 150);

        public CreateOrderRequest completeOrder = new CreateOrderRequest(
                validUser, "product-123", 50, "CARD"
        );

        public CreateOrderRequest failingOrder = new CreateOrderRequest(
                invalidUser, "product-123", 50, "CARD"
        );
    }
}
package com.fdd.lambda.functions;

import com.fdd.demo.domain.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.function.Function;

/**
 * 🚀 REVOLUTIONARY FDD ORDER PROCESSOR! 🚀
 *
 * This demonstrates the CORE FDD concept:
 * - Developer writes pure business logic with @Autowired functions
 * - FDD framework handles cross-Lambda calls transparently
 * - Same code works locally AND in distributed Lambda environment
 * - NO knowledge of Lambda URLs, HTTP calls, or AWS APIs required!
 */
@Component("orderProcessor")
public class OrderProcessorFunction implements Function<CreateOrderRequest, OrderResult> {

    // 🎯 THE MAGIC: These functions might be running on different Lambda instances!
    // The FDD framework automatically handles cross-Lambda calls
    @Autowired
    @Qualifier("userValidator")
    private Function<UserData, ValidationResult> userValidator;

    @Autowired
    @Qualifier("inventoryChecker")
    private Function<InventoryCheckRequest, InventoryResult> inventoryChecker;

    @Autowired
    @Qualifier("paymentProcessor")
    private Function<PaymentRequest, PaymentResult> paymentProcessor;

    @Override
    public OrderResult apply(CreateOrderRequest request) {
        System.out.println("🚀 FDD OrderProcessor starting...");

        // Step 1: Validate user data
        // This might call userValidator Lambda behind the scenes!
        System.out.println("👤 Validating user...");
        ValidationResult validation = userValidator.apply(request.getUserData());
        if (!validation.isValid()) {
            return OrderResult.failed("User validation failed: " + validation.getMessage());
        }
        System.out.println("✅ User validation passed");

        // Step 2: Check inventory availability
        // This might call inventoryChecker Lambda behind the scenes!
        System.out.println("📦 Checking inventory...");
        InventoryCheckRequest inventoryRequest = new InventoryCheckRequest(
                request.getProductId(),
                request.getQuantity()
        );
        InventoryResult inventoryResult = inventoryChecker.apply(inventoryRequest);
        if (!inventoryResult.isAvailable()) {
            return OrderResult.failed("Inventory check failed: " + inventoryResult.getMessage());
        }
        System.out.println("✅ Inventory check passed");

        // Step 3: Process payment
        // This might call paymentProcessor Lambda behind the scenes!
        System.out.println("💳 Processing payment...");
        PaymentRequest paymentRequest = new PaymentRequest(
                request.getUserData().getName(),
                calculateOrderTotal(request),
                "USD",
                request.getPaymentMethod() != null ? request.getPaymentMethod() : "CARD",
                "temp-order-id"
        );

        PaymentResult paymentResult = paymentProcessor.apply(paymentRequest);
        if (!paymentResult.isSuccess()) {
            return OrderResult.failed("Payment processing failed: " + paymentResult.getMessage());
        }
        System.out.println("✅ Payment processed");

        // Step 4: Create order
        String orderId = "fdd-cross-lambda-order-" + System.currentTimeMillis();
        System.out.println("🎉 Order created: " + orderId);

        return OrderResult.success(orderId, paymentResult.getTransactionId());
    }

    private BigDecimal calculateOrderTotal(CreateOrderRequest request) {
        BigDecimal unitPrice = new BigDecimal("10.00");
        return unitPrice.multiply(new BigDecimal(request.getQuantity()));
    }
}
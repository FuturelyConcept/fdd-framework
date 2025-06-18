package com.fdd.demo.functions;

import com.fdd.demo.domain.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.function.Function;

/**
 * Complete order processor that demonstrates complex function composition
 * Shows the power of FDD: type-safe composition with zero framework overhead
 */
@Component
public class OrderProcessor {

    // Type-safe function injection - just like regular Spring beans!
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
     * Process an order by composing multiple functions in a workflow
     * This demonstrates the core FDD principle: complex business logic
     * built from simple, reusable, type-safe functions
     */
    public OrderResult createOrder(CreateOrderRequest request) {
        // Step 1: Validate user data
        ValidationResult validation = userValidator.apply(request.getUserData());
        if (!validation.isValid()) {
            return OrderResult.failed("User validation failed: " + validation.getMessage());
        }

        // Step 2: Check inventory availability
        InventoryCheckRequest inventoryRequest = new InventoryCheckRequest(
                request.getProductId(),
                request.getQuantity()
        );
        InventoryResult inventoryResult = inventoryChecker.apply(inventoryRequest);
        if (!inventoryResult.isAvailable()) {
            return OrderResult.failed("Inventory check failed: " + inventoryResult.getMessage());
        }

        // Step 3: Process payment
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

        // Step 4: Create order (simplified - in real world this would save to DB)
        String orderId = "order-" + System.currentTimeMillis();
        return OrderResult.success(orderId, paymentResult.getTransactionId());
    }

    /**
     * Calculate order total (simplified pricing logic)
     */
    private BigDecimal calculateOrderTotal(CreateOrderRequest request) {
        // Simple pricing: $10 per unit
        BigDecimal unitPrice = new BigDecimal("10.00");
        return unitPrice.multiply(new BigDecimal(request.getQuantity()));
    }
}
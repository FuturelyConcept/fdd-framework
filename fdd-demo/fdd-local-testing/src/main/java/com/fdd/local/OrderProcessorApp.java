// fdd-demo/fdd-local-testing/src/main/java/com/fdd/local/OrderProcessorApp.java
package com.fdd.local;

import com.fdd.demo.domain.*;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.context.annotation.Bean;
import java.math.BigDecimal;

@SpringBootApplication(scanBasePackages = {
        "com.fdd.core",
        "com.fdd.starter",
        "com.fdd.demo.functions"
})
@RestController
public class OrderProcessorApp {

    private final RestTemplate restTemplate = new RestTemplate();

    // Local service URLs
    private static final String USER_VALIDATOR_URL = "http://localhost:8081";
    private static final String INVENTORY_CHECKER_URL = "http://localhost:8082";
    private static final String PAYMENT_PROCESSOR_URL = "http://localhost:8083";

    @PostMapping("/")
    public OrderResult processOrder(@RequestBody CreateOrderRequest request) {
        System.out.println("🛒 OrderProcessor received order for: " + request.getUserData().getName());

        try {
            // Step 1: Validate user data
            System.out.println("👤 Calling UserValidator...");
            ValidationResult validation = restTemplate.postForObject(
                    USER_VALIDATOR_URL,
                    request.getUserData(),
                    ValidationResult.class
            );

            if (validation == null || !validation.isValid()) {
                return OrderResult.failed("User validation failed: " +
                        (validation != null ? validation.getMessage() : "Unknown error"));
            }
            System.out.println("✅ User validation passed");

            // Step 2: Check inventory availability
            System.out.println("📦 Calling InventoryChecker...");
            InventoryCheckRequest inventoryRequest = new InventoryCheckRequest(
                    request.getProductId(),
                    request.getQuantity()
            );
            InventoryResult inventoryResult = restTemplate.postForObject(
                    INVENTORY_CHECKER_URL,
                    inventoryRequest,
                    InventoryResult.class
            );

            if (inventoryResult == null || !inventoryResult.isAvailable()) {
                return OrderResult.failed("Inventory check failed: " +
                        (inventoryResult != null ? inventoryResult.getMessage() : "Unknown error"));
            }
            System.out.println("✅ Inventory check passed");

            // Step 3: Process payment
            System.out.println("💳 Calling PaymentProcessor...");
            PaymentRequest paymentRequest = new PaymentRequest(
                    request.getUserData().getName(),
                    calculateOrderTotal(request),
                    "USD",
                    request.getPaymentMethod() != null ? request.getPaymentMethod() : "CARD",
                    "temp-order-id"
            );

            PaymentResult paymentResult = restTemplate.postForObject(
                    PAYMENT_PROCESSOR_URL,
                    paymentRequest,
                    PaymentResult.class
            );

            if (paymentResult == null || !paymentResult.isSuccess()) {
                return OrderResult.failed("Payment processing failed: " +
                        (paymentResult != null ? paymentResult.getMessage() : "Unknown error"));
            }
            System.out.println("✅ Payment processed");

            // Step 4: Create order
            String orderId = "local-order-" + System.currentTimeMillis();
            System.out.println("🎉 Order created: " + orderId);

            return OrderResult.success(orderId, paymentResult.getTransactionId());

        } catch (Exception e) {
            System.err.println("❌ Order processing failed: " + e.getMessage());
            e.printStackTrace();
            return OrderResult.failed("Order processing error: " + e.getMessage());
        }
    }

    private BigDecimal calculateOrderTotal(CreateOrderRequest request) {
        BigDecimal unitPrice = new BigDecimal("10.00");
        return unitPrice.multiply(new BigDecimal(request.getQuantity()));
    }

    @GetMapping("/health")
    public String health() {
        return "OrderProcessor OK";
    }

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    public static void main(String[] args) {
        System.setProperty("server.port", "8084");
        System.out.println("🚀 Starting OrderProcessor on port 8084");
        SpringApplication.run(OrderProcessorApp.class, args);
    }
}
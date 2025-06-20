package com.fdd.demo;

import com.fdd.demo.domain.*;
import com.fdd.demo.functions.OrderProcessor;
import com.fdd.core.registry.FunctionRegistry;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Comprehensive integration test for FDD Demo Application
 */
@SpringBootTest(classes = FddDemoApplication.class)
@TestPropertySource(properties = {
        "fdd.function.enabled=true",
        "fdd.function.discovery.enabled=true",
        "fdd.function.monitoring.enabled=false", // Disable monitoring for tests
        "logging.level.com.fdd=DEBUG"
})
class FddDemoApplicationTest {

    @Autowired
    private FunctionRegistry functionRegistry;

    @Autowired
    @Qualifier("userValidator")
    private Function<UserData, ValidationResult> userValidator;

    @Autowired
    @Qualifier("inventoryChecker")
    private Function<InventoryCheckRequest, InventoryResult> inventoryChecker;

    @Autowired
    @Qualifier("paymentProcessor")
    private Function<PaymentRequest, PaymentResult> paymentProcessor;

    @Autowired
    private OrderProcessor orderProcessor;

    @Test
    void contextLoads() {
        assertThat(functionRegistry).isNotNull();
        assertThat(userValidator).isNotNull();
        assertThat(inventoryChecker).isNotNull();
        assertThat(paymentProcessor).isNotNull();
        assertThat(orderProcessor).isNotNull();
    }

    @Test
    void functionRegistryContainsAllFunctions() {
        assertThat(functionRegistry.isRegistered("userValidator")).isTrue();
        assertThat(functionRegistry.isRegistered("inventoryChecker")).isTrue();
        assertThat(functionRegistry.isRegistered("paymentProcessor")).isTrue();
        assertThat(functionRegistry.size()).isGreaterThanOrEqualTo(3);
    }

    @Test
    void userValidationFunctionWorks() {
        // Test valid user
        UserData validUser = new UserData("John Doe", "john@example.com", 25);
        ValidationResult result = userValidator.apply(validUser);

        assertThat(result.isValid()).isTrue();
        assertThat(result.getMessage()).isEqualTo("Valid");
    }

    @Test
    void userValidationRejectsInvalidUser() {
        // Test invalid user (under 18)
        UserData invalidUser = new UserData("Jane", "jane@example.com", 17);
        ValidationResult result = userValidator.apply(invalidUser);

        assertThat(result.isValid()).isFalse();
    }

    @Test
    void inventoryCheckFunctionWorks() {
        // Test available inventory
        InventoryCheckRequest request = new InventoryCheckRequest("product-123", 50);
        InventoryResult result = inventoryChecker.apply(request);

        assertThat(result.isAvailable()).isTrue();
        assertThat(result.getAvailableQuantity()).isEqualTo(50);
    }

    @Test
    void inventoryCheckRejectsExcessiveQuantity() {
        // Test excessive quantity
        InventoryCheckRequest request = new InventoryCheckRequest("product-123", 150);
        InventoryResult result = inventoryChecker.apply(request);

        assertThat(result.isAvailable()).isFalse();
        assertThat(result.getMessage()).contains("Insufficient inventory");
    }

    @Test
    void paymentProcessorWorks() {
        // Test successful payment
        PaymentRequest request = new PaymentRequest(
                "john-doe",
                new java.math.BigDecimal("100.00"),
                "USD",
                "CARD",
                "order-123"
        );
        PaymentResult result = paymentProcessor.apply(request);

        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getTransactionId()).isNotNull();
    }

    @Test
    void orderProcessorComposesMultipleFunctions() {
        // Test successful order creation
        UserData validUser = new UserData("John Doe", "john@example.com", 25);
        CreateOrderRequest request = new CreateOrderRequest(validUser, "product-123", 50);

        OrderResult result = orderProcessor.createOrder(request);

        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getOrderId()).isNotNull();
        assertThat(result.getOrderId()).startsWith("order-");
    }

    @Test
    void orderProcessorRejectsInvalidUser() {
        // Test order rejection due to invalid user
        UserData invalidUser = new UserData("Jane", "jane@example.com", 17);
        CreateOrderRequest request = new CreateOrderRequest(invalidUser, "product-123", 50);

        OrderResult result = orderProcessor.createOrder(request);

        assertThat(result.isSuccess()).isFalse();
        assertThat(result.getMessage()).contains("User validation failed");
    }

    @Test
    void orderProcessorRejectsInsufficientInventory() {
        // Test order rejection due to insufficient inventory
        UserData validUser = new UserData("John Doe", "john@example.com", 25);
        CreateOrderRequest request = new CreateOrderRequest(validUser, "product-123", 150);

        OrderResult result = orderProcessor.createOrder(request);

        assertThat(result.isSuccess()).isFalse();
        assertThat(result.getMessage()).contains("Inventory check failed");
    }
}
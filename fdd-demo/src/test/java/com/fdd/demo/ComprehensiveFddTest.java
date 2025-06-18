package com.fdd.demo;

import com.fdd.demo.domain.*;
import com.fdd.demo.functions.OrderProcessor;
import com.fdd.core.registry.FunctionRegistry;
import com.fdd.core.monitoring.MetricsCollector;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import java.math.BigDecimal;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Comprehensive integration test for the complete FDD Demo Application
 */
@SpringBootTest(classes = FddDemoApplication.class)
@TestPropertySource(properties = {
        "fdd.function.enabled=true",
        "fdd.function.discovery.enabled=true",
        "logging.level.com.fdd=DEBUG"
})
class ComprehensiveFddTest {

    @Autowired
    private FunctionRegistry functionRegistry;

    @Autowired
    private MetricsCollector metricsCollector;

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
    void contextLoadsWithAllComponents() {
        assertThat(functionRegistry).isNotNull();
        assertThat(metricsCollector).isNotNull();
        assertThat(userValidator).isNotNull();
        assertThat(inventoryChecker).isNotNull();
        assertThat(paymentProcessor).isNotNull();
        assertThat(orderProcessor).isNotNull();
    }

    @Test
    void allFunctionsAreRegistered() {
        assertThat(functionRegistry.isRegistered("userValidator")).isTrue();
        assertThat(functionRegistry.isRegistered("inventoryChecker")).isTrue();
        assertThat(functionRegistry.isRegistered("paymentProcessor")).isTrue();
        assertThat(functionRegistry.size()).isGreaterThanOrEqualTo(3);
    }

    // User Validation Tests
    @Test
    void userValidation_ValidUser_Success() {
        UserData validUser = new UserData("John Doe", "john@example.com", 25);
        ValidationResult result = userValidator.apply(validUser);

        assertThat(result.isValid()).isTrue();
        assertThat(result.getMessage()).isEqualTo("Valid");
    }

    @Test
    void userValidation_InvalidAge_Failure() {
        UserData invalidUser = new UserData("Jane", "jane@example.com", 17);
        ValidationResult result = userValidator.apply(invalidUser);

        assertThat(result.isValid()).isFalse();
    }

    @Test
    void userValidation_InvalidEmail_Failure() {
        UserData invalidUser = new UserData("John", "invalid-email", 25);
        ValidationResult result = userValidator.apply(invalidUser);

        assertThat(result.isValid()).isFalse();
    }

    @Test
    void userValidation_NullUser_Failure() {
        ValidationResult result = userValidator.apply(null);

        assertThat(result.isValid()).isFalse();
        assertThat(result.getMessage()).contains("null");
    }

    // Inventory Tests
    @Test
    void inventoryCheck_AvailableQuantity_Success() {
        InventoryCheckRequest request = new InventoryCheckRequest("product-123", 50);
        InventoryResult result = inventoryChecker.apply(request);

        assertThat(result.isAvailable()).isTrue();
        assertThat(result.getAvailableQuantity()).isEqualTo(50);
    }

    @Test
    void inventoryCheck_ExcessiveQuantity_Failure() {
        InventoryCheckRequest request = new InventoryCheckRequest("product-123", 150);
        InventoryResult result = inventoryChecker.apply(request);

        assertThat(result.isAvailable()).isFalse();
        assertThat(result.getMessage()).contains("Insufficient inventory");
    }

    @Test
    void inventoryCheck_MissingProductId_Failure() {
        InventoryCheckRequest request = new InventoryCheckRequest(null, 50);
        InventoryResult result = inventoryChecker.apply(request);

        assertThat(result.isAvailable()).isFalse();
        assertThat(result.getMessage()).contains("Product ID is required");
    }

    // Payment Processing Tests
    @Test
    void paymentProcessing_ValidCardPayment_Success() {
        PaymentRequest request = new PaymentRequest(
                "john-doe",
                new BigDecimal("500.00"),
                "USD",
                "CARD",
                "order-123"
        );
        PaymentResult result = paymentProcessor.apply(request);

        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getTransactionId()).isNotNull();
        assertThat(result.getProcessedAmount()).isEqualTo(new BigDecimal("500.00"));
    }

    @Test
    void paymentProcessing_BankTransfer_Pending() {
        PaymentRequest request = new PaymentRequest(
                "john-doe",
                new BigDecimal("1000.00"),
                "USD",
                "BANK_TRANSFER",
                "order-124"
        );
        PaymentResult result = paymentProcessor.apply(request);

        assertThat(result.isSuccess()).isFalse(); // Pending, not completed
        assertThat(result.getStatus()).isEqualTo("PENDING");
        assertThat(result.getTransactionId()).isNotNull();
    }

    @Test
    void paymentProcessing_ExcessiveAmount_Failure() {
        PaymentRequest request = new PaymentRequest(
                "john-doe",
                new BigDecimal("15000.00"),
                "USD",
                "CARD",
                "order-125"
        );
        PaymentResult result = paymentProcessor.apply(request);

        assertThat(result.isSuccess()).isFalse();
        assertThat(result.getMessage()).contains("exceeds maximum limit");
    }

    @Test
    void paymentProcessing_UnsupportedMethod_Failure() {
        PaymentRequest request = new PaymentRequest(
                "john-doe",
                new BigDecimal("100.00"),
                "USD",
                "CRYPTO",
                "order-126"
        );
        PaymentResult result = paymentProcessor.apply(request);

        assertThat(result.isSuccess()).isFalse();
        assertThat(result.getMessage()).contains("Unsupported payment method");
    }

    // Order Processing (Function Composition) Tests
    @Test
    void orderProcessing_ValidOrder_Success() {
        UserData validUser = new UserData("John Doe", "john@example.com", 25);
        CreateOrderRequest request = new CreateOrderRequest(validUser, "product-123", 50, "CARD");

        OrderResult result = orderProcessor.createOrder(request);

        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getOrderId()).isNotNull();
        assertThat(result.getOrderId()).startsWith("order-");
        assertThat(result.getTransactionId()).isNotNull();
    }

    @Test
    void orderProcessing_InvalidUser_FailsAtUserValidation() {
        UserData invalidUser = new UserData("Jane", "invalid-email", 17);
        CreateOrderRequest request = new CreateOrderRequest(invalidUser, "product-123", 50, "CARD");

        OrderResult result = orderProcessor.createOrder(request);

        assertThat(result.isSuccess()).isFalse();
        assertThat(result.getMessage()).contains("User validation failed");
    }

    @Test
    void orderProcessing_InsufficientInventory_FailsAtInventoryCheck() {
        UserData validUser = new UserData("John Doe", "john@example.com", 25);
        CreateOrderRequest request = new CreateOrderRequest(validUser, "product-123", 150, "CARD");

        OrderResult result = orderProcessor.createOrder(request);

        assertThat(result.isSuccess()).isFalse();
        assertThat(result.getMessage()).contains("Inventory check failed");
    }

    @Test
    void orderProcessing_ExcessiveAmount_FailsAtPayment() {
        UserData validUser = new UserData("John Doe", "john@example.com", 25);
        CreateOrderRequest request = new CreateOrderRequest(validUser, "product-123", 2000, "CARD"); // 2000 * $10 = $20,000

        OrderResult result = orderProcessor.createOrder(request);

        assertThat(result.isSuccess()).isFalse();
        assertThat(result.getMessage()).contains("Payment processing failed");
    }

    // Function Registry Tests
    @Test
    void functionRegistry_RetrieveFunction_Success() {
        var function = functionRegistry.getFunction("userValidator");
        assertThat(function).isPresent();

        var metadata = functionRegistry.getMetadata("userValidator");
        assertThat(metadata).isPresent();
        assertThat(metadata.get().getComponent()).isEqualTo("userValidator");
    }

    @Test
    void functionRegistry_GetAllFunctionNames() {
        var functionNames = functionRegistry.getFunctionNames();
        assertThat(functionNames).contains("userValidator", "inventoryChecker", "paymentProcessor");
    }

    // Metrics Tests
    @Test
    void metricsCollector_TracksMetrics() {
        // Execute some functions to generate metrics
        userValidator.apply(new UserData("Test", "test@example.com", 25));
        inventoryChecker.apply(new InventoryCheckRequest("test-product", 10));

        // Check if metrics are being collected
        var allStats = metricsCollector.getAllStats();
        assertThat(allStats).isNotEmpty();
    }
}
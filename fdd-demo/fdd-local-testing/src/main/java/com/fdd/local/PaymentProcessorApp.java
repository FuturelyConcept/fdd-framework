// fdd-demo/fdd-local-testing/src/main/java/com/fdd/local/PaymentProcessorApp.java
package com.fdd.local;

import com.fdd.demo.domain.PaymentRequest;
import com.fdd.demo.domain.PaymentResult;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.*;
import java.util.function.Function;
import java.math.BigDecimal;

@SpringBootApplication(scanBasePackages = {
        "com.fdd.core",
        "com.fdd.starter",
        "com.fdd.demo.functions"
})
@RestController
public class PaymentProcessorApp {

    private static final BigDecimal MAX_AMOUNT = new BigDecimal("10000.00");

    // The actual FDD function
    private final Function<PaymentRequest, PaymentResult> paymentProcessor = request -> {
        if (request == null) {
            return PaymentResult.failed("Payment request is null");
        }

        // Validate required fields
        if (request.getUserId() == null || request.getUserId().trim().isEmpty()) {
            return PaymentResult.failed("User ID is required");
        }

        if (request.getAmount() == null || request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            return PaymentResult.failed("Invalid payment amount");
        }

        if (request.getAmount().compareTo(MAX_AMOUNT) > 0) {
            return PaymentResult.failed("Payment amount exceeds maximum limit of " + MAX_AMOUNT);
        }

        if (request.getPaymentMethod() == null || request.getPaymentMethod().trim().isEmpty()) {
            return PaymentResult.failed("Payment method is required");
        }

        // Simulate payment processing logic
        if ("CARD".equals(request.getPaymentMethod())) {
            String transactionId = "txn-" + System.currentTimeMillis();
            return PaymentResult.success(transactionId, request.getAmount());
        } else if ("BANK_TRANSFER".equals(request.getPaymentMethod())) {
            String transactionId = "txn-" + System.currentTimeMillis();
            return PaymentResult.pending(transactionId, request.getAmount());
        } else {
            return PaymentResult.failed("Unsupported payment method: " + request.getPaymentMethod());
        }
    };

    @PostMapping("/")
    public PaymentResult processPayment(@RequestBody PaymentRequest request) {
        System.out.println("💳 PaymentProcessor received: " + request.getUserId() + " amount:" + request.getAmount());
        return paymentProcessor.apply(request);
    }

    @GetMapping("/health")
    public String health() {
        return "PaymentProcessor OK";
    }

    public static void main(String[] args) {
        System.setProperty("server.port", "8083");
        System.out.println("🚀 Starting PaymentProcessor on port 8083");
        SpringApplication.run(PaymentProcessorApp.class, args);
    }
}
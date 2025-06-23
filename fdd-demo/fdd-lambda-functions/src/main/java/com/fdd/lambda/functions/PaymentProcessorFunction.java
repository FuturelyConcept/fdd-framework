package com.fdd.lambda.functions;

import com.fdd.demo.domain.PaymentRequest;
import com.fdd.demo.domain.PaymentResult;
import org.springframework.stereotype.Component;
import java.util.function.Function;
import java.math.BigDecimal;

/**
 * Pure FDD Payment Processing Function
 */
@Component("paymentProcessor")
public class PaymentProcessorFunction implements Function<PaymentRequest, PaymentResult> {

    private static final BigDecimal MAX_AMOUNT = new BigDecimal("10000.00");

    @Override
    public PaymentResult apply(PaymentRequest request) {
        System.out.println("🚀 FDD PaymentProcessorFunction starting...");

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
            // Simulate successful card payment
            String transactionId = "txn-" + System.currentTimeMillis();
            return PaymentResult.success(transactionId, request.getAmount());
        } else if ("BANK_TRANSFER".equals(request.getPaymentMethod())) {
            // Simulate pending bank transfer
            String transactionId = "txn-" + System.currentTimeMillis();
            return PaymentResult.pending(transactionId, request.getAmount());
        } else {
            return PaymentResult.failed("Unsupported payment method: " + request.getPaymentMethod());
        }
    }
}
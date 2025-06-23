package com.fdd.lambda.functions;

import com.fdd.demo.domain.InventoryCheckRequest;
import com.fdd.demo.domain.InventoryResult;
import org.springframework.stereotype.Component;
import java.util.function.Function;

/**
 * Pure FDD Function for inventory checking
 * ZERO AWS Lambda code - just pure business logic!
 */
@Component("inventoryChecker")
public class InventoryCheckFunction implements Function<InventoryCheckRequest, InventoryResult> {

    @Override
    public InventoryResult apply(InventoryCheckRequest request) {
        System.out.println("🚀 FDD InventoryCheckFunction starting...");

        if (request == null) {
            return InventoryResult.unavailable("Request is null");
        }

        if (request.getProductId() == null || request.getProductId().trim().isEmpty()) {
            return InventoryResult.unavailable("Product ID is required");
        }

        // Simple business logic - in real world this would check actual inventory
        boolean available = request.getQuantity() <= 100; // Max 100 units available

        if (available) {
            return InventoryResult.available(request.getQuantity());
        } else {
            return InventoryResult.unavailable("Insufficient inventory. Max 100 units available.");
        }
    }
}
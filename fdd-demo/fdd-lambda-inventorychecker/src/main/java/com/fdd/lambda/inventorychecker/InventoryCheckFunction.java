// fdd-lambda-inventorychecker/src/main/java/com/fdd/lambda/inventorychecker/InventoryCheckFunction.java
package com.fdd.lambda.inventorychecker;

import com.fdd.demo.domain.InventoryCheckRequest;
import com.fdd.demo.domain.InventoryResult;
import org.springframework.stereotype.Component;
import java.util.function.Function;

/**
 * 📦 Pure FDD InventoryCheck Function
 * This is the ONLY function in this Lambda!
 */
@Component("inventoryChecker")
public class InventoryCheckFunction implements Function<InventoryCheckRequest, InventoryResult> {

    @Override
    public InventoryResult apply(InventoryCheckRequest request) {
        System.out.println("🚀 DEDICATED InventoryChecker Lambda executing!");
        System.out.println("📦 Checking inventory for: " + (request != null ? request.getProductId() : "null"));

        if (request == null) {
            return InventoryResult.unavailable("Request is null");
        }

        if (request.getProductId() == null || request.getProductId().trim().isEmpty()) {
            return InventoryResult.unavailable("Product ID is required");
        }

        // Simple business logic - max 100 units available
        boolean available = request.getQuantity() <= 100;

        if (available) {
            System.out.println("✅ Inventory available for: " + request.getProductId() + " qty:" + request.getQuantity());
            return InventoryResult.available(request.getQuantity());
        } else {
            System.out.println("❌ Insufficient inventory for: " + request.getProductId() + " qty:" + request.getQuantity());
            return InventoryResult.unavailable("Insufficient inventory. Max 100 units available.");
        }
    }
}
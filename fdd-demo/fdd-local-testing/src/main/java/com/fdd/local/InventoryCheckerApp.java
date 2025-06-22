// fdd-demo/fdd-local-testing/src/main/java/com/fdd/local/InventoryCheckerApp.java
package com.fdd.local;

import com.fdd.demo.domain.InventoryCheckRequest;
import com.fdd.demo.domain.InventoryResult;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.*;
import java.util.function.Function;

@SpringBootApplication(scanBasePackages = {
        "com.fdd.core",
        "com.fdd.starter",
        "com.fdd.demo.functions"
})
@RestController
public class InventoryCheckerApp {

    // The actual FDD function
    private final Function<InventoryCheckRequest, InventoryResult> inventoryChecker = request -> {
        if (request == null) {
            return InventoryResult.unavailable("Request is null");
        }
        if (request.getProductId() == null || request.getProductId().trim().isEmpty()) {
            return InventoryResult.unavailable("Product ID is required");
        }

        // Simple business logic - max 100 units available
        boolean available = request.getQuantity() <= 100;

        if (available) {
            return InventoryResult.available(request.getQuantity());
        } else {
            return InventoryResult.unavailable("Insufficient inventory. Max 100 units available.");
        }
    };

    @PostMapping("/")
    public InventoryResult checkInventory(@RequestBody InventoryCheckRequest request) {
        System.out.println("📦 InventoryChecker received: " + request.getProductId() + " qty:" + request.getQuantity());
        return inventoryChecker.apply(request);
    }

    @GetMapping("/health")
    public String health() {
        return "InventoryChecker OK";
    }

    public static void main(String[] args) {
        System.setProperty("server.port", "8082");
        System.out.println("🚀 Starting InventoryChecker on port 8082");
        SpringApplication.run(InventoryCheckerApp.class, args);
    }
}
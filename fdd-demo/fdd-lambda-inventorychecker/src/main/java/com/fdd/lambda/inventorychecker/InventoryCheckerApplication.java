package com.fdd.lambda.inventorychecker;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {
        "com.fdd.core",
        "com.fdd.starter",
        "com.fdd.aws.lambda",
        "com.fdd.lambda.inventorychecker"  // 🎯 ONLY this package!
})
public class InventoryCheckerApplication {
    public static void main(String[] args) {
        System.out.println("🚀 FDD InventoryChecker Lambda starting...");
        SpringApplication.run(InventoryCheckerApplication.class, args);
    }
}

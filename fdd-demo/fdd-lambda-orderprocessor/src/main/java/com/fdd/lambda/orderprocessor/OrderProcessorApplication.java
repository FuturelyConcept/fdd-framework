package com.fdd.lambda.orderprocessor;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 🛒 OrderProcessor Lambda Application
 *
 * CRITICAL: This Lambda ONLY contains OrderProcessorFunction!
 * When it tries to @Autowire the other functions, FDD will create HTTP proxies!
 */
@SpringBootApplication(scanBasePackages = {
        "com.fdd.core",
        "com.fdd.starter",
        "com.fdd.aws.lambda",
        "com.fdd.lambda.orderprocessor"  // 🎯 ONLY this package - no other functions!
})
public class OrderProcessorApplication {
    public static void main(String[] args) {
        System.out.println("🚀 FDD OrderProcessor Lambda starting...");
        SpringApplication.run(OrderProcessorApplication.class, args);
    }
}
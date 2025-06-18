package com.fdd.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Demo application showcasing Function Driven Development
 *
 * This enables component scanning for:
 * - com.fdd.demo (demo application code)
 * - com.fdd.core (FDD framework core)
 */
@SpringBootApplication(scanBasePackages = {"com.fdd.demo", "com.fdd.core"})
public class FddDemoApplication {
    public static void main(String[] args) {
        SpringApplication.run(FddDemoApplication.class, args);
    }
}
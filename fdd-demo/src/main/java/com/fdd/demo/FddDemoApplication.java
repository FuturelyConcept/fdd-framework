package com.fdd.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;

/**
 * FDD Demo Application
 * Excludes Spring Boot's default security auto-configuration to allow FDD framework
 * to manage security through its own conditional configuration
 */
@SpringBootApplication
public class FddDemoApplication {
    public static void main(String[] args) {
        SpringApplication.run(FddDemoApplication.class, args);
    }
}
package com.fdd.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

/**
 * FDD Demo Application with explicit component scan
 * This should force Spring to find FDD components
 */
@SpringBootApplication
@ComponentScan(basePackages = {
        "com.fdd.demo",     // Demo application
        "com.fdd.core"      // FDD Framework core
})
public class FddDemoApplication {
    public static void main(String[] args) {
        SpringApplication.run(FddDemoApplication.class, args);
    }
}
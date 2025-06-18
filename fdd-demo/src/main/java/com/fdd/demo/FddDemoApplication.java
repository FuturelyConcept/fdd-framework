package com.fdd.demo;

import com.fdd.core.config.FddAutoConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

/**
 * FDD Demo Application with manual configuration import
 */
@SpringBootApplication(scanBasePackages = {"com.fdd.demo", "com.fdd.core"})
@Import(FddAutoConfiguration.class)
public class FddDemoApplication {
    public static void main(String[] args) {
        SpringApplication.run(FddDemoApplication.class, args);
    }
}
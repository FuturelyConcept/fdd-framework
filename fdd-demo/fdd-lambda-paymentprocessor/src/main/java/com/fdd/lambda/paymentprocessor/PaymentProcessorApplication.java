package com.fdd.lambda.paymentprocessor;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {
        "com.fdd.core",
        "com.fdd.starter",
        "com.fdd.aws.lambda",
        "com.fdd.lambda.paymentprocessor"  // 🎯 ONLY this package!
})
public class PaymentProcessorApplication {
    public static void main(String[] args) {
        System.out.println("🚀 FDD PaymentProcessor Lambda starting...");
        SpringApplication.run(PaymentProcessorApplication.class, args);
    }
}

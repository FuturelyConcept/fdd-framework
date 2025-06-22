// fdd-aws-lambda-starter/src/main/java/com/fdd/aws/lambda/FddLambdaApplication.java
package com.fdd.aws.lambda;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * FDD Lambda Application
 * Minimal Spring Boot app for Lambda environment
 */
@SpringBootApplication(scanBasePackages = {
        "com.fdd.core",
        "com.fdd.starter",
        "com.fdd.demo.functions"
})
public class FddLambdaApplication {
    public static void main(String[] args) {
        SpringApplication.run(FddLambdaApplication.class, args);
    }
}
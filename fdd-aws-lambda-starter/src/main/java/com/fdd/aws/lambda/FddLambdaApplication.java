// fdd-aws-lambda-starter/src/main/java/com/fdd/aws/lambda/FddLambdaApplication.java
package com.fdd.aws.lambda;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * FDD Lambda Application - FIXED COMPONENT SCANNING
 * Now scans the correct packages for Lambda functions
 */
@SpringBootApplication(scanBasePackages = {
        "com.fdd.core",           // FDD core framework
        "com.fdd.starter",        // FDD starter auto-configuration
        "com.fdd.aws.lambda",     // AWS Lambda integration (CrossLambdaFunctionRegistry)
        "com.fdd.lambda.functions" // ⭐ CRITICAL: This is where your Lambda functions are!
})
public class FddLambdaApplication {
    public static void main(String[] args) {
        SpringApplication.run(FddLambdaApplication.class, args);
    }
}
// fdd-lambda-uservalidator/src/main/java/com/fdd/lambda/uservalidator/UserValidatorApplication.java
package com.fdd.lambda.uservalidator;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 🚀 Dedicated UserValidator Lambda Application
 * ONLY contains UserValidationFunction - nothing else!
 */
@SpringBootApplication(scanBasePackages = {
        "com.fdd.core",                    // FDD framework
        "com.fdd.starter",                 // FDD auto-configuration
        "com.fdd.aws.lambda",              // Lambda integration
        "com.fdd.lambda.uservalidator"     // 🎯 ONLY this package!
})
public class UserValidatorApplication {
    public static void main(String[] args) {
        System.out.println("🚀 FDD UserValidator Lambda starting...");
        SpringApplication.run(UserValidatorApplication.class, args);
    }
}
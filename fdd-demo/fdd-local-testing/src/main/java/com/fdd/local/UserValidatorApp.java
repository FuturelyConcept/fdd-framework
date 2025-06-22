// fdd-demo/fdd-local-testing/src/main/java/com/fdd/local/UserValidatorApp.java
package com.fdd.local;

import com.fdd.demo.domain.UserData;
import com.fdd.demo.domain.ValidationResult;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.*;
import java.util.function.Function;

@SpringBootApplication(scanBasePackages = {
        "com.fdd.core",
        "com.fdd.starter",
        "com.fdd.demo.functions"
})
@RestController
public class UserValidatorApp {

    // The actual FDD function
    private final Function<UserData, ValidationResult> userValidator = userData -> {
        if (userData == null) {
            return ValidationResult.invalid("User data is null");
        }
        if (!userData.isValid()) {
            return ValidationResult.invalid("User data validation failed");
        }
        return ValidationResult.valid();
    };

    @PostMapping("/")
    public ValidationResult validateUser(@RequestBody UserData userData) {
        System.out.println("👤 UserValidator received: " + userData.getName());
        return userValidator.apply(userData);
    }

    @GetMapping("/health")
    public String health() {
        return "UserValidator OK";
    }

    public static void main(String[] args) {
        System.setProperty("server.port", "8081");
        System.out.println("🚀 Starting UserValidator on port 8081");
        SpringApplication.run(UserValidatorApp.class, args);
    }
}
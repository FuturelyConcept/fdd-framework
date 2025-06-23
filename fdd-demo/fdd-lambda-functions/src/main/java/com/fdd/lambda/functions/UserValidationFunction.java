package com.fdd.lambda.functions;

import com.fdd.demo.domain.UserData;
import com.fdd.demo.domain.ValidationResult;
import org.springframework.stereotype.Component;
import java.util.function.Function;

/**
 * Pure FDD Function - NO AWS Lambda knowledge required!
 * This is EXACTLY the same as the local version
 */
@Component("userValidator")
public class UserValidationFunction implements Function<UserData, ValidationResult> {

    @Override
    public ValidationResult apply(UserData userData) {
        System.out.println("🚀 FDD UserValidationFunction starting...");
        if (userData == null) {
            return ValidationResult.invalid("User data is null");
        }

        if (!userData.isValid()) {
            return ValidationResult.invalid("User data validation failed");
        }

        return ValidationResult.valid();
    }
}
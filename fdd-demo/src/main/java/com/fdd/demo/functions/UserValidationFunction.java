package com.fdd.demo.functions;

import com.fdd.demo.domain.UserData;
import com.fdd.demo.domain.ValidationResult;
import org.springframework.stereotype.Component;
import java.util.function.Function;

/**
 * Pure java.util.Function for user validation - zero learning curve!
 */
@Component("userValidator")
public class UserValidationFunction implements Function<UserData, ValidationResult> {
    
    @Override
    public ValidationResult apply(UserData userData) {
        if (userData == null) {
            return ValidationResult.invalid("User data is null");
        }
        
        if (!userData.isValid()) {
            return ValidationResult.invalid("User data validation failed");
        }
        
        return ValidationResult.valid();
    }
}

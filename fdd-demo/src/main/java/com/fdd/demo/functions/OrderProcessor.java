package com.fdd.demo.functions;

import com.fdd.demo.domain.CreateOrderRequest;
import com.fdd.demo.domain.OrderResult;
import com.fdd.demo.domain.UserData;
import com.fdd.demo.domain.ValidationResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.function.Function;

/**
 * Demonstrates function composition with type-safe @Autowired dependency injection
 */
@Component
public class OrderProcessor {

    // Type-safe function injection - just like regular Spring beans!
    @Autowired
    @Qualifier("userValidator")
    private Function<UserData, ValidationResult> userValidator;

    /**
     * Process an order by composing multiple functions
     */
    public OrderResult createOrder(CreateOrderRequest request) {
        // Type-safe function calls with compile-time checking
        ValidationResult validation = userValidator.apply(request.getUserData());

        if (!validation.isValid()) {
            return OrderResult.failed("User validation failed: " + validation.getMessage());
        }

        // For now, just return success - we'll add more functions later
        return OrderResult.success("order-" + System.currentTimeMillis());
    }
}
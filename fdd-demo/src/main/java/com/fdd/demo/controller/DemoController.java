package com.fdd.demo.controller;

import com.fdd.demo.domain.CreateOrderRequest;
import com.fdd.demo.domain.OrderResult;
import com.fdd.demo.domain.UserData;
import com.fdd.demo.domain.ValidationResult;
import com.fdd.demo.functions.OrderProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.*;

import java.util.function.Function;

/**
 * Demo controller showing FDD in action
 */
@RestController
@RequestMapping("/demo")
public class DemoController {

    @Autowired
    private OrderProcessor orderProcessor;

    // Direct function injection for simple cases
    @Autowired
    @Qualifier("userValidator")
    private Function<UserData, ValidationResult> userValidator;

    /**
     * Test user validation function directly
     */
    @PostMapping("/validate-user")
    public ValidationResult validateUser(@RequestBody UserData userData) {
        return userValidator.apply(userData);
    }

    /**
     * Test order processing with function composition
     */
    @PostMapping("/create-order")
    public OrderResult createOrder(@RequestBody CreateOrderRequest request) {
        return orderProcessor.createOrder(request);
    }

    /**
     * Quick test endpoint
     */
    @GetMapping("/test")
    public String test() {
        return "FDD Demo is running! Try POST /demo/validate-user or /demo/create-order";
    }
}
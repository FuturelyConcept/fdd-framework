package com.fdd.demo;

import com.fdd.demo.domain.UserData;
import com.fdd.demo.domain.ValidationResult;
import com.fdd.demo.functions.UserValidationFunction;
import com.fdd.core.registry.FunctionRegistry;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Standalone test without Spring to verify core functionality
 */
class StandaloneFddTest {

    @Test
    void functionRegistryWorks() {
        FunctionRegistry registry = new FunctionRegistry();
        assertThat(registry).isNotNull();
        assertThat(registry.size()).isEqualTo(0);
    }

    @Test
    void userValidationFunctionWorks() {
        UserValidationFunction function = new UserValidationFunction();
        UserData validUser = new UserData("John Doe", "john@example.com", 25);

        ValidationResult result = function.apply(validUser);

        assertThat(result).isNotNull();
        assertThat(result.isValid()).isTrue();
        assertThat(result.getMessage()).isEqualTo("Valid");
    }

    @Test
    void userValidationRejectsInvalidUser() {
        UserValidationFunction function = new UserValidationFunction();
        UserData invalidUser = new UserData("Jane", "invalid-email", 17);

        ValidationResult result = function.apply(invalidUser);

        assertThat(result).isNotNull();
        assertThat(result.isValid()).isFalse();
    }

    @Test
    void functionRegistryCanRegisterFunctions() {
        FunctionRegistry registry = new FunctionRegistry();
        UserValidationFunction function = new UserValidationFunction();

        // Create minimal metadata
        com.fdd.core.registry.FunctionMetadata metadata =
                new com.fdd.core.registry.FunctionMetadata();
        metadata.setComponent("userValidator");
        metadata.setName("test.user.validator");

        registry.registerFunction("userValidator", function, metadata);

        assertThat(registry.size()).isEqualTo(1);
        assertThat(registry.isRegistered("userValidator")).isTrue();
    }
}
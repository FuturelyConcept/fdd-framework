package com.fdd.demo;

import com.fdd.demo.domain.UserData;
import com.fdd.demo.domain.ValidationResult;
import com.fdd.core.registry.FunctionRegistry;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration test for FDD Demo Application
 */
@SpringBootTest
@TestPropertySource(properties = {
        "fdd.function.enabled=true",
        "fdd.function.discovery.enabled=true"
})
class FddDemoApplicationTest {

    @Autowired
    private FunctionRegistry functionRegistry;

    @Autowired
    @Qualifier("userValidator")
    private Function<UserData, ValidationResult> userValidator;

    @Test
    void contextLoads() {
        assertThat(functionRegistry).isNotNull();
        assertThat(userValidator).isNotNull();
    }

    @Test
    void functionRegistryContainsUserValidator() {
        assertThat(functionRegistry.isRegistered("userValidator")).isTrue();
        assertThat(functionRegistry.size()).isGreaterThan(0);
    }

    @Test
    void userValidationFunctionWorks() {
        // Test valid user
        UserData validUser = new UserData("John Doe", "john@example.com", 25);
        ValidationResult result = userValidator.apply(validUser);

        assertThat(result.isValid()).isTrue();
        assertThat(result.getMessage()).isEqualTo("Valid");
    }

    @Test
    void userValidationRejectsInvalidUser() {
        // Test invalid user (under 18)
        UserData invalidUser = new UserData("Jane", "jane@example.com", 17);
        ValidationResult result = userValidator.apply(invalidUser);

        assertThat(result.isValid()).isFalse();
    }

    @Test
    void userValidationRejectsNullUser() {
        ValidationResult result = userValidator.apply(null);

        assertThat(result.isValid()).isFalse();
        assertThat(result.getMessage()).contains("null");
    }
}
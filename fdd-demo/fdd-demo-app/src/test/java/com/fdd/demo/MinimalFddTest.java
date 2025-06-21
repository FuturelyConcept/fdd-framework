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
 * Minimal test to verify basic FDD functionality
 */
@SpringBootTest(classes = FddDemoApplication.class)
@TestPropertySource(properties = {
        "fdd.function.enabled=true",
        "fdd.function.discovery.enabled=true",
        "logging.level.com.fdd=DEBUG",
        "logging.level.org.springframework=WARN"
})
class MinimalFddTest {

    @Autowired(required = false)
    private FunctionRegistry functionRegistry;

    @Autowired(required = false)
    @Qualifier("userValidator")
    private Function<UserData, ValidationResult> userValidator;

    @Test
    void springContextLoads() {
        // Just verify Spring context can load
        assertThat(true).isTrue();
    }

    @Test
    void functionRegistryExists() {
        assertThat(functionRegistry).isNotNull();
    }

    @Test
    void userValidatorExists() {
        assertThat(userValidator).isNotNull();
    }

    @Test
    void userValidationWorks() {
        if (userValidator != null) {
            UserData validUser = new UserData("John Doe", "john@example.com", 25);
            ValidationResult result = userValidator.apply(validUser);
            assertThat(result.isValid()).isTrue();
        }
    }
}
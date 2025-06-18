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
 * Working Spring test without discovery controller
 */
@SpringBootTest(classes = FddDemoApplication.class)
@TestPropertySource(properties = {
        "fdd.function.enabled=true",
        "logging.level.com.fdd=DEBUG"
})
class WorkingFddTest {

    @Autowired
    private FunctionRegistry functionRegistry;

    @Autowired
    @Qualifier("userValidator")
    private Function<UserData, ValidationResult> userValidator;

    @Test
    void springContextLoads() {
        assertThat(functionRegistry).isNotNull();
        assertThat(userValidator).isNotNull();
    }

    @Test
    void functionRegistryWorks() {
        assertThat(functionRegistry.size()).isGreaterThan(0);
        assertThat(functionRegistry.isRegistered("userValidator")).isTrue();
    }

    @Test
    void userValidationWorks() {
        UserData validUser = new UserData("John Doe", "john@example.com", 25);
        ValidationResult result = userValidator.apply(validUser);

        assertThat(result.isValid()).isTrue();
        assertThat(result.getMessage()).isEqualTo("Valid");
    }

    @Test
    void functionsAreAutoRegistered() {
        // Verify that functions are automatically discovered and registered
        var functionNames = functionRegistry.getFunctionNames();
        assertThat(functionNames).contains("userValidator");

        var metadata = functionRegistry.getMetadata("userValidator");
        assertThat(metadata).isPresent();
        assertThat(metadata.get().getComponent()).isEqualTo("userValidator");
    }
}
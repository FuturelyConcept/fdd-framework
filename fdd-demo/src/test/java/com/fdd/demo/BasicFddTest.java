package com.fdd.demo;

import com.fdd.demo.domain.UserData;
import com.fdd.demo.domain.ValidationResult;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Simplified test to ensure basic FDD functionality works
 */
@SpringBootTest(classes = FddDemoApplication.class)
@TestPropertySource(properties = {
        "fdd.function.enabled=true",
        "fdd.function.discovery.enabled=true",
        "fdd.function.monitoring.enabled=false", // Disable monitoring for initial test
        "logging.level.com.fdd=DEBUG"
})
class BasicFddTest {

    @Autowired
    @Qualifier("userValidator")
    private Function<UserData, ValidationResult> userValidator;

    @Test
    void contextLoads() {
        // Basic test to ensure Spring context loads
        assertThat(userValidator).isNotNull();
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
}
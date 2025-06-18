package com.fdd.core.registry;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for FunctionRegistry
 */
class FunctionRegistryTest {

    private FunctionRegistry registry;

    @BeforeEach
    void setUp() {
        registry = new FunctionRegistry();
    }

    @Test
    void shouldRegisterAndRetrieveFunction() {
        // Given
        Function<String, String> testFunction = String::toUpperCase;
        FunctionMetadata metadata = new FunctionMetadata("test.function", "testBean", String.class, String.class);

        // When
        registry.registerFunction("testBean", testFunction, metadata);

        // Then
        assertThat(registry.isRegistered("testBean")).isTrue();
        assertThat(registry.size()).isEqualTo(1);

        var retrievedFunction = registry.getFunction("testBean");
        assertThat(retrievedFunction).isPresent();

        var retrievedMetadata = registry.getMetadata("testBean");
        assertThat(retrievedMetadata).isPresent();
        assertThat(retrievedMetadata.get().getName()).isEqualTo("test.function");
    }

    @Test
    void shouldReturnEmptyForNonExistentFunction() {
        var function = registry.getFunction("nonExistent");
        var metadata = registry.getMetadata("nonExistent");

        assertThat(function).isEmpty();
        assertThat(metadata).isEmpty();
        assertThat(registry.isRegistered("nonExistent")).isFalse();
    }

    @Test
    void shouldClearAllFunctions() {
        // Given
        Function<String, String> testFunction = String::toUpperCase;
        FunctionMetadata metadata = new FunctionMetadata("test.function", "testBean", String.class, String.class);
        registry.registerFunction("testBean", testFunction, metadata);

        // When
        registry.clear();

        // Then
        assertThat(registry.size()).isEqualTo(0);
        assertThat(registry.isRegistered("testBean")).isFalse();
    }

    @Test
    void shouldReturnAllFunctionNames() {
        // Given
        Function<String, String> function1 = String::toUpperCase;
        Function<String, String> function2 = String::toLowerCase;

        FunctionMetadata metadata1 = new FunctionMetadata("func1", "bean1", String.class, String.class);
        FunctionMetadata metadata2 = new FunctionMetadata("func2", "bean2", String.class, String.class);

        registry.registerFunction("bean1", function1, metadata1);
        registry.registerFunction("bean2", function2, metadata2);

        // When
        var functionNames = registry.getFunctionNames();

        // Then
        assertThat(functionNames).containsExactlyInAnyOrder("bean1", "bean2");
    }
}
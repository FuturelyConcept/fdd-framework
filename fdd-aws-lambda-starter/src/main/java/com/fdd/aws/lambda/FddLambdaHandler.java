package com.fdd.aws.lambda;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fdd.core.registry.FunctionRegistry;
import com.fdd.core.registry.FunctionMetadata;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContext;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class FddLambdaHandler implements RequestHandler<Object, Object> {

    private static ApplicationContext applicationContext;
    private static final ObjectMapper objectMapper = new ObjectMapper();

    static {
        try {
            System.setProperty("spring.main.web-application-type", "none");
            applicationContext = SpringApplication.run(FddLambdaApplication.class);
            System.out.println("✅ FDD Lambda Handler initialized");
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("FDD initialization failed", e);
        }
    }

    @Override
    public Object handleRequest(Object input, Context context) {
        String functionName = System.getenv("FDD_FUNCTION_NAME");

        try {
            System.out.println("🚀 Processing function: " + functionName);
            System.out.println("📥 Input type: " + input.getClass().getSimpleName());

            if (functionName == null) {
                throw new RuntimeException("FDD_FUNCTION_NAME environment variable not set");
            }

            FunctionRegistry registry = applicationContext.getBean(FunctionRegistry.class);
            Function<Object, Object> function = registry.getFunction(functionName)
                    .orElseThrow(() -> new RuntimeException("Function not found: " + functionName));

            FunctionMetadata metadata = registry.getMetadata(functionName)
                    .orElseThrow(() -> new RuntimeException("Function metadata not found: " + functionName));

            // CRITICAL FIX: Ensure proper type conversion
            Object typedInput = ensureProperTypeConversion(input, metadata);

            System.out.println("✅ Converted input to: " +
                    (typedInput != null ? typedInput.getClass().getSimpleName() : "null"));

            // Execute function
            Object result = function.apply(typedInput);

            System.out.println("🎉 Function executed successfully");

            // Return HTTP response if needed
            if (isHttpRequest(input)) {
                return createHttpResponse(200, result);
            }
            return result;

        } catch (Exception e) {
            System.err.println("❌ FDD execution failed: " + e.getMessage());
            e.printStackTrace();

            Map<String, Object> error = new HashMap<>();
            error.put("functionName", functionName);
            error.put("error", "FDD_EXECUTION_FAILED");
            error.put("message", e.getMessage());
            error.put("type", e.getClass().getSimpleName());

            if (isHttpRequest(input)) {
                return createHttpResponse(500, error);
            }
            return error;
        }
    }

    private Object ensureProperTypeConversion(Object input, FunctionMetadata metadata) throws Exception {
        Class<?> expectedType = metadata.getInputType();
        System.out.println("🔍 Expected type: " + (expectedType != null ? expectedType.getSimpleName() : "Any"));

        if (expectedType == null) {
            return input;
        }

        // Handle HTTP request
        if (isHttpRequest(input)) {
            Map<String, Object> httpEvent = (Map<String, Object>) input;
            String jsonBody = (String) httpEvent.get("body");
            System.out.println("📝 HTTP body: " + jsonBody);

            if (jsonBody != null && !jsonBody.trim().isEmpty()) {
                try {
                    Object converted = objectMapper.readValue(jsonBody, expectedType);
                    System.out.println("✅ JSON → " + expectedType.getSimpleName() + " conversion successful");
                    return converted;
                } catch (Exception e) {
                    System.err.println("❌ JSON conversion failed: " + e.getMessage());
                    throw e;
                }
            }
        }

        // Handle direct invocation - force conversion even if input seems correct
        try {
            if (input.getClass().equals(expectedType)) {
                System.out.println("✅ Input already correct type");
                return input;
            }

            // FORCE conversion using Jackson - this handles LinkedHashMap → POJO
            System.out.println("🔄 Force converting " + input.getClass().getSimpleName() + " → " + expectedType.getSimpleName());
            Object converted = objectMapper.convertValue(input, expectedType);
            System.out.println("✅ Forced conversion successful");
            return converted;

        } catch (Exception e) {
            System.err.println("❌ Type conversion failed: " + e.getMessage());

            // Last resort: try JSON round-trip
            try {
                System.out.println("🔄 Trying JSON round-trip conversion...");
                String json = objectMapper.writeValueAsString(input);
                Object converted = objectMapper.readValue(json, expectedType);
                System.out.println("✅ JSON round-trip conversion successful");
                return converted;
            } catch (Exception e2) {
                System.err.println("❌ All conversion methods failed");
                throw new RuntimeException("Cannot convert input to " + expectedType.getSimpleName() + ": " + e.getMessage(), e);
            }
        }
    }

    private boolean isHttpRequest(Object input) {
        return input instanceof Map && ((Map<?, ?>) input).containsKey("body");
    }

    private Object createHttpResponse(int statusCode, Object body) {
        Map<String, Object> response = new HashMap<>();
        response.put("statusCode", statusCode);
        response.put("headers", Map.of(
                "Content-Type", "application/json",
                "Access-Control-Allow-Origin", "*"
        ));

        try {
            response.put("body", objectMapper.writeValueAsString(body));
        } catch (Exception e) {
            response.put("body", "{\"error\":\"Serialization failed\"}");
        }

        return response;
    }
}
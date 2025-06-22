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

/**
 * Universal FDD Lambda Handler
 * This single handler can serve ANY Function<T,R> component
 */
public class FddLambdaHandler implements RequestHandler<Object, Object> {

    private static ApplicationContext applicationContext;
    private static final ObjectMapper objectMapper = new ObjectMapper();

    // Initialize Spring context once (cold start)
    static {
        try {
            System.setProperty("spring.main.web-application-type", "none");
            applicationContext = SpringApplication.run(FddLambdaApplication.class);
            System.out.println("FDD Lambda Handler initialized successfully");
        } catch (Exception e) {
            System.err.println("Failed to initialize FDD Lambda Handler: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("FDD initialization failed", e);
        }
    }

    @Override
    public Object handleRequest(Object input, Context context) {
        try {
            context.getLogger().log("FDD Lambda Handler - Raw input: " + input);

            // Get the function name from environment variable
            String functionName = System.getenv("FDD_FUNCTION_NAME");
            if (functionName == null) {
                throw new RuntimeException("FDD_FUNCTION_NAME environment variable not set");
            }

            context.getLogger().log("Executing FDD function: " + functionName);

            // Get FDD registry and function
            FunctionRegistry registry = applicationContext.getBean(FunctionRegistry.class);
            Function<Object, Object> function = registry.getFunction(functionName)
                    .orElseThrow(() -> new RuntimeException("Function not found: " + functionName));

            // Convert input to proper type
            Object typedInput = convertInput(input, functionName, registry, context);

            // Execute the function
            Object result = function.apply(typedInput);

            context.getLogger().log("Function executed successfully: " + result);

            // Handle HTTP response if this is a Function URL
            if (isHttpRequest(input)) {
                return createHttpResponse(200, result);
            } else {
                return result;
            }

        } catch (Exception e) {
            context.getLogger().log("FDD Lambda execution failed: " + e.getMessage());
            e.printStackTrace();

            Map<String, Object> error = new HashMap<>();
            error.put("error", "FDD_EXECUTION_FAILED");
            error.put("message", e.getMessage());
            error.put("type", e.getClass().getSimpleName());

            if (isHttpRequest(input)) {
                return createHttpResponse(500, error);
            } else {
                return error;
            }
        }
    }

    private Object convertInput(Object input, String functionName, FunctionRegistry registry, Context context) throws Exception {
        Object actualInput = input;

        // Handle HTTP Function URL requests
        if (input instanceof Map && ((Map<?, ?>) input).containsKey("body")) {
            Map<String, Object> httpEvent = (Map<String, Object>) input;
            String body = (String) httpEvent.get("body");
            context.getLogger().log("Extracting body from HTTP request: " + body);
            actualInput = objectMapper.readTree(body);
        }

        // Get metadata to determine input type
        var metadataOpt = registry.getMetadata(functionName);
        if (metadataOpt.isPresent()) {
            FunctionMetadata metadata = metadataOpt.get();
            Class<?> inputType = metadata.getInputType();
            if (inputType != null) {
                return objectMapper.convertValue(actualInput, inputType);
            }
        }

        return actualInput;
    }

    private boolean isHttpRequest(Object input) {
        return input instanceof Map && ((Map<?, ?>) input).containsKey("body");
    }

    private Object createHttpResponse(int statusCode, Object body) {
        Map<String, Object> response = new HashMap<>();
        response.put("statusCode", statusCode);

        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");
        headers.put("Access-Control-Allow-Origin", "*");
        response.put("headers", headers);

        try {
            response.put("body", objectMapper.writeValueAsString(body));
        } catch (Exception e) {
            response.put("body", "{\"error\":\"Serialization failed\"}");
        }

        return response;
    }
}
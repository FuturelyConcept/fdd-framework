package com.fdd.aws.lambda;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fdd.core.registry.FunctionRegistry;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContext;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * CORRECTED FDD Lambda Handler - Compilation Error Fixed
 */
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

            // Extract actual input type from Function<T,R>
            Class<?> expectedInputType = extractInputTypeFromFunction(function);
            System.out.println("🔍 Expected input type: " + (expectedInputType != null ? expectedInputType.getSimpleName() : "Any"));

            // Enhanced type conversion
            Object typedInput = convertToExpectedType(input, expectedInputType, functionName);
            System.out.println("✅ Converted input to: " + (typedInput != null ? typedInput.getClass().getSimpleName() : "null"));

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

    /**
     * Extract input type from Function<T,R> signature using reflection
     */
    private Class<?> extractInputTypeFromFunction(Function<?, ?> function) {
        try {
            Class<?> functionClass = function.getClass();
            System.out.println("🔍 Analyzing function class: " + functionClass.getName());

            // Strategy 1: Direct interface check
            Type[] genericInterfaces = functionClass.getGenericInterfaces();
            for (Type genericInterface : genericInterfaces) {
                if (genericInterface instanceof ParameterizedType) {
                    ParameterizedType paramType = (ParameterizedType) genericInterface;
                    if (paramType.getRawType().equals(Function.class)) {
                        Type[] typeArgs = paramType.getActualTypeArguments();
                        if (typeArgs.length > 0 && typeArgs[0] instanceof Class) {
                            Class<?> inputType = (Class<?>) typeArgs[0];
                            System.out.println("✅ Found input type from direct interface: " + inputType.getSimpleName());
                            return inputType;
                        }
                    }
                }
            }

            // Strategy 2: Check superclass for some Spring proxy types
            Type genericSuperClass = functionClass.getGenericSuperclass();
            if (genericSuperClass instanceof ParameterizedType) {
                ParameterizedType paramType = (ParameterizedType) genericSuperClass;
                Type[] typeArgs = paramType.getActualTypeArguments();
                if (typeArgs.length > 0 && typeArgs[0] instanceof Class) {
                    Class<?> inputType = (Class<?>) typeArgs[0];
                    System.out.println("✅ Found input type from superclass: " + inputType.getSimpleName());
                    return inputType;
                }
            }

            // Strategy 3: Walk up the class hierarchy
            Class<?> currentClass = functionClass;
            while (currentClass != null && currentClass != Object.class) {
                for (Type iface : currentClass.getGenericInterfaces()) {
                    if (iface instanceof ParameterizedType) {
                        ParameterizedType paramType = (ParameterizedType) iface;
                        Type rawType = paramType.getRawType();
                        if (rawType instanceof Class && Function.class.isAssignableFrom((Class<?>) rawType)) {
                            Type[] typeArgs = paramType.getActualTypeArguments();
                            if (typeArgs.length > 0 && typeArgs[0] instanceof Class) {
                                Class<?> inputType = (Class<?>) typeArgs[0];
                                System.out.println("✅ Found input type from hierarchy: " + inputType.getSimpleName());
                                return inputType;
                            }
                        }
                    }
                }
                currentClass = currentClass.getSuperclass();
            }

            // Strategy 4: Check if it's a Spring CGLIB proxy
            if (functionClass.getName().contains("$$")) {
                Class<?> originalClass = functionClass.getSuperclass();
                System.out.println("🔍 Detected proxy, checking original class: " + originalClass.getName());
                return extractInputTypeFromProxy(originalClass);
            }

        } catch (Exception e) {
            System.err.println("⚠️ Could not extract input type: " + e.getMessage());
        }

        return null;
    }

    /**
     * Helper method for proxy class analysis
     */
    private Class<?> extractInputTypeFromProxy(Class<?> actualClass) {
        try {
            Type[] genericInterfaces = actualClass.getGenericInterfaces();
            for (Type genericInterface : genericInterfaces) {
                if (genericInterface instanceof ParameterizedType) {
                    ParameterizedType paramType = (ParameterizedType) genericInterface;
                    if (paramType.getRawType().equals(Function.class)) {
                        Type[] typeArgs = paramType.getActualTypeArguments();
                        if (typeArgs.length > 0 && typeArgs[0] instanceof Class) {
                            return (Class<?>) typeArgs[0];
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("⚠️ Proxy analysis failed: " + e.getMessage());
        }
        return null;
    }

    /**
     * Enhanced type conversion with multiple strategies
     */
    private Object convertToExpectedType(Object input, Class<?> expectedType, String functionName) throws Exception {
        if (input == null) {
            return null;
        }

        // Handle HTTP request body extraction
        Object actualInput = input;
        if (isHttpRequest(input)) {
            Map<String, Object> httpEvent = (Map<String, Object>) input;
            String body = (String) httpEvent.get("body");
            System.out.println("📝 HTTP body: " + body);

            if (body != null && !body.trim().isEmpty()) {
                try {
                    actualInput = objectMapper.readValue(body, Object.class);
                    System.out.println("✅ Parsed HTTP body to: " + actualInput.getClass().getSimpleName());
                } catch (Exception e) {
                    System.err.println("❌ Failed to parse HTTP body: " + e.getMessage());
                    throw new RuntimeException("Invalid JSON in request body", e);
                }
            } else {
                actualInput = new HashMap<>(); // Empty request
            }
        }

        // If no expected type, return parsed input
        if (expectedType == null) {
            System.out.println("ℹ️ No expected type specified, using parsed input");
            return actualInput;
        }

        // If input is already the correct type, return as-is
        if (expectedType.isAssignableFrom(actualInput.getClass())) {
            System.out.println("✅ Input already correct type");
            return actualInput;
        }

        // Convert LinkedHashMap/Map to POJO
        try {
            System.out.println("🔄 Converting " + actualInput.getClass().getSimpleName() + " → " + expectedType.getSimpleName());
            Object converted = objectMapper.convertValue(actualInput, expectedType);
            System.out.println("✅ ObjectMapper conversion successful");
            return converted;
        } catch (Exception e) {
            System.err.println("❌ ObjectMapper conversion failed: " + e.getMessage());
        }

        // Fallback: JSON round-trip
        try {
            System.out.println("🔄 Trying JSON round-trip conversion...");
            String json = objectMapper.writeValueAsString(actualInput);
            System.out.println("📝 JSON: " + json);
            Object converted = objectMapper.readValue(json, expectedType);
            System.out.println("✅ JSON round-trip successful");
            return converted;
        } catch (Exception e) {
            System.err.println("❌ JSON round-trip failed: " + e.getMessage());
            throw new RuntimeException("Cannot convert input to " + expectedType.getSimpleName() + ": " + e.getMessage(), e);
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
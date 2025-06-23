package com.fdd.aws.lambda;

import com.fdd.core.registry.FunctionRegistry;
import com.fdd.core.registry.FunctionMetadata;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.function.Function;
import java.util.Optional;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Enhanced Cross-Lambda Function Registry
 * This creates HTTP proxies for @Autowired Function<T,R> dependencies
 */
@Component
@Primary
public class CrossLambdaFunctionRegistry extends FunctionRegistry {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final Map<String, String> lambdaUrls = new ConcurrentHashMap<>();

    public CrossLambdaFunctionRegistry() {
        super();
        loadLambdaUrls();
    }

    /**
     * Enhanced function registration with type auto-detection
     */
    @Override
    public void registerFunction(String componentName, Function<?, ?> function, FunctionMetadata metadata) {
        // Auto-detect types if missing
        if (metadata.getInputType() == null || metadata.getOutputType() == null) {
            System.out.println("🔍 Auto-detecting types for function: " + componentName);
            Class<?>[] types = extractFunctionTypes(function);

            if (types[0] != null && metadata.getInputType() == null) {
                metadata.setInputType(types[0]);
                System.out.println("✅ Auto-detected input type: " + types[0].getSimpleName());
            }
            if (types[1] != null && metadata.getOutputType() == null) {
                metadata.setOutputType(types[1]);
                System.out.println("✅ Auto-detected output type: " + types[1].getSimpleName());
            }
        }

        super.registerFunction(componentName, function, metadata);
    }

    /**
     * CRITICAL: Override getFunction to create HTTP proxies for remote functions
     * This is what makes @Autowired Function<T,R> work across Lambda boundaries
     */
    @Override
    @SuppressWarnings("unchecked")
    public <T, R> Optional<Function<T, R>> getFunction(String componentName) {
        // First try to get local function
        Optional<Function<T, R>> localFunction = super.getFunction(componentName);
        if (localFunction.isPresent()) {
            System.out.println("📍 Found local function: " + componentName);
            return localFunction;
        }

        // If not found locally, create a cross-Lambda proxy
        String lambdaUrl = lambdaUrls.get(componentName.toLowerCase());
        if (lambdaUrl != null) {
            System.out.println("🌐 Creating cross-Lambda proxy for: " + componentName + " -> " + lambdaUrl);
            Function<T, R> crossLambdaProxy = createCrossLambdaProxy(componentName, lambdaUrl);
            return Optional.of(crossLambdaProxy);
        }

        System.out.println("❌ Function not found locally or remotely: " + componentName);
        return Optional.empty();
    }

    /**
     * Extract Function<T,R> types via reflection
     */
    private Class<?>[] extractFunctionTypes(Function<?, ?> function) {
        Class<?>[] types = new Class<?>[2]; // [inputType, outputType]

        try {
            Class<?> functionClass = function.getClass();

            // Check direct interface implementation
            Type[] genericInterfaces = functionClass.getGenericInterfaces();
            for (Type genericInterface : genericInterfaces) {
                if (genericInterface instanceof ParameterizedType) {
                    ParameterizedType paramType = (ParameterizedType) genericInterface;
                    if (paramType.getRawType().equals(Function.class)) {
                        Type[] typeArgs = paramType.getActualTypeArguments();
                        if (typeArgs.length >= 2) {
                            if (typeArgs[0] instanceof Class) types[0] = (Class<?>) typeArgs[0];
                            if (typeArgs[1] instanceof Class) types[1] = (Class<?>) typeArgs[1];
                            return types;
                        }
                    }
                }
            }

            // Check superclass
            Type genericSuperClass = functionClass.getGenericSuperclass();
            if (genericSuperClass instanceof ParameterizedType) {
                ParameterizedType paramType = (ParameterizedType) genericSuperClass;
                Type[] typeArgs = paramType.getActualTypeArguments();
                if (typeArgs.length >= 2) {
                    if (typeArgs[0] instanceof Class) types[0] = (Class<?>) typeArgs[0];
                    if (typeArgs[1] instanceof Class) types[1] = (Class<?>) typeArgs[1];
                    return types;
                }
            }

        } catch (Exception e) {
            System.err.println("⚠️ Could not extract function types: " + e.getMessage());
        }

        return types;
    }

    /**
     * Load Lambda URLs from environment variables
     * Format: FDD_LAMBDA_URL_<FUNCTION_NAME>=<URL>
     */
    private void loadLambdaUrls() {
        System.getenv().forEach((key, value) -> {
            if (key.startsWith("FDD_LAMBDA_URL_")) {
                String functionName = key.substring("FDD_LAMBDA_URL_".length()).toLowerCase();
                lambdaUrls.put(functionName, value);
                System.out.println("🔗 Registered cross-Lambda URL: " + functionName + " -> " + value);
            }
        });
    }

    /**
     * CRITICAL: Create HTTP proxy that makes Lambda functions feel like local @Autowired beans
     * This is the core of the FDD cross-Lambda magic!
     */
    @SuppressWarnings("unchecked")
    private <T, R> Function<T, R> createCrossLambdaProxy(String functionName, String lambdaUrl) {
        return (T input) -> {
            try {
                System.out.println("🌐 FDD Cross-Lambda call: " + functionName + " -> " + lambdaUrl);
                System.out.println("📤 Sending: " + objectMapper.writeValueAsString(input));

                String requestBody = objectMapper.writeValueAsString(input);

                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(lambdaUrl))
                        .header("Content-Type", "application/json")
                        .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                        .timeout(java.time.Duration.ofSeconds(30))
                        .build();

                HttpResponse<String> response = httpClient.send(request,
                        HttpResponse.BodyHandlers.ofString());

                System.out.println("📥 Response status: " + response.statusCode());
                System.out.println("📥 Response body: " + response.body());

                if (response.statusCode() == 200) {
                    Object result = objectMapper.readValue(response.body(), Object.class);
                    System.out.println("✅ Cross-Lambda call successful");
                    return (R) result;
                } else {
                    throw new RuntimeException("Cross-Lambda call failed: " + response.statusCode() +
                            " - " + response.body());
                }

            } catch (Exception e) {
                System.err.println("❌ Cross-Lambda call error for " + functionName + ": " + e.getMessage());
                throw new RuntimeException("Cross-Lambda call error for " + functionName + ": " +
                        e.getMessage(), e);
            }
        };
    }

    /**
     * Register a Lambda URL for cross-function calls
     */
    public void registerLambdaUrl(String functionName, String url) {
        lambdaUrls.put(functionName.toLowerCase(), url);
        System.out.println("📝 Registered Lambda URL: " + functionName + " -> " + url);
    }
}
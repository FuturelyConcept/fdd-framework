package com.fdd.aws.lambda;

import com.fdd.core.registry.FunctionRegistry;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.function.Function;
import java.util.Optional;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Cross-Lambda Function Registry
 * This extends the base FunctionRegistry to handle cross-Lambda calls
 * When a function is not available locally, it creates a proxy that calls another Lambda
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
     * Override getFunction to handle cross-Lambda calls
     */
    @Override
    @SuppressWarnings("unchecked")
    public <T, R> Optional<Function<T, R>> getFunction(String componentName) {
        // First try to get local function
        Optional<Function<T, R>> localFunction = super.getFunction(componentName);
        if (localFunction.isPresent()) {
            return localFunction;
        }

        // If not found locally, create a cross-Lambda proxy
        String lambdaUrl = lambdaUrls.get(componentName);
        if (lambdaUrl != null) {
            Function<T, R> crossLambdaProxy = createCrossLambdaProxy(componentName, lambdaUrl);
            return Optional.of(crossLambdaProxy);
        }

        return Optional.empty();
    }

    /**
     * Create a proxy function that calls another Lambda
     * This is the CORE of cross-Lambda FDD functionality!
     */
    @SuppressWarnings("unchecked")
    private <T, R> Function<T, R> createCrossLambdaProxy(String functionName, String lambdaUrl) {
        return (T input) -> {
            try {
                System.out.println("🌐 FDD Cross-Lambda call: " + functionName + " -> " + lambdaUrl);

                String requestBody = objectMapper.writeValueAsString(input);

                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(lambdaUrl))
                        .header("Content-Type", "application/json")
                        .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                        .build();

                HttpResponse<String> response = httpClient.send(request,
                        HttpResponse.BodyHandlers.ofString());

                if (response.statusCode() == 200) {
                    // Try to determine the return type and deserialize
                    // For now, return as generic object - could be enhanced with type info
                    Object result = objectMapper.readValue(response.body(), Object.class);
                    return (R) result;
                } else {
                    throw new RuntimeException("Cross-Lambda call failed: " + response.statusCode() +
                            " - " + response.body());
                }

            } catch (Exception e) {
                throw new RuntimeException("Cross-Lambda call error for " + functionName + ": " +
                        e.getMessage(), e);
            }
        };
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
     * Register a Lambda URL for cross-function calls
     */
    public void registerLambdaUrl(String functionName, String url) {
        lambdaUrls.put(functionName, url);
        System.out.println("📝 Registered Lambda URL: " + functionName + " -> " + url);
    }
}

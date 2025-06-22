package com.fdd.orderservice;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.Map;

/**
 * Revolutionary FDD Order Service Handler
 * Demonstrates cross-Lambda function composition via HTTP calls
 * This is the FUTURE of serverless development!
 */
public class OrderServiceHandler implements RequestHandler<Object, Object> {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final HttpClient httpClient = HttpClient.newHttpClient();

    // Environment variables for service URLs
    private final String userServiceUrl = System.getenv("USER_SERVICE_URL");
    private final String inventoryServiceUrl = System.getenv("INVENTORY_SERVICE_URL");

    @Override
    public Object handleRequest(Object input, Context context) {
        context.getLogger().log("🚀 FDD Order Service - Processing request");
        context.getLogger().log("📡 User Service URL: " + userServiceUrl);
        context.getLogger().log("📦 Inventory Service URL: " + inventoryServiceUrl);

        try {
            // STEP 1: Parse input with same robust pattern as other services
            OrderRequestData orderData = parseOrderRequest(input, context);
            boolean isHttpRequest = isHttpRequest(input);

            if (orderData == null) {
                return createResponse("Invalid order request format", false, null, isHttpRequest);
            }

            context.getLogger().log("✅ Order request parsed successfully");

            // STEP 2: Validate user via User Service Lambda
            context.getLogger().log("🔍 Step 1: Validating user...");
            ValidationResult userValidation = callUserService(orderData, context);
            if (!userValidation.isValid()) {
                return createResponse("User validation failed: " + userValidation.getMessage(),
                        false, null, isHttpRequest);
            }
            context.getLogger().log("✅ User validation passed");

            // STEP 3: Check inventory via Inventory Service Lambda
            context.getLogger().log("📦 Step 2: Checking inventory...");
            InventoryResult inventoryResult = callInventoryService(orderData, context);
            if (!inventoryResult.isAvailable()) {
                return createResponse("Inventory check failed: " + inventoryResult.getMessage(),
                        false, null, isHttpRequest);
            }
            context.getLogger().log("✅ Inventory check passed");

            // STEP 4: Create order successfully
            String orderId = "fdd-cross-lambda-order-" + System.currentTimeMillis();
            context.getLogger().log("🎉 Order created successfully: " + orderId);

            return createResponse("Order created successfully", true, orderId, isHttpRequest);

        } catch (Exception e) {
            context.getLogger().log("❌ Order Service Error: " + e.getMessage());
            e.printStackTrace();
            return createResponse("Order processing error: " + e.getMessage(), false, null,
                    isHttpRequest(input));
        }
    }

    /**
     * Parse order request with robust input handling
     */
    private OrderRequestData parseOrderRequest(Object input, Context context) {
        try {
            String name = null, email = null, productId = null, paymentMethod = null;
            Integer age = null, quantity = null;

            if (input instanceof Map) {
                Map<String, Object> inputMap = (Map<String, Object>) input;

                if (inputMap.containsKey("body")) {
                    // HTTP Function URL request
                    String body = (String) inputMap.get("body");
                    JsonNode jsonNode = objectMapper.readTree(body);
                    return parseFromJsonNode(jsonNode);
                } else {
                    // Direct Lambda invocation
                    return parseFromMap(inputMap);
                }
            } else {
                // String JSON input
                JsonNode jsonNode = objectMapper.readTree(input.toString());
                return parseFromJsonNode(jsonNode);
            }
        } catch (Exception e) {
            context.getLogger().log("Failed to parse order request: " + e.getMessage());
            return null;
        }
    }

    private OrderRequestData parseFromJsonNode(JsonNode json) {
        OrderRequestData data = new OrderRequestData();

        // Extract user data
        if (json.has("userData")) {
            JsonNode userData = json.get("userData");
            data.name = userData.has("name") ? userData.get("name").asText() : null;
            data.email = userData.has("email") ? userData.get("email").asText() : null;
            data.age = userData.has("age") ? userData.get("age").asInt() : null;
        }

        // Extract order data
        data.productId = json.has("productId") ? json.get("productId").asText() : null;
        data.quantity = json.has("quantity") ? json.get("quantity").asInt() : null;
        data.paymentMethod = json.has("paymentMethod") ? json.get("paymentMethod").asText() : "CARD";

        return data;
    }

    private OrderRequestData parseFromMap(Map<String, Object> inputMap) {
        OrderRequestData data = new OrderRequestData();

        // Extract user data from nested map
        if (inputMap.containsKey("userData")) {
            Map<String, Object> userData = (Map<String, Object>) inputMap.get("userData");
            data.name = (String) userData.get("name");
            data.email = (String) userData.get("email");
            data.age = (Integer) userData.get("age");
        }

        // Extract order data
        data.productId = (String) inputMap.get("productId");
        data.quantity = (Integer) inputMap.get("quantity");
        data.paymentMethod = (String) inputMap.get("paymentMethod");
        if (data.paymentMethod == null) data.paymentMethod = "CARD";

        return data;
    }

    /**
     * Call User Service Lambda via HTTP - REVOLUTIONARY!
     */
    private ValidationResult callUserService(OrderRequestData orderData, Context context) {
        try {
            Map<String, Object> userRequest = new HashMap<>();
            userRequest.put("name", orderData.name);
            userRequest.put("email", orderData.email);
            userRequest.put("age", orderData.age);

            String requestBody = objectMapper.writeValueAsString(userRequest);
            context.getLogger().log("📤 Calling User Service: " + requestBody);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(userServiceUrl))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            HttpResponse<String> response = httpClient.send(request,
                    HttpResponse.BodyHandlers.ofString());

            context.getLogger().log("📥 User Service Response: " + response.statusCode() +
                    " - " + response.body());

            if (response.statusCode() == 200) {
                JsonNode jsonResponse = objectMapper.readTree(response.body());
                return new ValidationResult(
                        jsonResponse.get("valid").asBoolean(),
                        jsonResponse.get("message").asText()
                );
            } else {
                return new ValidationResult(false, "User service call failed: " + response.statusCode());
            }

        } catch (Exception e) {
            context.getLogger().log("❌ User service call error: " + e.getMessage());
            return new ValidationResult(false, "User service error: " + e.getMessage());
        }
    }

    /**
     * Call Inventory Service Lambda via HTTP - REVOLUTIONARY!
     */
    private InventoryResult callInventoryService(OrderRequestData orderData, Context context) {
        try {
            Map<String, Object> inventoryRequest = new HashMap<>();
            inventoryRequest.put("productId", orderData.productId);
            inventoryRequest.put("quantity", orderData.quantity);

            String requestBody = objectMapper.writeValueAsString(inventoryRequest);
            context.getLogger().log("📤 Calling Inventory Service: " + requestBody);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(inventoryServiceUrl))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            HttpResponse<String> response = httpClient.send(request,
                    HttpResponse.BodyHandlers.ofString());

            context.getLogger().log("📥 Inventory Service Response: " + response.statusCode() +
                    " - " + response.body());

            if (response.statusCode() == 200) {
                JsonNode jsonResponse = objectMapper.readTree(response.body());
                return new InventoryResult(
                        jsonResponse.get("available").asBoolean(),
                        jsonResponse.has("availableQuantity") ? jsonResponse.get("availableQuantity").asInt() : 0,
                        jsonResponse.get("message").asText()
                );
            } else {
                return new InventoryResult(false, 0, "Inventory service call failed: " + response.statusCode());
            }

        } catch (Exception e) {
            context.getLogger().log("❌ Inventory service call error: " + e.getMessage());
            return new InventoryResult(false, 0, "Inventory service error: " + e.getMessage());
        }
    }

    /**
     * Create consistent response format
     */
    private Object createResponse(String message, boolean success, String orderId, boolean isHttpRequest) {
        Map<String, Object> result = new HashMap<>();
        result.put("success", success);
        result.put("message", message);
        if (orderId != null) {
            result.put("orderId", orderId);
        }

        if (isHttpRequest) {
            return createHttpResponse(success ? 200 : 400, result);
        } else {
            return result;
        }
    }

    private boolean isHttpRequest(Object input) {
        return input instanceof Map && ((Map<String, Object>) input).containsKey("body");
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
            response.put("body", "{\"error\":\"Failed to serialize response\"}");
        }

        return response;
    }

    // Helper classes for type safety
    private static class OrderRequestData {
        String name, email, productId, paymentMethod;
        Integer age, quantity;
    }

    private static class ValidationResult {
        boolean valid;
        String message;
        ValidationResult(boolean valid, String message) {
            this.valid = valid;
            this.message = message;
        }
        boolean isValid() { return valid; }
        String getMessage() { return message; }
    }

    private static class InventoryResult {
        boolean available;
        int availableQuantity;
        String message;
        InventoryResult(boolean available, int availableQuantity, String message) {
            this.available = available;
            this.availableQuantity = availableQuantity;
            this.message = message;
        }
        boolean isAvailable() { return available; }
        String getMessage() { return message; }
    }
}
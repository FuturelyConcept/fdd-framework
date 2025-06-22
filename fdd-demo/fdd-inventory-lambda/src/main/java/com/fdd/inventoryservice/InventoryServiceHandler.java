package com.fdd.inventoryservice;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.HashMap;
import java.util.Map;

/**
 * Inventory service handler that works with both direct calls and Function URLs
 */
public class InventoryServiceHandler implements RequestHandler<Object, Object> {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public Object handleRequest(Object input, Context context) {
        context.getLogger().log("InventoryServiceHandler - Raw input: " + input);
        context.getLogger().log("InventoryServiceHandler - Input type: " + input.getClass().getName());

        try {
            String productId = null;
            Integer quantity = null;
            boolean isHttpRequest = false;

            // Handle different input types
            if (input instanceof Map) {
                Map<String, Object> inputMap = (Map<String, Object>) input;
                context.getLogger().log("InventoryServiceHandler - Input map keys: " + inputMap.keySet());

                // Check if this is an HTTP event (Function URL)
                if (inputMap.containsKey("body")) {
                    context.getLogger().log("InventoryServiceHandler - Processing HTTP event");
                    isHttpRequest = true;
                    String body = (String) inputMap.get("body");
                    context.getLogger().log("InventoryServiceHandler - HTTP body: " + body);

                    // Parse the JSON body
                    JsonNode jsonNode = objectMapper.readTree(body);
                    productId = jsonNode.has("productId") ? jsonNode.get("productId").asText() : null;
                    quantity = jsonNode.has("quantity") ? jsonNode.get("quantity").asInt() : null;
                } else {
                    // Direct JSON input
                    context.getLogger().log("InventoryServiceHandler - Processing direct JSON");
                    productId = (String) inputMap.get("productId");
                    quantity = (Integer) inputMap.get("quantity");
                }
            } else {
                // Handle string input
                context.getLogger().log("InventoryServiceHandler - Processing string input");
                JsonNode jsonNode = objectMapper.readTree(input.toString());
                productId = jsonNode.has("productId") ? jsonNode.get("productId").asText() : null;
                quantity = jsonNode.has("quantity") ? jsonNode.get("quantity").asInt() : null;
            }

            context.getLogger().log("InventoryServiceHandler - Extracted values: productId=" + productId + ", quantity=" + quantity);

            // Inventory validation logic (same as original FDD function)
            boolean available = false;
            String message = "";

            if (productId == null || productId.trim().isEmpty()) {
                message = "Product ID is required";
            } else if (quantity == null || quantity <= 0) {
                message = "Valid quantity is required";
            } else if (quantity <= 100) {
                // Simple business logic - max 100 units available
                available = true;
                message = "Inventory available";
            } else {
                message = "Insufficient inventory. Max 100 units available.";
            }

            Map<String, Object> result = new HashMap<>();
            result.put("available", available);
            result.put("availableQuantity", available ? quantity : 0);
            result.put("message", message);

            context.getLogger().log("InventoryServiceHandler - Result: available=" + available + ", message=" + message);

            // Return appropriate response format
            if (isHttpRequest) {
                return createHttpResponse(200, result);
            } else {
                return result;
            }

        } catch (Exception e) {
            context.getLogger().log("InventoryServiceHandler - Error: " + e.getMessage());
            e.printStackTrace();

            Map<String, Object> errorResult = new HashMap<>();
            errorResult.put("available", false);
            errorResult.put("availableQuantity", 0);
            errorResult.put("message", "Inventory service error: " + e.getMessage());

            // Try to determine if this was an HTTP request
            boolean isHttpRequest = input instanceof Map && ((Map<String, Object>) input).containsKey("body");

            if (isHttpRequest) {
                return createHttpResponse(500, errorResult);
            } else {
                return errorResult;
            }
        }
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
}
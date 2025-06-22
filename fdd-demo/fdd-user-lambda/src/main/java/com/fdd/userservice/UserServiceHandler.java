package com.fdd.userservice;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.HashMap;
import java.util.Map;

/**
 * Handler that works with both direct calls and Function URLs
 */
public class UserServiceHandler implements RequestHandler<Object, Object> {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public Object handleRequest(Object input, Context context) {
        context.getLogger().log("UserServiceHandler - Raw input: " + input);
        context.getLogger().log("UserServiceHandler - Input type: " + input.getClass().getName());

        try {
            String name = null;
            String email = null;
            Integer age = null;
            boolean isHttpRequest = false;

            // Handle different input types
            if (input instanceof Map) {
                Map<String, Object> inputMap = (Map<String, Object>) input;
                context.getLogger().log("UserServiceHandler - Input map keys: " + inputMap.keySet());

                // Check if this is an HTTP event (Function URL)
                if (inputMap.containsKey("body")) {
                    context.getLogger().log("UserServiceHandler - Processing HTTP event");
                    isHttpRequest = true;
                    String body = (String) inputMap.get("body");
                    context.getLogger().log("UserServiceHandler - HTTP body: " + body);

                    // Parse the JSON body
                    JsonNode jsonNode = objectMapper.readTree(body);
                    name = jsonNode.has("name") ? jsonNode.get("name").asText() : null;
                    email = jsonNode.has("email") ? jsonNode.get("email").asText() : null;
                    age = jsonNode.has("age") ? jsonNode.get("age").asInt() : null;
                } else {
                    // Direct JSON input
                    context.getLogger().log("UserServiceHandler - Processing direct JSON");
                    name = (String) inputMap.get("name");
                    email = (String) inputMap.get("email");
                    age = (Integer) inputMap.get("age");
                }
            } else {
                // Handle string input
                context.getLogger().log("UserServiceHandler - Processing string input");
                JsonNode jsonNode = objectMapper.readTree(input.toString());
                name = jsonNode.has("name") ? jsonNode.get("name").asText() : null;
                email = jsonNode.has("email") ? jsonNode.get("email").asText() : null;
                age = jsonNode.has("age") ? jsonNode.get("age").asInt() : null;
            }

            context.getLogger().log("UserServiceHandler - Extracted values: name=" + name + ", email=" + email + ", age=" + age);

            // Validate
            boolean valid = name != null && !name.trim().isEmpty() &&
                    email != null && email.contains("@") &&
                    age != null && age >= 18;

            Map<String, Object> result = new HashMap<>();
            result.put("valid", valid);
            result.put("message", valid ? "Valid" : "Invalid user data");

            context.getLogger().log("UserServiceHandler - Validation result: " + valid);

            // Return appropriate response format
            if (isHttpRequest) {
                return createHttpResponse(200, result);
            } else {
                return result;
            }

        } catch (Exception e) {
            context.getLogger().log("UserServiceHandler - Error: " + e.getMessage());
            e.printStackTrace();

            Map<String, Object> errorResult = new HashMap<>();
            errorResult.put("valid", false);
            errorResult.put("message", "Error: " + e.getMessage());

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
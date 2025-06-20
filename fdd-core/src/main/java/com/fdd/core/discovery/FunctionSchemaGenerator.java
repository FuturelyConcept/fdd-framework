package com.fdd.core.discovery;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.module.jsonSchema.JsonSchema;
import com.fasterxml.jackson.module.jsonSchema.JsonSchemaGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * Generates JSON schemas from Java classes for function input/output types
 */
@Component
public class FunctionSchemaGenerator {
    private static final Logger logger = LoggerFactory.getLogger(FunctionSchemaGenerator.class);

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final JsonSchemaGenerator schemaGenerator = new JsonSchemaGenerator(objectMapper);

    /**
     * Generate JSON schema for a Java class
     */
    public Map<String, Object> generateSchema(Class<?> clazz) {
        if (clazz == null) {
            return createSimpleSchema("any", "No type information available");
        }

        try {
            JsonSchema schema = schemaGenerator.generateSchema(clazz);
            JsonNode schemaNode = objectMapper.valueToTree(schema);

            // Convert to Map for easier manipulation
            Map<String, Object> schemaMap = objectMapper.convertValue(schemaNode, Map.class);

            // Add additional metadata
            enhanceSchema(schemaMap, clazz);

            return schemaMap;

        } catch (Exception e) {
            logger.warn("Failed to generate schema for class {}: {}", clazz.getName(), e.getMessage());
            return createSimpleSchema("object", "Schema generation failed: " + e.getMessage());
        }
    }

    /**
     * Generate example data for a class
     */
    public Object generateExample(Class<?> clazz) {
        if (clazz == null) {
            return null;
        }

        try {
            // Generate examples based on class type
            if (clazz.getSimpleName().equals("UserData")) {
                return createUserDataExample();
            } else if (clazz.getSimpleName().equals("ValidationResult")) {
                return createValidationResultExample();
            } else if (clazz.getSimpleName().equals("InventoryCheckRequest")) {
                return createInventoryCheckExample();
            } else if (clazz.getSimpleName().equals("InventoryResult")) {
                return createInventoryResultExample();
            } else if (clazz.getSimpleName().equals("PaymentRequest")) {
                return createPaymentRequestExample();
            } else if (clazz.getSimpleName().equals("PaymentResult")) {
                return createPaymentResultExample();
            }

            // Default example
            return Map.of(
                    "note", "Example data for " + clazz.getSimpleName(),
                    "type", clazz.getSimpleName()
            );

        } catch (Exception e) {
            logger.warn("Failed to generate example for class {}: {}", clazz.getName(), e.getMessage());
            return Map.of("error", "Example generation failed");
        }
    }

    /**
     * Enhance schema with additional metadata
     */
    private void enhanceSchema(Map<String, Object> schema, Class<?> clazz) {
        schema.put("javaType", clazz.getName());
        schema.put("simpleType", clazz.getSimpleName());

        // Add description based on class name
        String description = generateDescription(clazz.getSimpleName());
        if (description != null) {
            schema.put("description", description);
        }
    }

    /**
     * Generate human-readable description from class name
     */
    private String generateDescription(String className) {
        switch (className) {
            case "UserData":
                return "User information including name, email, and age for validation";
            case "ValidationResult":
                return "Result of user data validation with success flag and message";
            case "InventoryCheckRequest":
                return "Request to check product inventory availability";
            case "InventoryResult":
                return "Result of inventory check with availability status";
            case "PaymentRequest":
                return "Payment processing request with amount and method";
            case "PaymentResult":
                return "Result of payment processing with transaction details";
            case "CreateOrderRequest":
                return "Complete order creation request with user, product, and payment info";
            case "OrderResult":
                return "Result of order creation with success status and order ID";
            default:
                return null;
        }
    }

    /**
     * Create simple schema for basic types
     */
    private Map<String, Object> createSimpleSchema(String type, String description) {
        Map<String, Object> schema = new HashMap<>();
        schema.put("type", type);
        schema.put("description", description);
        return schema;
    }

    // Example generators for different types
    private Object createUserDataExample() {
        return Map.of(
                "name", "John Doe",
                "email", "john.doe@example.com",
                "age", 25
        );
    }

    private Object createValidationResultExample() {
        return Map.of(
                "valid", true,
                "message", "Valid user data"
        );
    }

    private Object createInventoryCheckExample() {
        return Map.of(
                "productId", "product-123",
                "quantity", 50
        );
    }

    private Object createInventoryResultExample() {
        return Map.of(
                "available", true,
                "availableQuantity", 50,
                "message", "Inventory available"
        );
    }

    private Object createPaymentRequestExample() {
        return Map.of(
                "userId", "john-doe",
                "amount", 100.00,
                "currency", "USD",
                "paymentMethod", "CARD",
                "orderId", "order-123"
        );
    }

    private Object createPaymentResultExample() {
        return Map.of(
                "success", true,
                "transactionId", "txn-1234567890",
                "processedAmount", 100.00,
                "message", "Payment processed successfully",
                "status", "COMPLETED"
        );
    }
}
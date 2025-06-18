# FDD Framework - Quick Start Guide

## 🚀 Build and Run

### Prerequisites
- Java 17 or later
- Maven 3.6+

### 1. Build the Framework
```bash
git clone https://github.com/FuturelyConcept/fdd-framework.git
cd fdd-framework
mvn clean install
```

### 2. Run the Demo Application
```bash
cd fdd-demo
mvn spring-boot:run
```

### 3. Test the Framework

#### Check Function Discovery
```bash
curl http://localhost:8080/functions
```

Expected response:
```json
{
  "count": 1,
  "functions": [
    {
      "name": "com.ecommerce.user.validate",
      "component": "userValidator",
      "inputType": "com.fdd.demo.domain.UserData",
      "outputType": "com.fdd.demo.domain.ValidationResult"
    }
  ]
}
```

#### Test User Validation Function
```bash
curl -X POST http://localhost:8080/demo/validate-user \
  -H "Content-Type: application/json" \
  -d '{
    "name": "John Doe",
    "email": "john@example.com", 
    "age": 25
  }'
```

Expected response:
```json
{
  "valid": true,
  "message": "Valid"
}
```

#### Test Function Composition
```bash
curl -X POST http://localhost:8080/demo/create-order \
  -H "Content-Type: application/json" \
  -d '{
    "userData": {
      "name": "John Doe",
      "email": "john@example.com",
      "age": 25
    },
    "productId": "product-123",
    "quantity": 2
  }'
```

Expected response:
```json
{
  "success": true,
  "orderId": "order-1703123456789",
  "message": "Order created successfully"
}
```

## 🔧 What's Happening Under the Hood

1. **Function Registration**: The `UserValidationFunction` is automatically discovered and registered
2. **Metadata Loading**: Configuration from `serverless.yml` is loaded and applied
3. **Type-Safe Injection**: Functions are available via `@Autowired @Qualifier("userValidator")`
4. **Function Composition**: `OrderProcessor` composes functions using familiar Spring patterns
5. **Discovery Endpoint**: All functions are exposed at `/functions` for introspection

## ✅ Verification Checklist

- [ ] Build completes without errors
- [ ] Demo application starts on port 8080
- [ ] `/functions` endpoint returns function metadata
- [ ] User validation works with valid/invalid data
- [ ] Order creation demonstrates function composition
- [ ] All tests pass with `mvn test`

## 🐛 Troubleshooting

### Build Issues
- Ensure Java 17+ is installed: `java -version`
- Check Maven version: `mvn -version`
- Clear Maven cache: `rm -rf ~/.m2/repository/com/fdd`

### Runtime Issues
- Check application logs for startup errors
- Verify `serverless.yml` is on classpath
- Ensure port 8080 is available

## 🎯 Next Steps

1. **Add More Functions**: Create additional `Function<T,R>` components
2. **Enhance Metadata**: Update `serverless.yml` with security configurations
3. **Test Composition**: Create more complex function orchestrations
4. **Cloud Deployment**: Deploy to AWS Lambda or Azure Functions

## 📚 Key Files to Explore

- `fdd-demo/src/main/java/com/fdd/demo/functions/UserValidationFunction.java` - Pure function example
- `fdd-demo/src/main/java/com/fdd/demo/functions/OrderProcessor.java` - Function composition
- `fdd-demo/src/main/resources/serverless.yml` - Function metadata configuration
- `fdd-core/src/main/java/com/fdd/core/config/FddAutoConfiguration.java` - Framework bootstrap
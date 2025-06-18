# FDD Framework - Function Driven Development

Revolutionary approach to serverless development using pure `java.util.Function` with configuration-driven metadata.

## 🚀 What is FDD?

FDD (Function Driven Development) bridges the gap between serverless deployment capabilities and developer experience by:

- ✅ **Pure `java.util.Function`** - Zero learning curve, full ecosystem compatibility
- ✅ **Configuration-driven metadata** - All security and deployment info in `serverless.yml`
- ✅ **Type-safe composition** - Compile-time checking with familiar `@Autowired`
- ✅ **Automatic discovery** - Functions self-register for `/functions` endpoint
- ✅ **AOP-based security** - Transparent context propagation and audit logging
- ✅ **Multi-cloud deployment** - AWS Lambda, Azure Functions, Google Cloud Functions

## 🎯 The Problem We Solve

Current serverless platforms excel at **deployment** but provide minimal **developer experience**:

| Traditional Serverless | FDD Approach |
|------------------------|--------------|
| ❌ Manual HTTP calls between functions | ✅ Type-safe `@Autowired Function<T,R>` |
| ❌ No compile-time validation | ✅ Full IDE support with autocomplete |
| ❌ Manual security context handling | ✅ Transparent security propagation |
| ❌ Hard to test function interactions | ✅ Standard Mockito testing |
| ❌ No function discovery | ✅ Automatic registry with contracts |

## ⚡ Quick Start

### 1. Build the Framework

```bash
git clone https://github.com/FuturelyConcept/fdd-framework.git
cd fdd-framework
mvn clean install
```

### 2. Run the Demo

```bash
cd fdd-demo
mvn spring-boot:run
```

### 3. Test Function Discovery

```bash
curl http://localhost:8080/functions
```

Expected response:
```json
{
  "count": 2,
  "functions": [
    {
      "name": "com.ecommerce.user.validate",
      "component": "userValidator",
      "inputType": "com.fdd.demo.domain.UserData",
      "outputType": "com.fdd.demo.domain.ValidationResult"
    },
    {
      "name": "com.ecommerce.inventory.check",
      "component": "inventoryChecker",
      "inputType": "com.fdd.demo.domain.InventoryCheckRequest",
      "outputType": "com.fdd.demo.domain.InventoryResult"
    }
  ]
}
```

### 4. Test Function Composition

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
    "quantity": 50
  }'
```

## 🧩 Core Concepts

### Pure Functions
```java
@Component("userValidator")
public class UserValidator implements Function<UserData, ValidationResult> {
    @Override
    public ValidationResult apply(UserData userData) {
        // Pure business logic - no framework clutter
        return userData.isValid() ? 
            ValidationResult.valid() : 
            ValidationResult.invalid("Invalid user data");
    }
}
```

### Type-Safe Composition
```java
@Component
public class OrderProcessor {
    
    // Familiar Spring dependency injection!
    @Autowired @Qualifier("userValidator")
    private Function<UserData, ValidationResult> userValidator;
    
    @Autowired @Qualifier("inventoryChecker")
    private Function<InventoryCheckRequest, InventoryResult> inventoryChecker;
    
    public OrderResult createOrder(CreateOrderRequest request) {
        // Type-safe function calls with compile-time checking
        ValidationResult validation = userValidator.apply(request.getUserData());
        if (!validation.isValid()) {
            return OrderResult.failed("User validation failed");
        }
        
        InventoryResult inventory = inventoryChecker.apply(
            new InventoryCheckRequest(request.getProductId(), request.getQuantity())
        );
        if (!inventory.isAvailable()) {
            return OrderResult.failed("Insufficient inventory");
        }
        
        return OrderResult.success("order-" + System.currentTimeMillis());
    }
}
```

### Configuration-Driven Metadata
```yaml
serverless:
  functions:
    userValidator:
      name: "com.ecommerce.user.validate"
      input: "com.fdd.demo.domain.UserData"
      output: "com.fdd.demo.domain.ValidationResult"
      security:
        group: "user-management"
        roles: ["USER_VALIDATOR"]
      deployment:
        cloud: "aws"
        memory: "256MB"
```

## 📦 Project Structure

```
fdd-framework/
├── fdd-core/                 # Core framework (registry, AOP, security)
├── fdd-starter/             # Spring Boot starter for zero-config
├── fdd-demo/                # Complete e-commerce demo
└── fdd-maven-plugin/        # Build-time contract generation (planned)
```

## 🔧 Key Features

### Function Discovery
- **Automatic Registration**: Functions self-register on startup
- **REST API**: `GET /functions` returns all available functions
- **Type Information**: Input/output types with JSON schemas
- **Metadata**: Security, deployment, and business context

### Type Safety
- **Compile-time Checking**: Wrong function signatures = compilation errors
- **IDE Support**: Full autocomplete, refactoring, and navigation
- **Contract Validation**: Input/output types validated automatically

### Function Composition
- **Familiar Patterns**: Use `@Autowired` like any Spring bean
- **Zero Overhead**: Direct method calls, no HTTP serialization
- **Error Propagation**: Standard Java exception handling

### Security Model
- **Function Groups**: Organize by business domain
- **Role-Based Access**: Fine-grained permission control
- **Context Propagation**: Security flows automatically between functions

## 🧪 Testing

### Unit Testing
```java
@Test
void userValidationWorks() {
    UserValidator validator = new UserValidator();
    UserData user = new UserData("John", "john@example.com", 25);
    
    ValidationResult result = validator.apply(user);
    
    assertThat(result.isValid()).isTrue();
}
```

### Integration Testing
```java
@SpringBootTest
class OrderProcessorTest {
    
    @Autowired
    private OrderProcessor orderProcessor;
    
    @Test
    void createOrderWithValidData() {
        CreateOrderRequest request = new CreateOrderRequest(
            new UserData("John", "john@example.com", 25),
            "product-123",
            50
        );
        
        OrderResult result = orderProcessor.createOrder(request);
        
        assertThat(result.isSuccess()).isTrue();
    }
}
```

## 🌐 Cloud Deployment

FDD builds on Spring Cloud Function for seamless deployment:

### AWS Lambda
```bash
mvn clean package
aws lambda update-function-code \
  --function-name user-validator \
  --zip-file fileb://target/demo.jar
```

### Azure Functions
```bash
mvn azure-functions:deploy
```

### Google Cloud Functions
```bash
gcloud functions deploy user-validator \
  --trigger-http \
  --runtime java17
```

## 📊 Benefits

- **Developer Productivity**: Focus on business logic, not plumbing
- **Type Safety**: Catch errors at compile time, not runtime
- **Testability**: Easy unit and integration testing
- **Discoverability**: Automatic function registry and documentation
- **Scalability**: Each function scales independently
- **Reusability**: Compose functions into complex workflows

## 🗺️ Roadmap

- [x] **Phase 1**: Core framework with registry and auto-configuration
- [x] **Phase 2**: Function discovery and basic composition
- [ ] **Phase 3**: Security context propagation and AOP
- [ ] **Phase 4**: Maven plugin for contract generation
- [ ] **Phase 5**: Enhanced cloud deployment tools
- [ ] **Phase 6**: Monitoring and observability integration

## 🤝 Contributing

We welcome contributions! Areas where help is needed:

- **Security**: JWT token validation and context propagation
- **AOP**: Function call interception for monitoring/logging
- **Maven Plugin**: Contract generation and validation
- **Documentation**: More examples and tutorials
- **Cloud Adapters**: Enhanced deployment tools

## 📚 Documentation

- [Getting Started Guide](docs/getting-started.md)
- [Function Composition Patterns](docs/function-composition.md)
- [Security Model](docs/security.md)
- [Cloud Deployment Guide](docs/cloud-deployment.md)

## 🔗 Links

- **Concept Overview**: [FDD: Function Driven Development](https://futurelyconcept.com/concepts/fdd.html)
- **GitHub Repository**: [fdd-framework](https://github.com/FuturelyConcept/fdd-framework)
- **Issues**: [Report bugs or request features](https://github.com/FuturelyConcept/fdd-framework/issues)

## 📄 License

Apache License 2.0
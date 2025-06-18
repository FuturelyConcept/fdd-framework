# FDD Framework - Function Driven Development

Revolutionary approach to serverless development using pure `java.util.Function` with configuration-driven metadata.

## ðŸš€ What is FDD?

FDD (Function Driven Development) bridges the gap between serverless deployment capabilities and developer experience by:

- âœ… **Pure `java.util.Function`** - Zero learning curve, full ecosystem compatibility
- âœ… **Configuration-driven metadata** - All security and deployment info in `serverless.yml`
- âœ… **Type-safe composition** - Compile-time checking with familiar `@Autowired`
- âœ… **Automatic discovery** - Functions self-register for `/functions` endpoint
- âœ… **AOP-based security** - Transparent context propagation and audit logging
- âœ… **Multi-cloud deployment** - AWS Lambda, Azure Functions, Google Cloud Functions

## ðŸŽ¯ The Problem We Solve

Current serverless platforms excel at **deployment** but provide minimal **developer experience**:

| Traditional Serverless | FDD Approach |
|------------------------|--------------|
| âŒ Manual HTTP calls between functions | âœ… Type-safe `@Autowired Function<T,R>` |
| âŒ No compile-time validation | âœ… Full IDE support with autocomplete |
| âŒ Manual security context handling | âœ… Transparent security propagation |
| âŒ Hard to test function interactions | âœ… Standard Mockito testing |
| âŒ No function discovery | âœ… Automatic registry with contracts |

## âš¡ Quick Start

### 1. Add FDD Starter

```xml
<dependency>
    <groupId>com.fdd</groupId>
    <artifactId>fdd-starter</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>
```

### 2. Create Functions (Pure java.util.Function!)

```java
@Component("userValidator")
public class UserValidator implements Function<UserData, ValidationResult> {
    @Override
    public ValidationResult apply(UserData userData) {
        // Pure business logic - no framework clutter
        return new ValidationResult(userData.isValid());
    }
}
```

### 3. Compose Functions with @Autowired

```java
@Component
public class OrderProcessor {
    
    // Familiar Spring dependency injection!
    @Autowired @Qualifier("userValidator")
    private Function<UserData, ValidationResult> userValidator;
    
    public OrderResult createOrder(CreateOrderRequest request) {
        // Type-safe function calls - zero framework overhead
        ValidationResult validation = userValidator.apply(request.getUserData());
        return validation.isValid() ? 
            OrderResult.success("order123") : 
            OrderResult.failed("Invalid user");
    }
}
```

### 4. Configure Security & Deployment (serverless.yml)

```yaml
serverless:
  functions:
    userValidator:
      name: "com.ecommerce.user.validate"
      input: "com.example.UserData"
      output: "com.example.ValidationResult"
      security:
        group: "user-management"
        roles: ["USER_VALIDATOR"]
      deployment:
        cloud: "aws"
        memory: "256MB"
```

## ðŸ“¦ Modules

- **fdd-core**: Core framework with AOP, security, and registry
- **fdd-maven-plugin**: Build-time contract generation and validation  
- **fdd-starter**: Spring Boot starter for zero-config setup
- **fdd-demo**: Complete e-commerce example with security

## ðŸ”’ Enterprise Security

- **Security Groups**: Organize functions by business domain
- **Role-Based Access**: Function-level permission control
- **Context Propagation**: Automatic security context flow between functions
- **Audit Logging**: Comprehensive function call auditing

## ðŸ“š Documentation

- [Getting Started](docs/getting-started.md)
- [Function Composition](docs/function-composition.md)
- [Security Model](docs/security.md)
- [Cloud Deployment](docs/cloud-deployment.md)

## ðŸ¤ Contributing

We welcome contributions! Please see our [Contributing Guide](CONTRIBUTING.md).

## ðŸ“„ License

Apache License 2.0

---

**Ready to revolutionize your serverless development?**  
The future of cloud-native development is function-driven. Join us in building it.

ðŸŒ **Blog Post**: [FDD: Function Driven Development](https://futurelyconcept.com/concepts/fdd.html)

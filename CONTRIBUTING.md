# Contributing to FDD Framework

Thank you for your interest in contributing to the Function Driven Development (FDD) Framework! This document provides guidelines and information for contributors.

## 🎯 Project Vision

FDD aims to bridge the gap between serverless deployment capabilities and developer experience by providing:

- Type-safe function composition using pure `java.util.Function`
- Configuration-driven metadata and security
- Zero learning curve for Spring developers
- Enterprise-grade monitoring and observability

## 🚀 Getting Started

### Prerequisites
- Java 17 or later
- Maven 3.6+
- Git
- IDE with Spring support (IntelliJ IDEA, Eclipse STS, VS Code)

### Development Setup

1. **Fork and Clone**
```bash
git clone https://github.com/YOUR_USERNAME/fdd-framework.git
cd fdd-framework
```

2. **Build the Project**
```bash
mvn clean install
```

3. **Run Tests**
```bash
mvn test
```

4. **Run Demo Application**
```bash
cd fdd-demo
mvn spring-boot:run
```

5. **Verify Setup**
- Visit `http://localhost:8080/functions` to see function registry
- Visit `http://localhost:8080/metrics` to see function metrics

## 📋 How to Contribute

### Areas Where We Need Help

#### 🔒 Security Enhancements
- JWT token validation and parsing
- Enhanced security context propagation
- OAuth2 integration
- Function-level authorization improvements

#### 📊 Monitoring & Observability
- Micrometer metrics integration
- Distributed tracing support
- Custom metrics collectors
- Performance monitoring dashboards

#### 🔧 Maven Plugin Development
- Contract generation from serverless.yml
- Function validation at build time
- OpenAPI specification generation
- Cloud deployment automation

#### ☁️ Cloud Adapters
- Enhanced AWS Lambda integration
- Azure Functions improvements
- Google Cloud Functions optimization
- Multi-cloud deployment tools

#### 📚 Documentation
- Tutorial videos and guides
- Best practices documentation
- Architecture decision records
- API documentation improvements

### Contributing Process

1. **Check Existing Issues**
    - Look at [GitHub Issues](https://github.com/FuturelyConcept/fdd-framework/issues)
    - Comment on issues you'd like to work on

2. **Create a Feature Branch**
```bash
git checkout -b feature/your-feature-name
```

3. **Make Your Changes**
    - Follow coding standards (see below)
    - Add tests for new functionality
    - Update documentation as needed

4. **Test Your Changes**
```bash
mvn clean test
mvn verify
```

5. **Submit a Pull Request**
    - Provide clear description of changes
    - Reference related issues
    - Include test results

## 🏗️ Project Structure

```
fdd-framework/
├── fdd-core/                    # Core framework (registry, AOP, security)
│   ├── src/main/java/com/fdd/core/
│   │   ├── config/              # Auto-configuration
│   │   ├── registry/            # Function registry
│   │   ├── security/            # Security context and interceptors
│   │   └── monitoring/          # Metrics and monitoring
│   └── src/test/java/           # Unit tests
├── fdd-starter/                 # Spring Boot starter
├── fdd-demo/                    # Demo application
│   ├── src/main/java/com/fdd/demo/
│   │   ├── functions/           # Example functions
│   │   ├── domain/              # Domain objects
│   │   └── controller/          # REST controllers
│   └── src/main/resources/
│       └── serverless.yml       # Function configuration
├── fdd-maven-plugin/            # Maven plugin (planned)
└── docs/                        # Documentation
```

## 🎨 Coding Standards

### Code Style
- Follow standard Java conventions
- Use meaningful variable and method names
- Write self-documenting code with clear comments
- Maximum line length: 120 characters

### Spring Conventions
- Use `@Component`, `@Service`, `@Repository` appropriately
- Prefer constructor injection over field injection for required dependencies
- Use `@Qualifier` for multiple beans of same type
- Follow Spring Boot auto-configuration patterns

### Function Development
- Functions must implement `java.util.Function<T, R>`
- Keep functions pure (no side effects when possible)
- Use descriptive component names (`@Component("userValidator")`)
- Define clear input/output types

### Testing
- Write unit tests for all public methods
- Use `@SpringBootTest` for integration tests
- Mock external dependencies using Mockito
- Achieve 80%+ code coverage for new features

### Example Function
```java
@Component("orderValidator")
public class OrderValidationFunction implements Function<OrderData, ValidationResult> {
    
    private final OrderValidationRules rules;
    
    public OrderValidationFunction(OrderValidationRules rules) {
        this.rules = rules;
    }
    
    @Override
    public ValidationResult apply(OrderData orderData) {
        if (orderData == null) {
            return ValidationResult.invalid("Order data cannot be null");
        }
        
        if (!rules.isValidAmount(orderData.getAmount())) {
            return ValidationResult.invalid("Invalid order amount");
        }
        
        return ValidationResult.valid();
    }
}
```

## 🧪 Testing Guidelines

### Unit Tests
```java
@ExtendWith(MockitoExtension.class)
class OrderValidationFunctionTest {
    
    @Mock
    private OrderValidationRules rules;
    
    @InjectMocks
    private OrderValidationFunction function;
    
    @Test
    void shouldValidateValidOrder() {
        // Given
        OrderData orderData = new OrderData(new BigDecimal("100.00"));
        when(rules.isValidAmount(any())).thenReturn(true);
        
        // When
        ValidationResult result = function.apply(orderData);
        
        // Then
        assertThat(result.isValid()).isTrue();
    }
}
```

### Integration Tests
```java
@SpringBootTest
@TestPropertySource(properties = {"fdd.function.enabled=true"})
class FunctionCompositionTest {
    
    @Autowired
    private OrderProcessor orderProcessor;
    
    @Test
    void shouldProcessValidOrder() {
        // Test complete function composition workflow
        CreateOrderRequest request = createValidOrderRequest();
        OrderResult result = orderProcessor.createOrder(request);
        assertThat(result.isSuccess()).isTrue();
    }
}
```

## 📝 Documentation

### Code Documentation
- Use Javadoc for public classes and methods
- Include examples in documentation
- Document configuration properties

### Architecture Documentation
- Document design decisions in `docs/architecture/`
- Include sequence diagrams for complex flows
- Maintain architectural decision records (ADRs)

## 🐛 Bug Reports

When reporting bugs, please include:

1. **Description**: Clear description of the issue
2. **Steps to Reproduce**: Minimal steps to reproduce the bug
3. **Expected Behavior**: What you expected to happen
4. **Actual Behavior**: What actually happened
5. **Environment**: Java version, OS, Maven version
6. **Logs**: Relevant log output (use DEBUG level for FDD packages)

### Bug Report Template
```markdown
## Bug Description
Brief description of the issue.

## Steps to Reproduce
1. Step one
2. Step two
3. Step three

## Expected Behavior
What should happen.

## Actual Behavior
What actually happens.

## Environment
- Java Version: 17
- Maven Version: 3.9.4
- OS: macOS 14.0
- FDD Version: 1.0.0-SNAPSHOT

## Logs
```
Include relevant logs here
```

## 💡 Feature Requests

For feature requests, please:

1. Check existing issues first
2. Describe the problem you're trying to solve
3. Propose a solution
4. Consider backward compatibility
5. Include example usage

## 🤝 Community

- **GitHub Discussions**: For questions and general discussion
- **Issues**: For bug reports and feature requests
- **Pull Requests**: For code contributions

## 📄 License

By contributing to FDD Framework, you agree that your contributions will be licensed under the Apache License 2.0.

## 🙏 Recognition

Contributors will be recognized in:
- README.md contributors section
- Release notes
- Documentation

Thank you for helping make FDD Framework better! 🚀
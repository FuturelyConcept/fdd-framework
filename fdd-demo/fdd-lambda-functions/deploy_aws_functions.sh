cd fdd-demo/fdd-lambda-functions
mvn clean package

aws lambda update-function-code --function-name "fdd-demo-orderProcessor" --zip-file fileb://target/fdd-lambda-functions-1.0.0-SNAPSHOT-shaded.jar
aws lambda update-function-code --function-name "fdd-demo-userValidator" --zip-file fileb://target/fdd-lambda-functions-1.0.0-SNAPSHOT-shaded.jar
aws lambda update-function-code --function-name "fdd-demo-inventoryChecker" --zip-file fileb://target/fdd-lambda-functions-1.0.0-SNAPSHOT-shaded.jar
aws lambda update-function-code --function-name "fdd-demo-paymentProcessor" --zip-file fileb://target/fdd-lambda-functions-1.0.0-SNAPSHOT-shaded.jar

# 3. Test:
curl -X POST "https://hheyaxr6g7rsjnmpsmgjamqei40ihfnd.lambda-url.us-east-1.on.aws/" \
  -H "Content-Type: application/json" \
  -d '{"userData":{"name":"John Doe","email":"john@example.com","age":25},"productId":"product-123","quantity":50,"paymentMethod":"CARD"}'

# Expected: {"success":true,"orderId":"...","transactionId":"..."}
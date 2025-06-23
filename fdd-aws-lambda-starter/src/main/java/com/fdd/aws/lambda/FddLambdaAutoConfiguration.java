// fdd-aws-lambda-starter/src/main/java/com/fdd/aws/lambda/FddLambdaAutoConfiguration.java
package com.fdd.aws.lambda;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;

/**
 * FDD Lambda Auto-Configuration
 * This was the missing class causing the FileNotFoundException!
 */
@AutoConfiguration
@ConditionalOnClass(name = "com.amazonaws.services.lambda.runtime.Context")
@ComponentScan(basePackages = {
        "com.fdd.core",
        "com.fdd.starter",
        "com.fdd.aws.lambda",
        "com.fdd.lambda.functions"  // Your function classes
})
public class FddLambdaAutoConfiguration {

    @Bean
    public CrossLambdaFunctionRegistry crossLambdaFunctionRegistry() {
        return new CrossLambdaFunctionRegistry();
    }
}
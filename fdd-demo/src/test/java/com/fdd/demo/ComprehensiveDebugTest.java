package com.fdd.demo;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.TestPropertySource;

import java.io.InputStream;
import java.util.Properties;

/**
 * Comprehensive debug test to diagnose auto-configuration issues
 */
@SpringBootTest(classes = FddDemoApplication.class)
@TestPropertySource(properties = {
        "logging.level.org.springframework.boot.autoconfigure=DEBUG",
        "logging.level.com.fdd=DEBUG",
        "debug=true"
})
class ComprehensiveDebugTest {

    @Autowired
    private ApplicationContext applicationContext;

    @Test
    void comprehensiveDebug() {
        System.out.println("\n" + "=".repeat(60));
        System.out.println("🔍 FDD FRAMEWORK COMPREHENSIVE DEBUG");
        System.out.println("=".repeat(60));

        // 1. Check if spring.factories exists and is readable
        System.out.println("\n1️⃣ CHECKING SPRING.FACTORIES");
        checkSpringFactories();

        // 2. Check classpath for FDD classes
        System.out.println("\n2️⃣ CHECKING FDD CLASSES ON CLASSPATH");
        checkFddClasses();

        // 3. Check Spring Boot auto-configuration
        System.out.println("\n3️⃣ CHECKING AUTO-CONFIGURATION");
        checkAutoConfiguration();

        // 4. List all beans
        System.out.println("\n4️⃣ LISTING ALL BEANS");
        listAllBeans();

        // 5. Check for FDD beans specifically
        System.out.println("\n5️⃣ CHECKING FDD BEANS");
        checkFddBeans();

        System.out.println("\n" + "=".repeat(60));
    }

    private void checkSpringFactories() {
        try {
            // Try to load spring.factories from classpath
            InputStream is = getClass().getClassLoader()
                    .getResourceAsStream("META-INF/spring.factories");

            if (is != null) {
                System.out.println("✅ spring.factories found on classpath");

                Properties props = new Properties();
                props.load(is);

                String autoConfigClasses = props.getProperty(
                        "org.springframework.boot.autoconfigure.EnableAutoConfiguration");

                if (autoConfigClasses != null) {
                    System.out.println("✅ EnableAutoConfiguration property found");
                    System.out.println("📝 Auto-config classes: " + autoConfigClasses);

                    // Check if our FDD config is listed
                    if (autoConfigClasses.contains("FddAutoConfiguration")) {
                        System.out.println("✅ FddAutoConfiguration is registered");
                    } else {
                        System.out.println("❌ FddAutoConfiguration NOT found in registration");
                    }
                } else {
                    System.out.println("❌ EnableAutoConfiguration property NOT found");
                }

                is.close();
            } else {
                System.out.println("❌ spring.factories NOT found on classpath");
            }
        } catch (Exception e) {
            System.out.println("❌ Error reading spring.factories: " + e.getMessage());
        }
    }

    private void checkFddClasses() {
        String[] fddClasses = {
                "com.fdd.core.config.FddAutoConfiguration",
                "com.fdd.core.registry.FunctionRegistry",
                "com.fdd.core.config.ServerlessConfigLoader",
                "com.fdd.demo.functions.UserValidationFunction"
        };

        for (String className : fddClasses) {
            try {
                Class.forName(className);
                System.out.println("✅ " + className);
            } catch (ClassNotFoundException e) {
                System.out.println("❌ " + className + " - NOT FOUND");
            }
        }
    }

    private void checkAutoConfiguration() {
        // Check if our auto-configuration was processed
        try {
            if (applicationContext.containsBean("fddAutoConfiguration")) {
                System.out.println("✅ FddAutoConfiguration bean created");
                Object autoConfig = applicationContext.getBean("fddAutoConfiguration");
                System.out.println("📦 Type: " + autoConfig.getClass().getName());
            } else {
                System.out.println("❌ FddAutoConfiguration bean NOT created");
            }
        } catch (Exception e) {
            System.out.println("❌ Error checking auto-configuration: " + e.getMessage());
        }
    }

    private void listAllBeans() {
        String[] allBeans = applicationContext.getBeanDefinitionNames();
        System.out.println("📊 Total beans: " + allBeans.length);

        int fddBeanCount = 0;
        for (String beanName : allBeans) {
            if (beanName.toLowerCase().contains("fdd") ||
                    beanName.toLowerCase().contains("function")) {
                System.out.println("🔧 " + beanName);
                fddBeanCount++;
            }
        }

        if (fddBeanCount == 0) {
            System.out.println("❌ No FDD-related beans found!");
        } else {
            System.out.println("✅ Found " + fddBeanCount + " FDD-related beans");
        }
    }

    private void checkFddBeans() {
        // Check specific FDD beans
        String[] expectedBeans = {
                "functionRegistry",
                "serverlessConfigLoader",
                "functionDiscoveryController"
        };

        for (String beanName : expectedBeans) {
            if (applicationContext.containsBean(beanName)) {
                System.out.println("✅ " + beanName + " bean exists");
                try {
                    Object bean = applicationContext.getBean(beanName);
                    System.out.println("   Type: " + bean.getClass().getName());
                } catch (Exception e) {
                    System.out.println("   ❌ Error getting bean: " + e.getMessage());
                }
            } else {
                System.out.println("❌ " + beanName + " bean MISSING");
            }
        }
    }
}
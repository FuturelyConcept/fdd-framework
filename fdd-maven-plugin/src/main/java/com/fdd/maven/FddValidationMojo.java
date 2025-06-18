// FddValidationMojo.java
package com.fdd.maven;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

import java.io.File;
import java.io.IOException;
import java.util.Map;

/**
 * Maven plugin goal to validate serverless.yml configuration
 */
@Mojo(name = "validate", defaultPhase = LifecyclePhase.VALIDATE)
public class FddValidationMojo extends AbstractMojo {

    @Parameter(defaultValue = "${project}", readonly = true, required = true)
    private MavenProject project;

    @Parameter(property = "serverless.config", defaultValue = "src/main/resources/serverless.yml")
    private String serverlessConfig;

    @Parameter(property = "fdd.validation.strict", defaultValue = "false")
    private boolean strictValidation;

    private final ObjectMapper yamlMapper = new ObjectMapper(new YAMLFactory());

    @Override
    public void execute() throws MojoExecutionException {
        getLog().info("FDD Framework: Validating serverless configuration...");

        File configFile = new File(project.getBasedir(), serverlessConfig);
        if (!configFile.exists()) {
            if (strictValidation) {
                throw new MojoExecutionException("Serverless configuration not found: " + configFile.getAbsolutePath());
            } else {
                getLog().warn("Serverless configuration not found: " + configFile.getAbsolutePath());
                return;
            }
        }

        try {
            validateConfiguration(configFile);
            getLog().info("FDD Framework: Configuration validation completed successfully");
        } catch (Exception e) {
            throw new MojoExecutionException("Configuration validation failed", e);
        }
    }

    private void validateConfiguration(File configFile) throws IOException, MojoExecutionException {
        Map<String, Object> config = yamlMapper.readValue(configFile, Map.class);

        Map<String, Object> serverless = (Map<String, Object>) config.get("serverless");
        if (serverless == null) {
            throw new MojoExecutionException("Missing 'serverless' root element in configuration");
        }

        Map<String, Object> functions = (Map<String, Object>) serverless.get("functions");
        if (functions == null || functions.isEmpty()) {
            getLog().warn("No functions defined in serverless configuration");
            return;
        }

        getLog().info("Found " + functions.size() + " function(s) in configuration");

        for (Map.Entry<String, Object> entry : functions.entrySet()) {
            validateFunction(entry.getKey(), (Map<String, Object>) entry.getValue());
        }
    }

    private void validateFunction(String functionName, Map<String, Object> functionConfig) throws MojoExecutionException {
        getLog().debug("Validating function: " + functionName);

        // Validate required fields
        String name = (String) functionConfig.get("name");
        if (name == null || name.trim().isEmpty()) {
            throw new MojoExecutionException("Function '" + functionName + "' missing required 'name' field");
        }

        String component = (String) functionConfig.get("component");
        if (component == null || component.trim().isEmpty()) {
            throw new MojoExecutionException("Function '" + functionName + "' missing required 'component' field");
        }

        // Validate input/output types if present
        String input = (String) functionConfig.get("input");
        String output = (String) functionConfig.get("output");

        if (input != null && !isValidClassName(input)) {
            getLog().warn("Function '" + functionName + "' has potentially invalid input type: " + input);
        }

        if (output != null && !isValidClassName(output)) {
            getLog().warn("Function '" + functionName + "' has potentially invalid output type: " + output);
        }

        getLog().debug("Function '" + functionName + "' validation completed");
    }

    private boolean isValidClassName(String className) {
        // Basic validation for Java class names
        return className != null &&
                className.matches("^[a-zA-Z_$][a-zA-Z\\d_$]*(?:\\.[a-zA-Z_$][a-zA-Z\\d_$]*)*$");
    }
}

// FddGenerateMojo.java
package com.fdd.maven;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Maven plugin goal to generate function contracts and documentation
 */
@Mojo(name = "generate", defaultPhase = LifecyclePhase.GENERATE_SOURCES)
public class FddGenerateMojo extends AbstractMojo {

    @Parameter(defaultValue = "${project}", readonly = true, required = true)
    private MavenProject project;

    @Parameter(property = "serverless.config", defaultValue = "src/main/resources/serverless.yml")
    private String serverlessConfig;

    @Parameter(property = "fdd.output.dir", defaultValue = "${project.build.directory}/generated-sources/fdd")
    private String outputDir;

    @Override
    public void execute() throws MojoExecutionException {
        getLog().info("FDD Framework: Generating function contracts...");

        File configFile = new File(project.getBasedir(), serverlessConfig);
        if (!configFile.exists()) {
            getLog().warn("Serverless configuration not found, skipping generation: " + configFile.getAbsolutePath());
            return;
        }

        try {
            generateContracts();
            getLog().info("FDD Framework: Contract generation completed");
        } catch (Exception e) {
            throw new MojoExecutionException("Contract generation failed", e);
        }
    }

    private void generateContracts() throws IOException {
        File outputDirectory = new File(outputDir);
        if (!outputDirectory.exists()) {
            outputDirectory.mkdirs();
        }

        // Generate function registry documentation
        File registryDoc = new File(outputDirectory, "function-registry.md");
        try (FileWriter writer = new FileWriter(registryDoc)) {
            writer.write("# Function Registry\n\n");
            writer.write("This file is auto-generated by the FDD Maven plugin.\n\n");
            writer.write("## Available Functions\n\n");
            writer.write("Functions are automatically discovered and registered at runtime.\n");
            writer.write("See `/functions` endpoint for live function registry.\n");
        }

        getLog().info("Generated function registry documentation: " + registryDoc.getAbsolutePath());
    }
}

// Updated pom.xml for maven plugin
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
xsi:schemaLocation="http://maven.apache.org/POM/4.0.0
http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <parent>
        <groupId>com.fdd</groupId>
        <artifactId>fdd-framework</artifactId>
        <version>1.0.0-SNAPSHOT</version>
    </parent>

    <artifactId>fdd-maven-plugin</artifactId>
    <packaging>maven-plugin</packaging>
<name>FDD Maven Plugin</name>
<description>Maven plugin for serverless.yml processing and validation</description>

    <dependencies>
        <!-- Maven Plugin API -->
        <dependency>
            <groupId>org.apache.maven</groupId>
            <artifactId>maven-plugin-api</artifactId>
            <version>3.9.4</version>
            <scope>provided</scope>
        </dependency>

        <dependency>
            <groupId>org.apache.maven</groupId>
            <artifactId>maven-core</artifactId>
            <version>3.9.4</version>
            <scope>provided</scope>
        </dependency>

        <!-- Plugin Annotations -->
        <dependency>
            <groupId>org.apache.maven.plugin-tools</groupId>
            <artifactId>maven-plugin-annotations</artifactId>
            <version>3.9.0</version>
            <scope>provided</scope>
        </dependency>

        <!-- Jackson for YAML parsing -->
        <dependency>
            <groupId>com.fasterxml.jackson.core</groupId>
            <artifactId>jackson-databind</artifactId>
        </dependency>
        <dependency>
            <groupId>com.fasterxml.jackson.dataformat</groupId>
            <artifactId>jackson-dataformat-yaml</artifactId>
        </dependency>
    </dependencies>

    <build>
        <plugins>
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-plugin-plugin</artifactId>
                <version>3.9.0</version>
                <configuration>
                    <goalPrefix>fdd</goalPrefix>
                </configuration>
            </plugin>
        </plugins>
    </build>
</project>
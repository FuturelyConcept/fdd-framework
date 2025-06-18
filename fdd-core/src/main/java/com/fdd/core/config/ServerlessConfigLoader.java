package com.fdd.core.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

/**
 * Loads serverless.yml configuration and creates function metadata
 */
public class ServerlessConfigLoader {
    private final ObjectMapper yamlMapper = new ObjectMapper(new YAMLFactory());
    
    // TODO: Implement serverless.yml loading and parsing
}

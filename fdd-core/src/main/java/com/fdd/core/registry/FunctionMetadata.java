package com.fdd.core.registry;

import java.util.List;
import java.util.Objects;

/**
 * Metadata for a registered function from serverless.yml
 */
public class FunctionMetadata {
    private String name;
    private String component;
    private Class<?> inputType;
    private Class<?> outputType;
    private SecurityMetadata security;
    private DeploymentMetadata deployment;

    public FunctionMetadata() {}

    public FunctionMetadata(String name, String component,
                            Class<?> inputType, Class<?> outputType) {
        this.name = name;
        this.component = component;
        this.inputType = inputType;
        this.outputType = outputType;
    }

    // Getters and setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getComponent() { return component; }
    public void setComponent(String component) { this.component = component; }

    public Class<?> getInputType() { return inputType; }
    public void setInputType(Class<?> inputType) { this.inputType = inputType; }

    public Class<?> getOutputType() { return outputType; }
    public void setOutputType(Class<?> outputType) { this.outputType = outputType; }

    public SecurityMetadata getSecurity() { return security; }
    public void setSecurity(SecurityMetadata security) { this.security = security; }

    public DeploymentMetadata getDeployment() { return deployment; }
    public void setDeployment(DeploymentMetadata deployment) { this.deployment = deployment; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof FunctionMetadata)) return false;
        FunctionMetadata that = (FunctionMetadata) o;
        return Objects.equals(name, that.name) && Objects.equals(component, that.component);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, component);
    }

    @Override
    public String toString() {
        return "FunctionMetadata{" +
                "name='" + name + '\'' +
                ", component='" + component + '\'' +
                ", inputType=" + inputType +
                ", outputType=" + outputType +
                '}';
    }

    /**
     * Security configuration for a function
     */
    public static class SecurityMetadata {
        private String group;
        private List<String> roles;
        private String authentication;
        private boolean elevated;

        public SecurityMetadata() {}

        public String getGroup() { return group; }
        public void setGroup(String group) { this.group = group; }

        public List<String> getRoles() { return roles; }
        public void setRoles(List<String> roles) { this.roles = roles; }

        public String getAuthentication() { return authentication; }
        public void setAuthentication(String authentication) { this.authentication = authentication; }

        public boolean isElevated() { return elevated; }
        public void setElevated(boolean elevated) { this.elevated = elevated; }
    }

    /**
     * Deployment configuration for a function
     */
    public static class DeploymentMetadata {
        private String cloud;
        private String memory;
        private String timeout;

        public DeploymentMetadata() {}

        public String getCloud() { return cloud; }
        public void setCloud(String cloud) { this.cloud = cloud; }

        public String getMemory() { return memory; }
        public void setMemory(String memory) { this.memory = memory; }

        public String getTimeout() { return timeout; }
        public void setTimeout(String timeout) { this.timeout = timeout; }
    }
}
package com.fdd.core.config;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Map;
import java.util.List;

/**
 * Configuration classes for serverless.yml parsing
 */
public class ServerlessConfig {

    @JsonProperty("serverless")
    private ServerlessDefinition serverless;

    public ServerlessDefinition getServerless() { return serverless; }
    public void setServerless(ServerlessDefinition serverless) { this.serverless = serverless; }

    public static class ServerlessDefinition {
        private String service;
        private ProviderConfig provider;
        private Map<String, FunctionConfig> functions;
        private SecurityConfig security;
        private DiscoveryConfig discovery;

        public String getService() { return service; }
        public void setService(String service) { this.service = service; }

        public ProviderConfig getProvider() { return provider; }
        public void setProvider(ProviderConfig provider) { this.provider = provider; }

        public Map<String, FunctionConfig> getFunctions() { return functions; }
        public void setFunctions(Map<String, FunctionConfig> functions) { this.functions = functions; }

        public SecurityConfig getSecurity() { return security; }
        public void setSecurity(SecurityConfig security) { this.security = security; }

        public DiscoveryConfig getDiscovery() { return discovery; }
        public void setDiscovery(DiscoveryConfig discovery) { this.discovery = discovery; }
    }

    public static class ProviderConfig {
        private String name;
        private String runtime;

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public String getRuntime() { return runtime; }
        public void setRuntime(String runtime) { this.runtime = runtime; }
    }

    public static class FunctionConfig {
        private String name;
        private String component;
        private String input;
        private String output;
        private SecurityConfig security;
        private DeploymentConfig deployment;

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public String getComponent() { return component; }
        public void setComponent(String component) { this.component = component; }

        public String getInput() { return input; }
        public void setInput(String input) { this.input = input; }

        public String getOutput() { return output; }
        public void setOutput(String output) { this.output = output; }

        public SecurityConfig getSecurity() { return security; }
        public void setSecurity(SecurityConfig security) { this.security = security; }

        public DeploymentConfig getDeployment() { return deployment; }
        public void setDeployment(DeploymentConfig deployment) { this.deployment = deployment; }
    }

    public static class SecurityConfig {
        private String group;
        private List<String> roles;
        private String authentication;
        private boolean elevated;
        private JwtConfig jwt;
        private Map<String, GroupConfig> groups;

        public String getGroup() { return group; }
        public void setGroup(String group) { this.group = group; }

        public List<String> getRoles() { return roles; }
        public void setRoles(List<String> roles) { this.roles = roles; }

        public String getAuthentication() { return authentication; }
        public void setAuthentication(String authentication) { this.authentication = authentication; }

        public boolean isElevated() { return elevated; }
        public void setElevated(boolean elevated) { this.elevated = elevated; }

        public JwtConfig getJwt() { return jwt; }
        public void setJwt(JwtConfig jwt) { this.jwt = jwt; }

        public Map<String, GroupConfig> getGroups() { return groups; }
        public void setGroups(Map<String, GroupConfig> groups) { this.groups = groups; }
    }

    public static class JwtConfig {
        private String issuer;
        private String audience;

        public String getIssuer() { return issuer; }
        public void setIssuer(String issuer) { this.issuer = issuer; }

        public String getAudience() { return audience; }
        public void setAudience(String audience) { this.audience = audience; }
    }

    public static class GroupConfig {
        private String description;
        @JsonProperty("allowed-callers")
        private List<String> allowedCallers;

        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }

        public List<String> getAllowedCallers() { return allowedCallers; }
        public void setAllowedCallers(List<String> allowedCallers) { this.allowedCallers = allowedCallers; }
    }

    public static class DeploymentConfig {
        private String cloud;
        private String memory;
        private String timeout;

        public String getCloud() { return cloud; }
        public void setCloud(String cloud) { this.cloud = cloud; }

        public String getMemory() { return memory; }
        public void setMemory(String memory) { this.memory = memory; }

        public String getTimeout() { return timeout; }
        public void setTimeout(String timeout) { this.timeout = timeout; }
    }

    public static class DiscoveryConfig {
        private boolean enabled;
        private String endpoint;

        public boolean isEnabled() { return enabled; }
        public void setEnabled(boolean enabled) { this.enabled = enabled; }

        public String getEndpoint() { return endpoint; }
        public void setEndpoint(String endpoint) { this.endpoint = endpoint; }
    }
}
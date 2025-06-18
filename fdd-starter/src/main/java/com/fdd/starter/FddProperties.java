package com.fdd.starter;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration properties for FDD framework
 */
@ConfigurationProperties(prefix = "fdd")
public class FddProperties {

    private Function function = new Function();
    private Security security = new Security();
    private Config config = new Config();

    public Function getFunction() { return function; }
    public void setFunction(Function function) { this.function = function; }

    public Security getSecurity() { return security; }
    public void setSecurity(Security security) { this.security = security; }

    public Config getConfig() { return config; }
    public void setConfig(Config config) { this.config = config; }

    public static class Function {
        private boolean enabled = true;
        private Discovery discovery = new Discovery();
        private Registry registry = new Registry();

        public boolean isEnabled() { return enabled; }
        public void setEnabled(boolean enabled) { this.enabled = enabled; }

        public Discovery getDiscovery() { return discovery; }
        public void setDiscovery(Discovery discovery) { this.discovery = discovery; }

        public Registry getRegistry() { return registry; }
        public void setRegistry(Registry registry) { this.registry = registry; }
    }

    public static class Discovery {
        private boolean enabled = true;
        private String endpoint = "/functions";

        public boolean isEnabled() { return enabled; }
        public void setEnabled(boolean enabled) { this.enabled = enabled; }

        public String getEndpoint() { return endpoint; }
        public void setEndpoint(String endpoint) { this.endpoint = endpoint; }
    }

    public static class Registry {
        private String scanPackages = "com.fdd";

        public String getScanPackages() { return scanPackages; }
        public void setScanPackages(String scanPackages) { this.scanPackages = scanPackages; }
    }

    public static class Security {
        private ContextPropagation contextPropagation = new ContextPropagation();

        public ContextPropagation getContextPropagation() { return contextPropagation; }
        public void setContextPropagation(ContextPropagation contextPropagation) {
            this.contextPropagation = contextPropagation;
        }
    }

    public static class ContextPropagation {
        private boolean enabled = true;
        private String mode = "jwt";

        public boolean isEnabled() { return enabled; }
        public void setEnabled(boolean enabled) { this.enabled = enabled; }

        public String getMode() { return mode; }
        public void setMode(String mode) { this.mode = mode; }
    }

    public static class Config {
        private String serverlessYmlLocation = "classpath:serverless.yml";

        public String getServerlessYmlLocation() { return serverlessYmlLocation; }
        public void setServerlessYmlLocation(String serverlessYmlLocation) {
            this.serverlessYmlLocation = serverlessYmlLocation;
        }
    }
}
package com.fdd.core.discovery;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Analyzes and tracks dependencies between functions
 */
@Component
public class FunctionDependencyAnalyzer {
    private static final Logger logger = LoggerFactory.getLogger(FunctionDependencyAnalyzer.class);

    // Map: function -> set of functions it calls
    private final Map<String, Set<String>> functionCalls = new ConcurrentHashMap<>();

    // Map: function -> set of functions that call it
    private final Map<String, Set<String>> functionCallers = new ConcurrentHashMap<>();

    /**
     * Record that one function calls another
     */
    public void recordFunctionCall(String callerFunction, String calledFunction) {
        // Record the call relationship
        functionCalls.computeIfAbsent(callerFunction, k -> ConcurrentHashMap.newKeySet())
                .add(calledFunction);

        // Record the reverse relationship
        functionCallers.computeIfAbsent(calledFunction, k -> ConcurrentHashMap.newKeySet())
                .add(callerFunction);

        logger.debug("Recorded dependency: {} -> {}", callerFunction, calledFunction);
    }

    /**
     * Get functions that a given function calls
     */
    public Set<String> getFunctionDependencies(String functionName) {
        return Set.copyOf(functionCalls.getOrDefault(functionName, Set.of()));
    }

    /**
     * Get functions that call a given function
     */
    public Set<String> getFunctionCallers(String functionName) {
        return Set.copyOf(functionCallers.getOrDefault(functionName, Set.of()));
    }

    /**
     * Get complete dependency information for a function
     */
    public FunctionDependencyInfo getDependencyInfo(String functionName) {
        return new FunctionDependencyInfo(
                functionName,
                getFunctionDependencies(functionName),
                getFunctionCallers(functionName)
        );
    }

    /**
     * Get dependency information for all functions
     */
    public Map<String, FunctionDependencyInfo> getAllDependencies() {
        Map<String, FunctionDependencyInfo> result = new HashMap<>();

        // Get all known functions
        Set<String> allFunctions = new HashSet<>();
        allFunctions.addAll(functionCalls.keySet());
        allFunctions.addAll(functionCallers.keySet());

        // Build dependency info for each function
        for (String functionName : allFunctions) {
            result.put(functionName, getDependencyInfo(functionName));
        }

        return result;
    }

    /**
     * Get functions with no dependencies (leaf functions)
     */
    public Set<String> getLeafFunctions() {
        return functionCalls.entrySet().stream()
                .filter(entry -> entry.getValue().isEmpty())
                .map(Map.Entry::getKey)
                .collect(HashSet::new, Set::add, Set::addAll);
    }

    /**
     * Get functions that no other function calls (root functions)
     */
    public Set<String> getRootFunctions() {
        return functionCallers.entrySet().stream()
                .filter(entry -> entry.getValue().isEmpty())
                .map(Map.Entry::getKey)
                .collect(HashSet::new, Set::add, Set::addAll);
    }

    /**
     * Check if there are circular dependencies
     */
    public List<List<String>> findCircularDependencies() {
        List<List<String>> cycles = new ArrayList<>();
        Set<String> visited = new HashSet<>();
        Set<String> recursionStack = new HashSet<>();

        for (String function : functionCalls.keySet()) {
            if (!visited.contains(function)) {
                List<String> currentPath = new ArrayList<>();
                if (hasCycleDFS(function, visited, recursionStack, currentPath, cycles)) {
                    // Cycle detected and added to cycles list
                }
            }
        }

        return cycles;
    }

    /**
     * DFS helper for cycle detection
     */
    private boolean hasCycleDFS(String function, Set<String> visited, Set<String> recursionStack,
                                List<String> currentPath, List<List<String>> cycles) {
        visited.add(function);
        recursionStack.add(function);
        currentPath.add(function);

        Set<String> dependencies = functionCalls.getOrDefault(function, Set.of());
        for (String dependency : dependencies) {
            if (!visited.contains(dependency)) {
                if (hasCycleDFS(dependency, visited, recursionStack, currentPath, cycles)) {
                    return true;
                }
            } else if (recursionStack.contains(dependency)) {
                // Found a cycle
                int cycleStart = currentPath.indexOf(dependency);
                List<String> cycle = new ArrayList<>(currentPath.subList(cycleStart, currentPath.size()));
                cycle.add(dependency); // Complete the cycle
                cycles.add(cycle);
                return true;
            }
        }

        recursionStack.remove(function);
        currentPath.remove(currentPath.size() - 1);
        return false;
    }

    /**
     * Generate dependency graph in DOT format for visualization
     */
    public String generateDotGraph() {
        StringBuilder dot = new StringBuilder();
        dot.append("digraph FunctionDependencies {\n");
        dot.append("  rankdir=TB;\n");
        dot.append("  node [shape=box, style=rounded];\n\n");

        for (Map.Entry<String, Set<String>> entry : functionCalls.entrySet()) {
            String caller = entry.getKey();
            for (String called : entry.getValue()) {
                dot.append(String.format("  \"%s\" -> \"%s\";\n", caller, called));
            }
        }

        dot.append("}\n");
        return dot.toString();
    }

    /**
     * Clear all dependency tracking
     */
    public void clearDependencies() {
        functionCalls.clear();
        functionCallers.clear();
        logger.info("Cleared all function dependencies");
    }

    /**
     * Dependency information for a single function
     */
    public static class FunctionDependencyInfo {
        private final String functionName;
        private final Set<String> callsTo;      // Functions this function calls
        private final Set<String> calledBy;     // Functions that call this function

        public FunctionDependencyInfo(String functionName, Set<String> callsTo, Set<String> calledBy) {
            this.functionName = functionName;
            this.callsTo = callsTo;
            this.calledBy = calledBy;
        }

        public String getFunctionName() { return functionName; }
        public Set<String> getCallsTo() { return callsTo; }
        public Set<String> getCalledBy() { return calledBy; }

        public boolean isLeafFunction() { return callsTo.isEmpty(); }
        public boolean isRootFunction() { return calledBy.isEmpty(); }

        public Map<String, Object> toMap() {
            Map<String, Object> map = new HashMap<>();
            map.put("functionName", functionName);
            map.put("callsTo", callsTo);
            map.put("calledBy", calledBy);
            map.put("isLeaf", isLeafFunction());
            map.put("isRoot", isRootFunction());
            map.put("dependencyCount", callsTo.size());
            map.put("dependentCount", calledBy.size());
            return map;
        }
    }
}
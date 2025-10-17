package com.sheetpilot.transformation;

import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * Factory for creating transformation strategies
 * Uses singleton pattern to provide thread-safe access to strategies
 */
@Component
public class TransformationFactory {

    private final Map<String, TransformationStrategy> strategies;

    public TransformationFactory() {
        this.strategies = new HashMap<>();
        registerDefaultStrategies();
    }

    /**
     * Register all default transformation strategies
     */
    private void registerDefaultStrategies() {
        register(new FilterTransformation());
        register(new SortTransformation());
        register(new SelectTransformation());
        register(new RenameTransformation());
        register(new JoinTransformation());
    }

    /**
     * Register a transformation strategy
     */
    public void register(TransformationStrategy strategy) {
        strategies.put(strategy.getType(), strategy);
    }

    /**
     * Get a transformation strategy by type
     *
     * @param type Transformation type (e.g., "filter", "sort")
     * @return Strategy instance
     * @throws IllegalArgumentException if strategy not found
     */
    public TransformationStrategy getStrategy(String type) {
        if (type == null || type.isEmpty()) {
            throw new IllegalArgumentException("Transformation type cannot be null or empty");
        }

        TransformationStrategy strategy = strategies.get(type.toLowerCase());
        if (strategy == null) {
            throw new IllegalArgumentException("Unknown transformation type: " + type);
        }

        return strategy;
    }

    /**
     * Check if a transformation type is supported
     */
    public boolean isSupported(String type) {
        return type != null && strategies.containsKey(type.toLowerCase());
    }

    /**
     * Get all registered transformation types
     */
    public Map<String, TransformationStrategy> getAllStrategies() {
        return Map.copyOf(strategies);
    }
}

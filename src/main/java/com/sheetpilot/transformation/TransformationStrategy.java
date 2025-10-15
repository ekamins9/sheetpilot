package com.sheetpilot.transformation;

import java.util.Map;

/**
 * Strategy interface for spreadsheet transformations
 * Each transformation type implements this interface with its own logic
 */
public interface TransformationStrategy {

    /**
     * Apply transformation to a DataFrame
     *
     * @param dataFrame Input data frame
     * @param config Transformation configuration (JSON deserialized to Map)
     * @return Transformed DataFrame
     * @throws TransformationException if transformation fails
     */
    DataFrame apply(DataFrame dataFrame, Map<String, Object> config) throws TransformationException;

    /**
     * Validate transformation configuration
     *
     * @param config Configuration to validate
     * @throws TransformationException if configuration is invalid
     */
    void validateConfig(Map<String, Object> config) throws TransformationException;

    /**
     * Get the transformation type name
     *
     * @return Type name (e.g., "filter", "sort", "select")
     */
    String getType();
}

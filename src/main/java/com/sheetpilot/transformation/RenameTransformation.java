package com.sheetpilot.transformation;

import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Rename transformation - renames columns in the DataFrame
 *
 * Config format:
 * {
 *   "mapping": {
 *     "old_name": "new_name",
 *     "email_address": "email"
 *   }
 * }
 */
@Slf4j
public class RenameTransformation implements TransformationStrategy {

    private static final String TYPE = "rename";

    @Override
    public DataFrame apply(DataFrame dataFrame, Map<String, Object> config) throws TransformationException {
        validateConfig(config);

        @SuppressWarnings("unchecked")
        Map<String, String> mapping = (Map<String, String>) config.get("mapping");

        // Validate all old column names exist
        for (String oldName : mapping.keySet()) {
            if (!dataFrame.hasColumn(oldName)) {
                throw new TransformationException("Column not found: " + oldName);
            }
        }

        // Check for duplicate new names
        List<String> newNames = new ArrayList<>(mapping.values());
        long uniqueCount = newNames.stream().distinct().count();
        if (uniqueCount != newNames.size()) {
            throw new TransformationException("Duplicate column names in mapping");
        }

        // Create new headers with renamed columns
        List<String> newHeaders = new ArrayList<>();
        for (String header : dataFrame.getHeaders()) {
            String newName = mapping.getOrDefault(header, header);
            newHeaders.add(newName);
        }

        // Check for duplicate headers after renaming
        long uniqueHeaders = newHeaders.stream().distinct().count();
        if (uniqueHeaders != newHeaders.size()) {
            throw new TransformationException("Renaming would create duplicate column names");
        }

        log.debug("Rename: {} columns renamed", mapping.size());

        return DataFrame.builder()
                .headers(newHeaders)
                .rows(new ArrayList<>(dataFrame.getRows()))
                .build();
    }

    @Override
    public void validateConfig(Map<String, Object> config) throws TransformationException {
        if (config == null || config.isEmpty()) {
            throw new TransformationException("Rename configuration cannot be empty");
        }

        if (!config.containsKey("mapping")) {
            throw new TransformationException("Rename must specify 'mapping'");
        }

        Object mappingObj = config.get("mapping");
        if (!(mappingObj instanceof Map)) {
            throw new TransformationException("'mapping' must be a map/object");
        }

        @SuppressWarnings("unchecked")
        Map<String, String> mapping = (Map<String, String>) mappingObj;

        if (mapping.isEmpty()) {
            throw new TransformationException("'mapping' cannot be empty");
        }

        // Validate all values are strings
        for (Map.Entry<String, String> entry : mapping.entrySet()) {
            if (entry.getKey() == null || entry.getKey().isEmpty()) {
                throw new TransformationException("Column name cannot be null or empty");
            }
            if (entry.getValue() == null || entry.getValue().isEmpty()) {
                throw new TransformationException("New column name cannot be null or empty");
            }
        }
    }

    @Override
    public String getType() {
        return TYPE;
    }
}

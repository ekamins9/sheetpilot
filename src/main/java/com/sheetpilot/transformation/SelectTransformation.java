package com.sheetpilot.transformation;

import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Select transformation - selects specific columns from the DataFrame
 *
 * Config format:
 * {
 *   "columns": ["name", "email", "age"]
 * }
 */
@Slf4j
public class SelectTransformation implements TransformationStrategy {

    private static final String TYPE = "select";

    @Override
    public DataFrame apply(DataFrame dataFrame, Map<String, Object> config) throws TransformationException {
        validateConfig(config);

        @SuppressWarnings("unchecked")
        List<String> selectedColumns = (List<String>) config.get("columns");

        // Validate all columns exist
        for (String column : selectedColumns) {
            if (!dataFrame.hasColumn(column)) {
                throw new TransformationException("Column not found: " + column);
            }
        }

        // Get column indices
        List<Integer> columnIndices = selectedColumns.stream()
                .map(dataFrame::getColumnIndex)
                .toList();

        // Build new rows with only selected columns
        List<List<String>> newRows = new ArrayList<>();
        for (List<String> row : dataFrame.getRows()) {
            List<String> newRow = new ArrayList<>();
            for (Integer index : columnIndices) {
                newRow.add(row.get(index));
            }
            newRows.add(newRow);
        }

        log.debug("Select: {} columns selected from {} total",
                selectedColumns.size(), dataFrame.getColumnCount());

        return DataFrame.builder()
                .headers(new ArrayList<>(selectedColumns))
                .rows(newRows)
                .build();
    }

    @Override
    public void validateConfig(Map<String, Object> config) throws TransformationException {
        if (config == null || config.isEmpty()) {
            throw new TransformationException("Select configuration cannot be empty");
        }

        if (!config.containsKey("columns")) {
            throw new TransformationException("Select must specify 'columns'");
        }

        Object columnsObj = config.get("columns");
        if (!(columnsObj instanceof List)) {
            throw new TransformationException("'columns' must be a list");
        }

        @SuppressWarnings("unchecked")
        List<String> columns = (List<String>) columnsObj;

        if (columns.isEmpty()) {
            throw new TransformationException("'columns' list cannot be empty");
        }
    }

    @Override
    public String getType() {
        return TYPE;
    }
}

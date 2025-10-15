package com.sheetpilot.transformation;

import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

/**
 * Sort transformation - sorts rows by one or more columns
 *
 * Config format:
 * {
 *   "column": "name",
 *   "direction": "ASC"  // "ASC" or "DESC"
 * }
 *
 * Or for multiple columns:
 * {
 *   "columns": ["name", "age"],
 *   "directions": ["ASC", "DESC"]
 * }
 */
@Slf4j
public class SortTransformation implements TransformationStrategy {

    private static final String TYPE = "sort";

    @Override
    public DataFrame apply(DataFrame dataFrame, Map<String, Object> config) throws TransformationException {
        validateConfig(config);

        List<List<String>> sortedRows = new ArrayList<>(dataFrame.getRows());

        // Handle single column sort
        if (config.containsKey("column")) {
            String column = (String) config.get("column");
            String direction = (String) config.getOrDefault("direction", "ASC");

            if (!dataFrame.hasColumn(column)) {
                throw new TransformationException("Column not found: " + column);
            }

            int columnIndex = dataFrame.getColumnIndex(column);
            Comparator<List<String>> comparator = createComparator(columnIndex, direction);
            sortedRows.sort(comparator);

            log.debug("Sort: {} {} - {} rows sorted", column, direction, sortedRows.size());

        // Handle multiple column sort
        } else if (config.containsKey("columns")) {
            @SuppressWarnings("unchecked")
            List<String> columns = (List<String>) config.get("columns");

            @SuppressWarnings("unchecked")
            List<String> directions = (List<String>) config.getOrDefault("directions",
                    new ArrayList<>(List.of("ASC").stream().limit(columns.size()).toList()));

            // Validate all columns exist
            for (String column : columns) {
                if (!dataFrame.hasColumn(column)) {
                    throw new TransformationException("Column not found: " + column);
                }
            }

            // Build composite comparator
            Comparator<List<String>> comparator = null;
            for (int i = 0; i < columns.size(); i++) {
                int columnIndex = dataFrame.getColumnIndex(columns.get(i));
                String direction = i < directions.size() ? directions.get(i) : "ASC";
                Comparator<List<String>> colComparator = createComparator(columnIndex, direction);

                comparator = (comparator == null) ? colComparator : comparator.thenComparing(colComparator);
            }

            sortedRows.sort(comparator);
            log.debug("Sort: {} columns - {} rows sorted", columns.size(), sortedRows.size());
        }

        return DataFrame.builder()
                .headers(new ArrayList<>(dataFrame.getHeaders()))
                .rows(sortedRows)
                .build();
    }

    @Override
    public void validateConfig(Map<String, Object> config) throws TransformationException {
        if (config == null || config.isEmpty()) {
            throw new TransformationException("Sort configuration cannot be empty");
        }

        boolean hasSingleColumn = config.containsKey("column");
        boolean hasMultipleColumns = config.containsKey("columns");

        if (!hasSingleColumn && !hasMultipleColumns) {
            throw new TransformationException("Sort must specify either 'column' or 'columns'");
        }

        if (hasSingleColumn && hasMultipleColumns) {
            throw new TransformationException("Sort cannot specify both 'column' and 'columns'");
        }

        // Validate direction if specified
        if (config.containsKey("direction")) {
            String direction = (String) config.get("direction");
            if (!direction.equals("ASC") && !direction.equals("DESC")) {
                throw new TransformationException("Invalid direction: " + direction + ". Must be 'ASC' or 'DESC'");
            }
        }

        // Validate directions array if specified
        if (config.containsKey("directions")) {
            @SuppressWarnings("unchecked")
            List<String> directions = (List<String>) config.get("directions");

            for (String direction : directions) {
                if (!direction.equals("ASC") && !direction.equals("DESC")) {
                    throw new TransformationException("Invalid direction: " + direction + ". Must be 'ASC' or 'DESC'");
                }
            }
        }
    }

    @Override
    public String getType() {
        return TYPE;
    }

    /**
     * Create a comparator for a specific column and direction
     */
    private Comparator<List<String>> createComparator(int columnIndex, String direction) {
        Comparator<List<String>> comparator = (row1, row2) -> {
            String val1 = row1.get(columnIndex);
            String val2 = row2.get(columnIndex);

            // Try numeric comparison first
            try {
                double num1 = Double.parseDouble(val1);
                double num2 = Double.parseDouble(val2);
                return Double.compare(num1, num2);
            } catch (NumberFormatException e) {
                // Fall back to string comparison
                return val1.compareTo(val2);
            }
        };

        return direction.equals("DESC") ? comparator.reversed() : comparator;
    }
}

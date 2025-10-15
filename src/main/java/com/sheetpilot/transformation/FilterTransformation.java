package com.sheetpilot.transformation;

import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Filter transformation - filters rows based on column conditions
 *
 * Config format:
 * {
 *   "column": "age",
 *   "operator": ">",  // Supported: "=", "!=", ">", "<", ">=", "<=", "contains", "startsWith", "endsWith"
 *   "value": "18"
 * }
 */
@Slf4j
public class FilterTransformation implements TransformationStrategy {

    private static final String TYPE = "filter";

    @Override
    public DataFrame apply(DataFrame dataFrame, Map<String, Object> config) throws TransformationException {
        validateConfig(config);

        String column = (String) config.get("column");
        String operator = (String) config.get("operator");
        String value = String.valueOf(config.get("value"));

        if (!dataFrame.hasColumn(column)) {
            throw new TransformationException("Column not found: " + column);
        }

        int columnIndex = dataFrame.getColumnIndex(column);
        List<List<String>> filteredRows = new ArrayList<>();

        for (List<String> row : dataFrame.getRows()) {
            String cellValue = row.get(columnIndex);
            if (matchesCondition(cellValue, operator, value)) {
                filteredRows.add(new ArrayList<>(row));
            }
        }

        log.debug("Filter: {} {} '{}' - {} rows matched out of {}",
                column, operator, value, filteredRows.size(), dataFrame.getRowCount());

        return DataFrame.builder()
                .headers(new ArrayList<>(dataFrame.getHeaders()))
                .rows(filteredRows)
                .build();
    }

    @Override
    public void validateConfig(Map<String, Object> config) throws TransformationException {
        if (config == null || config.isEmpty()) {
            throw new TransformationException("Filter configuration cannot be empty");
        }

        if (!config.containsKey("column")) {
            throw new TransformationException("Filter must specify 'column'");
        }

        if (!config.containsKey("operator")) {
            throw new TransformationException("Filter must specify 'operator'");
        }

        if (!config.containsKey("value")) {
            throw new TransformationException("Filter must specify 'value'");
        }

        String operator = (String) config.get("operator");
        List<String> validOperators = List.of("=", "!=", ">", "<", ">=", "<=", "contains", "startsWith", "endsWith");

        if (!validOperators.contains(operator)) {
            throw new TransformationException("Invalid operator: " + operator + ". Valid operators: " + validOperators);
        }
    }

    @Override
    public String getType() {
        return TYPE;
    }

    /**
     * Check if cell value matches the filter condition
     */
    private boolean matchesCondition(String cellValue, String operator, String filterValue) {
        if (cellValue == null) {
            cellValue = "";
        }

        return switch (operator) {
            case "=" -> cellValue.equals(filterValue);
            case "!=" -> !cellValue.equals(filterValue);
            case "contains" -> cellValue.contains(filterValue);
            case "startsWith" -> cellValue.startsWith(filterValue);
            case "endsWith" -> cellValue.endsWith(filterValue);
            case ">", "<", ">=", "<=" -> compareNumeric(cellValue, operator, filterValue);
            default -> false;
        };
    }

    /**
     * Compare values numerically
     */
    private boolean compareNumeric(String cellValue, String operator, String filterValue) {
        try {
            double cell = Double.parseDouble(cellValue);
            double filter = Double.parseDouble(filterValue);

            return switch (operator) {
                case ">" -> cell > filter;
                case "<" -> cell < filter;
                case ">=" -> cell >= filter;
                case "<=" -> cell <= filter;
                default -> false;
            };
        } catch (NumberFormatException e) {
            log.warn("Cannot compare non-numeric values: '{}' and '{}'", cellValue, filterValue);
            return false;
        }
    }
}

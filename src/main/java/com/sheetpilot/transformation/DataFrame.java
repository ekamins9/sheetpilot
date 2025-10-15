package com.sheetpilot.transformation;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Represents a tabular data structure for transformations
 * Similar to pandas DataFrame but simplified for spreadsheet operations
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DataFrame {

    private List<String> headers;
    private List<List<String>> rows;

    /**
     * Get column index by name
     */
    public int getColumnIndex(String columnName) {
        int index = headers.indexOf(columnName);
        if (index == -1) {
            throw new IllegalArgumentException("Column not found: " + columnName);
        }
        return index;
    }

    /**
     * Get column by name
     */
    public List<String> getColumn(String columnName) {
        int colIndex = getColumnIndex(columnName);
        return rows.stream()
                .map(row -> row.get(colIndex))
                .collect(Collectors.toList());
    }

    /**
     * Check if column exists
     */
    public boolean hasColumn(String columnName) {
        return headers.contains(columnName);
    }

    /**
     * Get number of rows
     */
    public int getRowCount() {
        return rows.size();
    }

    /**
     * Get number of columns
     */
    public int getColumnCount() {
        return headers.size();
    }

    /**
     * Create a deep copy of this DataFrame
     */
    public DataFrame copy() {
        List<String> newHeaders = new ArrayList<>(headers);
        List<List<String>> newRows = rows.stream()
                .map(ArrayList::new)
                .collect(Collectors.toList());
        return new DataFrame(newHeaders, newRows);
    }

    /**
     * Validate DataFrame structure
     */
    public void validate() {
        if (headers == null || headers.isEmpty()) {
            throw new IllegalStateException("DataFrame must have headers");
        }

        if (rows == null) {
            throw new IllegalStateException("DataFrame rows cannot be null");
        }

        int expectedColumnCount = headers.size();
        for (int i = 0; i < rows.size(); i++) {
            List<String> row = rows.get(i);
            if (row.size() != expectedColumnCount) {
                throw new IllegalStateException(
                    String.format("Row %d has %d columns, expected %d",
                        i, row.size(), expectedColumnCount)
                );
            }
        }
    }

    /**
     * Convert to preview data format (for storage)
     */
    public Map<String, Object> toPreviewData() {
        return Map.of(
            "headers", headers,
            "rows", rows,
            "totalRows", rows.size(),
            "totalColumns", headers.size()
        );
    }

    /**
     * Create DataFrame from preview data format
     */
    public static DataFrame fromPreviewData(Map<String, Object> previewData) {
        @SuppressWarnings("unchecked")
        List<String> headers = (List<String>) previewData.get("headers");

        @SuppressWarnings("unchecked")
        List<List<String>> rows = (List<List<String>>) previewData.get("rows");

        return new DataFrame(headers, rows);
    }
}

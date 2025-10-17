package com.sheetpilot.transformation;

import lombok.extern.slf4j.Slf4j;

import java.util.*;

/**
 * Join transformation - joins two DataFrames on specified columns
 *
 * Config format:
 * {
 *   "joinType": "inner",  // "inner", "left", "right", "outer"
 *   "leftKey": "id",
 *   "rightKey": "user_id",
 *   "rightDataFrame": {...},  // Second DataFrame to join with
 *   "leftPrefix": "left_",   // Optional prefix for conflicting column names
 *   "rightPrefix": "right_"  // Optional prefix for conflicting column names
 * }
 */
@Slf4j
public class JoinTransformation implements TransformationStrategy {

    private static final String TYPE = "join";
    private static final Set<String> VALID_JOIN_TYPES = Set.of("inner", "left", "right", "outer");

    @Override
    public DataFrame apply(DataFrame leftDataFrame, Map<String, Object> config) throws TransformationException {
        validateConfig(config);

        String joinType = (String) config.get("joinType");
        String leftKey = (String) config.get("leftKey");
        String rightKey = (String) config.get("rightKey");
        String leftPrefix = (String) config.getOrDefault("leftPrefix", "");
        String rightPrefix = (String) config.getOrDefault("rightPrefix", "");

        // Extract right DataFrame from config
        @SuppressWarnings("unchecked")
        Map<String, Object> rightDataMap = (Map<String, Object>) config.get("rightDataFrame");
        DataFrame rightDataFrame = DataFrame.fromPreviewData(rightDataMap);

        // Validate keys exist
        if (!leftDataFrame.hasColumn(leftKey)) {
            throw new TransformationException("Left join key not found: " + leftKey);
        }
        if (!rightDataFrame.hasColumn(rightKey)) {
            throw new TransformationException("Right join key not found: " + rightKey);
        }

        // Build indexes for efficient lookup
        int leftKeyIndex = leftDataFrame.getColumnIndex(leftKey);
        int rightKeyIndex = rightDataFrame.getColumnIndex(rightKey);

        // Create index map for right DataFrame (key -> list of row indices)
        Map<String, List<Integer>> rightIndex = buildIndex(rightDataFrame, rightKeyIndex);

        // Resolve column name conflicts
        List<String> resultHeaders = resolveHeaders(
                leftDataFrame.getHeaders(),
                rightDataFrame.getHeaders(),
                leftKey,
                rightKey,
                leftPrefix,
                rightPrefix
        );

        // Perform join based on type
        List<List<String>> resultRows = switch (joinType.toLowerCase()) {
            case "inner" -> performInnerJoin(leftDataFrame, rightDataFrame, leftKeyIndex, rightKeyIndex, rightIndex);
            case "left" -> performLeftJoin(leftDataFrame, rightDataFrame, leftKeyIndex, rightKeyIndex, rightIndex);
            case "right" -> performRightJoin(leftDataFrame, rightDataFrame, leftKeyIndex, rightKeyIndex, rightIndex);
            case "outer" -> performOuterJoin(leftDataFrame, rightDataFrame, leftKeyIndex, rightKeyIndex, rightIndex);
            default -> throw new TransformationException("Invalid join type: " + joinType);
        };

        log.info("Join completed: {} {} - {} rows in result", joinType, leftKey, resultRows.size());

        return DataFrame.builder()
                .headers(resultHeaders)
                .rows(resultRows)
                .build();
    }

    @Override
    public void validateConfig(Map<String, Object> config) throws TransformationException {
        if (config == null || config.isEmpty()) {
            throw new TransformationException("Join configuration cannot be empty");
        }

        if (!config.containsKey("joinType")) {
            throw new TransformationException("Join must specify 'joinType'");
        }

        String joinType = (String) config.get("joinType");
        if (!VALID_JOIN_TYPES.contains(joinType.toLowerCase())) {
            throw new TransformationException(
                    "Invalid join type: " + joinType + ". Valid types: " + VALID_JOIN_TYPES
            );
        }

        if (!config.containsKey("leftKey")) {
            throw new TransformationException("Join must specify 'leftKey'");
        }

        if (!config.containsKey("rightKey")) {
            throw new TransformationException("Join must specify 'rightKey'");
        }

        if (!config.containsKey("rightDataFrame")) {
            throw new TransformationException("Join must specify 'rightDataFrame'");
        }
    }

    @Override
    public String getType() {
        return TYPE;
    }

    /**
     * Build an index map for fast lookup: key value -> list of row indices
     */
    private Map<String, List<Integer>> buildIndex(DataFrame dataFrame, int keyIndex) {
        Map<String, List<Integer>> index = new HashMap<>();

        for (int i = 0; i < dataFrame.getRowCount(); i++) {
            String key = dataFrame.getRows().get(i).get(keyIndex);
            index.computeIfAbsent(key, k -> new ArrayList<>()).add(i);
        }

        return index;
    }

    /**
     * Resolve column names, handling conflicts with prefixes
     */
    private List<String> resolveHeaders(
            List<String> leftHeaders,
            List<String> rightHeaders,
            String leftKey,
            String rightKey,
            String leftPrefix,
            String rightPrefix) {

        List<String> result = new ArrayList<>();

        // Add all left columns
        for (String header : leftHeaders) {
            if (header.equals(leftKey)) {
                // Keep join key without prefix
                result.add(header);
            } else {
                result.add(leftPrefix + header);
            }
        }

        // Add right columns (excluding the join key)
        for (String header : rightHeaders) {
            if (header.equals(rightKey)) {
                // Skip right join key (already included from left)
                continue;
            }

            String prefixedName = rightPrefix + header;

            // Check for conflicts even after prefixing
            if (result.contains(prefixedName)) {
                log.warn("Column name conflict after prefixing: {}", prefixedName);
            }

            result.add(prefixedName);
        }

        return result;
    }

    /**
     * Merge two rows, excluding the right join key
     */
    private List<String> mergeRows(List<String> leftRow, List<String> rightRow, int rightKeyIndex) {
        List<String> result = new ArrayList<>(leftRow);

        for (int i = 0; i < rightRow.size(); i++) {
            if (i != rightKeyIndex) {
                result.add(rightRow.get(i));
            }
        }

        return result;
    }

    /**
     * Create a null-filled row for non-matching joins
     */
    private List<String> createNullRow(int size) {
        List<String> row = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            row.add("");
        }
        return row;
    }

    /**
     * Perform inner join - only matching rows
     */
    private List<List<String>> performInnerJoin(
            DataFrame left,
            DataFrame right,
            int leftKeyIndex,
            int rightKeyIndex,
            Map<String, List<Integer>> rightIndex) {

        List<List<String>> result = new ArrayList<>();

        for (List<String> leftRow : left.getRows()) {
            String key = leftRow.get(leftKeyIndex);
            List<Integer> rightIndices = rightIndex.get(key);

            if (rightIndices != null) {
                for (Integer rightIdx : rightIndices) {
                    List<String> rightRow = right.getRows().get(rightIdx);
                    result.add(mergeRows(leftRow, rightRow, rightKeyIndex));
                }
            }
        }

        return result;
    }

    /**
     * Perform left join - all left rows, matching right rows or nulls
     */
    private List<List<String>> performLeftJoin(
            DataFrame left,
            DataFrame right,
            int leftKeyIndex,
            int rightKeyIndex,
            Map<String, List<Integer>> rightIndex) {

        List<List<String>> result = new ArrayList<>();
        int rightColumnCount = right.getColumnCount() - 1; // -1 for join key

        for (List<String> leftRow : left.getRows()) {
            String key = leftRow.get(leftKeyIndex);
            List<Integer> rightIndices = rightIndex.get(key);

            if (rightIndices != null) {
                for (Integer rightIdx : rightIndices) {
                    List<String> rightRow = right.getRows().get(rightIdx);
                    result.add(mergeRows(leftRow, rightRow, rightKeyIndex));
                }
            } else {
                // No match - add nulls for right columns
                List<String> merged = new ArrayList<>(leftRow);
                merged.addAll(createNullRow(rightColumnCount));
                result.add(merged);
            }
        }

        return result;
    }

    /**
     * Perform right join - all right rows, matching left rows or nulls
     */
    private List<List<String>> performRightJoin(
            DataFrame left,
            DataFrame right,
            int leftKeyIndex,
            int rightKeyIndex,
            Map<String, List<Integer>> rightIndex) {

        List<List<String>> result = new ArrayList<>();
        Set<Integer> matchedRightIndices = new HashSet<>();
        int leftColumnCount = left.getColumnCount();

        // First pass: add matching rows
        for (List<String> leftRow : left.getRows()) {
            String key = leftRow.get(leftKeyIndex);
            List<Integer> rightIndices = rightIndex.get(key);

            if (rightIndices != null) {
                for (Integer rightIdx : rightIndices) {
                    matchedRightIndices.add(rightIdx);
                    List<String> rightRow = right.getRows().get(rightIdx);
                    result.add(mergeRows(leftRow, rightRow, rightKeyIndex));
                }
            }
        }

        // Second pass: add unmatched right rows with nulls for left columns
        for (int i = 0; i < right.getRowCount(); i++) {
            if (!matchedRightIndices.contains(i)) {
                List<String> rightRow = right.getRows().get(i);
                List<String> merged = createNullRow(leftColumnCount);
                // Set the join key value
                merged.set(leftKeyIndex, rightRow.get(rightKeyIndex));
                // Add other right columns
                for (int j = 0; j < rightRow.size(); j++) {
                    if (j != rightKeyIndex) {
                        merged.add(rightRow.get(j));
                    }
                }
                result.add(merged);
            }
        }

        return result;
    }

    /**
     * Perform outer join - all rows from both sides
     */
    private List<List<String>> performOuterJoin(
            DataFrame left,
            DataFrame right,
            int leftKeyIndex,
            int rightKeyIndex,
            Map<String, List<Integer>> rightIndex) {

        List<List<String>> result = new ArrayList<>();
        Set<Integer> matchedRightIndices = new HashSet<>();
        int leftColumnCount = left.getColumnCount();
        int rightColumnCount = right.getColumnCount() - 1;

        // Add all left rows (matched or unmatched)
        for (List<String> leftRow : left.getRows()) {
            String key = leftRow.get(leftKeyIndex);
            List<Integer> rightIndices = rightIndex.get(key);

            if (rightIndices != null) {
                for (Integer rightIdx : rightIndices) {
                    matchedRightIndices.add(rightIdx);
                    List<String> rightRow = right.getRows().get(rightIdx);
                    result.add(mergeRows(leftRow, rightRow, rightKeyIndex));
                }
            } else {
                // No match - add nulls for right columns
                List<String> merged = new ArrayList<>(leftRow);
                merged.addAll(createNullRow(rightColumnCount));
                result.add(merged);
            }
        }

        // Add unmatched right rows
        for (int i = 0; i < right.getRowCount(); i++) {
            if (!matchedRightIndices.contains(i)) {
                List<String> rightRow = right.getRows().get(i);
                List<String> merged = createNullRow(leftColumnCount);
                // Set the join key value
                merged.set(leftKeyIndex, rightRow.get(rightKeyIndex));
                // Add other right columns
                for (int j = 0; j < rightRow.size(); j++) {
                    if (j != rightKeyIndex) {
                        merged.add(rightRow.get(j));
                    }
                }
                result.add(merged);
            }
        }

        return result;
    }
}

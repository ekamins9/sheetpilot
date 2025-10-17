package com.sheetpilot.transformation;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class JoinTransformationTest {

    private JoinTransformation transformation;
    private DataFrame leftDataFrame;
    private DataFrame rightDataFrame;

    @BeforeEach
    void setUp() {
        transformation = new JoinTransformation();

        // Left table: users
        List<String> leftHeaders = List.of("user_id", "name", "email");
        List<List<String>> leftRows = List.of(
                List.of("1", "Alice", "alice@example.com"),
                List.of("2", "Bob", "bob@example.com"),
                List.of("3", "Charlie", "charlie@example.com"),
                List.of("4", "Diana", "diana@example.com")
        );
        leftDataFrame = new DataFrame(leftHeaders, leftRows);

        // Right table: orders
        List<String> rightHeaders = List.of("order_id", "user_id", "amount");
        List<List<String>> rightRows = List.of(
                List.of("101", "1", "100.00"),
                List.of("102", "2", "150.00"),
                List.of("103", "1", "200.00"),
                List.of("104", "5", "75.00")  // user_id 5 doesn't exist in left table
        );
        rightDataFrame = new DataFrame(rightHeaders, rightRows);
    }

    @Test
    void testGetType() {
        assertEquals("join", transformation.getType());
    }

    @Test
    void testInnerJoin() throws TransformationException {
        Map<String, Object> config = Map.of(
                "joinType", "inner",
                "leftKey", "user_id",
                "rightKey", "user_id",
                "rightDataFrame", rightDataFrame.toPreviewData()
        );

        DataFrame result = transformation.apply(leftDataFrame, config);

        // Should have 3 rows (Alice has 2 orders, Bob has 1, user_id 5 has no match)
        assertEquals(3, result.getRowCount());

        // Check headers: user_id, name, email, order_id, amount
        assertEquals(5, result.getColumnCount());
        assertTrue(result.getHeaders().contains("user_id"));
        assertTrue(result.getHeaders().contains("order_id"));
        assertTrue(result.getHeaders().contains("amount"));

        // Verify first row
        List<String> firstRow = result.getRows().get(0);
        assertEquals("1", firstRow.get(0)); // user_id
        assertEquals("Alice", firstRow.get(1)); // name
        assertEquals("101", firstRow.get(3)); // order_id
    }

    @Test
    void testLeftJoin() throws TransformationException {
        Map<String, Object> config = Map.of(
                "joinType", "left",
                "leftKey", "user_id",
                "rightKey", "user_id",
                "rightDataFrame", rightDataFrame.toPreviewData()
        );

        DataFrame result = transformation.apply(leftDataFrame, config);

        // Should have 5 rows (3 matched + Charlie with nulls + Diana with nulls)
        assertEquals(5, result.getRowCount());

        // Find Charlie's row (should have empty values for order columns)
        List<String> charlieRow = result.getRows().stream()
                .filter(row -> row.get(1).equals("Charlie"))
                .findFirst()
                .orElseThrow();

        assertEquals("", charlieRow.get(3)); // order_id should be empty
        assertEquals("", charlieRow.get(4)); // amount should be empty
    }

    @Test
    void testRightJoin() throws TransformationException {
        Map<String, Object> config = Map.of(
                "joinType", "right",
                "leftKey", "user_id",
                "rightKey", "user_id",
                "rightDataFrame", rightDataFrame.toPreviewData()
        );

        DataFrame result = transformation.apply(leftDataFrame, config);

        // Should have 4 rows (all orders, even the one with user_id 5)
        assertEquals(4, result.getRowCount());

        // Find order 104 (user_id 5 doesn't exist in left)
        List<String> order104 = result.getRows().stream()
                .filter(row -> row.get(3).equals("104"))
                .findFirst()
                .orElseThrow();

        assertEquals("5", order104.get(0)); // user_id
        assertEquals("", order104.get(1)); // name should be empty
        assertEquals("", order104.get(2)); // email should be empty
    }

    @Test
    void testOuterJoin() throws TransformationException {
        Map<String, Object> config = Map.of(
                "joinType", "outer",
                "leftKey", "user_id",
                "rightKey", "user_id",
                "rightDataFrame", rightDataFrame.toPreviewData()
        );

        DataFrame result = transformation.apply(leftDataFrame, config);

        // Should have 6 rows (3 matched + Charlie + Diana + order 104)
        assertEquals(6, result.getRowCount());

        // Verify we have both unmatched left and right rows
        boolean hasCharlie = result.getRows().stream()
                .anyMatch(row -> row.get(1).equals("Charlie"));
        boolean hasDiana = result.getRows().stream()
                .anyMatch(row -> row.get(1).equals("Diana"));
        boolean hasOrder104 = result.getRows().stream()
                .anyMatch(row -> row.get(3).equals("104"));

        assertTrue(hasCharlie);
        assertTrue(hasDiana);
        assertTrue(hasOrder104);
    }

    @Test
    void testJoinWithPrefixes() throws TransformationException {
        Map<String, Object> config = Map.of(
                "joinType", "inner",
                "leftKey", "user_id",
                "rightKey", "user_id",
                "leftPrefix", "u_",
                "rightPrefix", "o_",
                "rightDataFrame", rightDataFrame.toPreviewData()
        );

        DataFrame result = transformation.apply(leftDataFrame, config);

        // Check headers have prefixes
        assertTrue(result.getHeaders().contains("user_id")); // Join key no prefix
        assertTrue(result.getHeaders().contains("u_name"));
        assertTrue(result.getHeaders().contains("u_email"));
        assertTrue(result.getHeaders().contains("o_order_id"));
        assertTrue(result.getHeaders().contains("o_amount"));
    }

    @Test
    void testJoinWithDuplicateKeys() throws TransformationException {
        // Create data with duplicate join keys
        List<String> leftHeaders = List.of("id", "value");
        List<List<String>> leftRows = List.of(
                List.of("1", "A"),
                List.of("1", "B")
        );
        DataFrame left = new DataFrame(leftHeaders, leftRows);

        List<String> rightHeaders = List.of("id", "amount");
        List<List<String>> rightRows = List.of(
                List.of("1", "100"),
                List.of("1", "200")
        );
        DataFrame right = new DataFrame(rightHeaders, rightRows);

        Map<String, Object> config = Map.of(
                "joinType", "inner",
                "leftKey", "id",
                "rightKey", "id",
                "rightDataFrame", right.toPreviewData()
        );

        DataFrame result = transformation.apply(left, config);

        // Should produce cartesian product: 2 left rows * 2 right rows = 4 result rows
        assertEquals(4, result.getRowCount());
    }

    @Test
    void testJoinInvalidLeftKey() {
        Map<String, Object> config = Map.of(
                "joinType", "inner",
                "leftKey", "invalid_key",
                "rightKey", "user_id",
                "rightDataFrame", rightDataFrame.toPreviewData()
        );

        assertThrows(TransformationException.class, () ->
                transformation.apply(leftDataFrame, config)
        );
    }

    @Test
    void testJoinInvalidRightKey() {
        Map<String, Object> config = Map.of(
                "joinType", "inner",
                "leftKey", "user_id",
                "rightKey", "invalid_key",
                "rightDataFrame", rightDataFrame.toPreviewData()
        );

        assertThrows(TransformationException.class, () ->
                transformation.apply(leftDataFrame, config)
        );
    }

    @Test
    void testValidateConfigMissingJoinType() {
        Map<String, Object> config = Map.of(
                "leftKey", "id",
                "rightKey", "id",
                "rightDataFrame", Map.of()
        );

        assertThrows(TransformationException.class, () ->
                transformation.validateConfig(config)
        );
    }

    @Test
    void testValidateConfigInvalidJoinType() {
        Map<String, Object> config = Map.of(
                "joinType", "invalid",
                "leftKey", "id",
                "rightKey", "id",
                "rightDataFrame", Map.of()
        );

        assertThrows(TransformationException.class, () ->
                transformation.validateConfig(config)
        );
    }

    @Test
    void testValidateConfigMissingRightDataFrame() {
        Map<String, Object> config = Map.of(
                "joinType", "inner",
                "leftKey", "id",
                "rightKey", "id"
        );

        assertThrows(TransformationException.class, () ->
                transformation.validateConfig(config)
        );
    }

    @Test
    void testJoinEmptyLeftTable() throws TransformationException {
        DataFrame emptyLeft = new DataFrame(List.of("id", "name"), List.of());

        Map<String, Object> config = Map.of(
                "joinType", "inner",
                "leftKey", "id",
                "rightKey", "user_id",
                "rightDataFrame", rightDataFrame.toPreviewData()
        );

        DataFrame result = transformation.apply(emptyLeft, config);
        assertEquals(0, result.getRowCount());
    }

    @Test
    void testJoinEmptyRightTable() throws TransformationException {
        DataFrame emptyRight = new DataFrame(List.of("id", "value"), List.of());

        Map<String, Object> config = Map.of(
                "joinType", "inner",
                "leftKey", "user_id",
                "rightKey", "id",
                "rightDataFrame", emptyRight.toPreviewData()
        );

        DataFrame result = transformation.apply(leftDataFrame, config);
        assertEquals(0, result.getRowCount());
    }
}

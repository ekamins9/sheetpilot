package com.sheetpilot.transformation;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class SortTransformationTest {

    private SortTransformation transformation;
    private DataFrame testDataFrame;

    @BeforeEach
    void setUp() {
        transformation = new SortTransformation();

        List<String> headers = List.of("name", "age", "city");
        List<List<String>> rows = List.of(
                List.of("Charlie", "35", "Boston"),
                List.of("Alice", "25", "New York"),
                List.of("Diana", "28", "New York"),
                List.of("Bob", "30", "San Francisco")
        );

        testDataFrame = new DataFrame(headers, rows);
    }

    @Test
    void testGetType() {
        assertEquals("sort", transformation.getType());
    }

    @Test
    void testSortByNameAscending() throws TransformationException {
        Map<String, Object> config = Map.of(
                "column", "name",
                "direction", "ASC"
        );

        DataFrame result = transformation.apply(testDataFrame, config);

        assertEquals(4, result.getRowCount());
        assertEquals("Alice", result.getRows().get(0).get(0));
        assertEquals("Bob", result.getRows().get(1).get(0));
        assertEquals("Charlie", result.getRows().get(2).get(0));
        assertEquals("Diana", result.getRows().get(3).get(0));
    }

    @Test
    void testSortByAgeDescending() throws TransformationException {
        Map<String, Object> config = Map.of(
                "column", "age",
                "direction", "DESC"
        );

        DataFrame result = transformation.apply(testDataFrame, config);

        assertEquals(4, result.getRowCount());
        assertEquals("Charlie", result.getRows().get(0).get(0));
        assertEquals("Bob", result.getRows().get(1).get(0));
        assertEquals("Diana", result.getRows().get(2).get(0));
        assertEquals("Alice", result.getRows().get(3).get(0));
    }

    @Test
    void testSortMultipleColumns() throws TransformationException {
        Map<String, Object> config = Map.of(
                "columns", List.of("city", "name"),
                "directions", List.of("ASC", "ASC")
        );

        DataFrame result = transformation.apply(testDataFrame, config);

        assertEquals(4, result.getRowCount());
        assertEquals("Charlie", result.getRows().get(0).get(0)); // Boston
        assertEquals("Alice", result.getRows().get(1).get(0)); // New York, Alice
        assertEquals("Diana", result.getRows().get(2).get(0)); // New York, Diana
        assertEquals("Bob", result.getRows().get(3).get(0)); // San Francisco
    }

    @Test
    void testSortInvalidColumn() {
        Map<String, Object> config = Map.of(
                "column", "invalid",
                "direction", "ASC"
        );

        assertThrows(TransformationException.class, () ->
                transformation.apply(testDataFrame, config)
        );
    }

    @Test
    void testValidateConfigMissingColumn() {
        Map<String, Object> config = Map.of(
                "direction", "ASC"
        );

        assertThrows(TransformationException.class, () ->
                transformation.validateConfig(config)
        );
    }

    @Test
    void testValidateConfigInvalidDirection() {
        Map<String, Object> config = Map.of(
                "column", "name",
                "direction", "INVALID"
        );

        assertThrows(TransformationException.class, () ->
                transformation.validateConfig(config)
        );
    }
}

package com.sheetpilot.transformation;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class FilterTransformationTest {

    private FilterTransformation transformation;
    private DataFrame testDataFrame;

    @BeforeEach
    void setUp() {
        transformation = new FilterTransformation();

        // Create test data
        List<String> headers = List.of("name", "age", "city");
        List<List<String>> rows = List.of(
                List.of("Alice", "25", "New York"),
                List.of("Bob", "30", "San Francisco"),
                List.of("Charlie", "35", "Boston"),
                List.of("Diana", "28", "New York")
        );

        testDataFrame = new DataFrame(headers, rows);
    }

    @Test
    void testGetType() {
        assertEquals("filter", transformation.getType());
    }

    @Test
    void testFilterEquals() throws TransformationException {
        Map<String, Object> config = Map.of(
                "column", "city",
                "operator", "=",
                "value", "New York"
        );

        DataFrame result = transformation.apply(testDataFrame, config);

        assertEquals(2, result.getRowCount());
        assertEquals("Alice", result.getRows().get(0).get(0));
        assertEquals("Diana", result.getRows().get(1).get(0));
    }

    @Test
    void testFilterGreaterThan() throws TransformationException {
        Map<String, Object> config = Map.of(
                "column", "age",
                "operator", ">",
                "value", "28"
        );

        DataFrame result = transformation.apply(testDataFrame, config);

        assertEquals(2, result.getRowCount());
        assertEquals("Bob", result.getRows().get(0).get(0));
        assertEquals("Charlie", result.getRows().get(1).get(0));
    }

    @Test
    void testFilterContains() throws TransformationException {
        Map<String, Object> config = Map.of(
                "column", "name",
                "operator", "contains",
                "value", "li"
        );

        DataFrame result = transformation.apply(testDataFrame, config);

        assertEquals(2, result.getRowCount());
        assertEquals("Alice", result.getRows().get(0).get(0));
        assertEquals("Charlie", result.getRows().get(1).get(0));
    }

    @Test
    void testFilterInvalidColumn() {
        Map<String, Object> config = Map.of(
                "column", "invalid",
                "operator", "=",
                "value", "test"
        );

        assertThrows(TransformationException.class, () ->
                transformation.apply(testDataFrame, config)
        );
    }

    @Test
    void testValidateConfigMissingColumn() {
        Map<String, Object> config = Map.of(
                "operator", "=",
                "value", "test"
        );

        assertThrows(TransformationException.class, () ->
                transformation.validateConfig(config)
        );
    }

    @Test
    void testValidateConfigInvalidOperator() {
        Map<String, Object> config = Map.of(
                "column", "name",
                "operator", "invalid",
                "value", "test"
        );

        assertThrows(TransformationException.class, () ->
                transformation.validateConfig(config)
        );
    }
}

package com.sheetpilot.transformation;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class SelectTransformationTest {

    private SelectTransformation transformation;
    private DataFrame testDataFrame;

    @BeforeEach
    void setUp() {
        transformation = new SelectTransformation();

        List<String> headers = List.of("name", "age", "city", "email");
        List<List<String>> rows = List.of(
                List.of("Alice", "25", "New York", "alice@example.com"),
                List.of("Bob", "30", "San Francisco", "bob@example.com")
        );

        testDataFrame = new DataFrame(headers, rows);
    }

    @Test
    void testGetType() {
        assertEquals("select", transformation.getType());
    }

    @Test
    void testSelectColumns() throws TransformationException {
        Map<String, Object> config = Map.of(
                "columns", List.of("name", "email")
        );

        DataFrame result = transformation.apply(testDataFrame, config);

        assertEquals(2, result.getColumnCount());
        assertEquals(2, result.getRowCount());
        assertEquals(List.of("name", "email"), result.getHeaders());
        assertEquals("Alice", result.getRows().get(0).get(0));
        assertEquals("alice@example.com", result.getRows().get(0).get(1));
    }

    @Test
    void testSelectReorderColumns() throws TransformationException {
        Map<String, Object> config = Map.of(
                "columns", List.of("email", "name", "city")
        );

        DataFrame result = transformation.apply(testDataFrame, config);

        assertEquals(3, result.getColumnCount());
        assertEquals(List.of("email", "name", "city"), result.getHeaders());
        assertEquals("alice@example.com", result.getRows().get(0).get(0));
        assertEquals("Alice", result.getRows().get(0).get(1));
        assertEquals("New York", result.getRows().get(0).get(2));
    }

    @Test
    void testSelectInvalidColumn() {
        Map<String, Object> config = Map.of(
                "columns", List.of("name", "invalid")
        );

        assertThrows(TransformationException.class, () ->
                transformation.apply(testDataFrame, config)
        );
    }

    @Test
    void testValidateConfigMissingColumns() {
        Map<String, Object> config = Map.of();

        assertThrows(TransformationException.class, () ->
                transformation.validateConfig(config)
        );
    }

    @Test
    void testValidateConfigEmptyColumns() {
        Map<String, Object> config = Map.of(
                "columns", List.of()
        );

        assertThrows(TransformationException.class, () ->
                transformation.validateConfig(config)
        );
    }
}

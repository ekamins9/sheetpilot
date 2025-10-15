package com.sheetpilot.transformation;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class RenameTransformationTest {

    private RenameTransformation transformation;
    private DataFrame testDataFrame;

    @BeforeEach
    void setUp() {
        transformation = new RenameTransformation();

        List<String> headers = List.of("first_name", "last_name", "email_address");
        List<List<String>> rows = List.of(
                List.of("Alice", "Smith", "alice@example.com"),
                List.of("Bob", "Jones", "bob@example.com")
        );

        testDataFrame = new DataFrame(headers, rows);
    }

    @Test
    void testGetType() {
        assertEquals("rename", transformation.getType());
    }

    @Test
    void testRenameColumns() throws TransformationException {
        Map<String, Object> config = Map.of(
                "mapping", Map.of(
                        "first_name", "name",
                        "email_address", "email"
                )
        );

        DataFrame result = transformation.apply(testDataFrame, config);

        assertEquals(3, result.getColumnCount());
        assertEquals(List.of("name", "last_name", "email"), result.getHeaders());
        assertEquals("Alice", result.getRows().get(0).get(0));
        assertEquals("Smith", result.getRows().get(0).get(1));
        assertEquals("alice@example.com", result.getRows().get(0).get(2));
    }

    @Test
    void testRenameInvalidColumn() {
        Map<String, Object> config = Map.of(
                "mapping", Map.of("invalid", "new_name")
        );

        assertThrows(TransformationException.class, () ->
                transformation.apply(testDataFrame, config)
        );
    }

    @Test
    void testRenameDuplicateNewNames() {
        Map<String, Object> config = Map.of(
                "mapping", Map.of(
                        "first_name", "name",
                        "last_name", "name"
                )
        );

        assertThrows(TransformationException.class, () ->
                transformation.apply(testDataFrame, config)
        );
    }

    @Test
    void testValidateConfigMissingMapping() {
        Map<String, Object> config = Map.of();

        assertThrows(TransformationException.class, () ->
                transformation.validateConfig(config)
        );
    }

    @Test
    void testValidateConfigEmptyMapping() {
        Map<String, Object> config = Map.of(
                "mapping", Map.of()
        );

        assertThrows(TransformationException.class, () ->
                transformation.validateConfig(config)
        );
    }
}

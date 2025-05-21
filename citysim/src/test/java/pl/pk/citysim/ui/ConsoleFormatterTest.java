package pl.pk.citysim.ui;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the ConsoleFormatter utility class.
 */
public class ConsoleFormatterTest {

    @BeforeEach
    void setUp() {
        // Disable colors for consistent testing
        ConsoleFormatter.setColorsEnabled(false);
    }

    @Test
    void testCreateHeader() {
        String header = ConsoleFormatter.createHeader("TEST HEADER");
        assertNotNull(header);
        assertTrue(header.contains("TEST HEADER"));
        assertTrue(header.contains("="));
    }

    @Test
    void testCreateDivider() {
        String divider = ConsoleFormatter.createDivider();
        assertNotNull(divider);
        assertTrue(divider.contains("-"));
    }

    @Test
    void testCreateTable() {
        String[] headers = {"Name", "Value"};
        List<String[]> rows = new ArrayList<>();
        rows.add(new String[]{"Row1", "Value1"});
        rows.add(new String[]{"Row2", "Value2"});

        String table = ConsoleFormatter.createTable(headers, rows);
        assertNotNull(table);
        assertTrue(table.contains("Name"));
        assertTrue(table.contains("Value"));
        assertTrue(table.contains("Row1"));
        assertTrue(table.contains("Value1"));
        assertTrue(table.contains("Row2"));
        assertTrue(table.contains("Value2"));
    }

    @Test
    void testCreateTableWithEmptyInput() {
        // Test with null headers
        String table1 = ConsoleFormatter.createTable(null, new ArrayList<>());
        assertEquals("", table1);

        // Test with empty headers
        String table2 = ConsoleFormatter.createTable(new String[]{}, new ArrayList<>());
        assertEquals("", table2);

        // Test with null rows
        String table3 = ConsoleFormatter.createTable(new String[]{"Header"}, null);
        assertEquals("", table3);
    }

    @Test
    void testCreateKeyValueTable() {
        Map<String, String> data = new HashMap<>();
        data.put("Key1", "Value1");
        data.put("Key2", "Value2");

        String table = ConsoleFormatter.createKeyValueTable("TEST TABLE", data);
        assertNotNull(table);
        assertTrue(table.contains("TEST TABLE"));
        assertTrue(table.contains("Key1:"));
        assertTrue(table.contains("Value1"));
        assertTrue(table.contains("Key2:"));
        assertTrue(table.contains("Value2"));
    }

    @Test
    void testCreateKeyValueTableWithEmptyInput() {
        // Test with null data
        String table1 = ConsoleFormatter.createKeyValueTable("TEST", null);
        assertEquals("", table1);

        // Test with empty data
        String table2 = ConsoleFormatter.createKeyValueTable("TEST", new HashMap<>());
        assertEquals("", table2);
    }

    @Test
    void testHighlightWarning() {
        String original = "warning message";
        String highlighted = ConsoleFormatter.highlightWarning(original);
        assertEquals("WARNING MESSAGE", highlighted);

        // Test with colors enabled
        ConsoleFormatter.setColorsEnabled(true);
        String colorHighlighted = ConsoleFormatter.highlightWarning(original);
        assertTrue(colorHighlighted.toUpperCase().contains("WARNING MESSAGE"));
        ConsoleFormatter.setColorsEnabled(false);
    }

    @Test
    void testHighlightError() {
        String original = "error message";
        String highlighted = ConsoleFormatter.highlightError(original);
        assertEquals("ERROR MESSAGE", highlighted);

        // Test with colors enabled
        ConsoleFormatter.setColorsEnabled(true);
        String colorHighlighted = ConsoleFormatter.highlightError(original);
        assertTrue(colorHighlighted.toUpperCase().contains("ERROR MESSAGE"));
        ConsoleFormatter.setColorsEnabled(false);
    }

    @Test
    void testHighlightSuccess() {
        String original = "success message";
        String highlighted = ConsoleFormatter.highlightSuccess(original);
        assertEquals(original, highlighted);

        // Test with colors enabled
        ConsoleFormatter.setColorsEnabled(true);
        String colorHighlighted = ConsoleFormatter.highlightSuccess(original);
        assertTrue(colorHighlighted.contains(original));
        ConsoleFormatter.setColorsEnabled(false);
    }

    @Test
    void testHighlightInfo() {
        String original = "info message";
        String highlighted = ConsoleFormatter.highlightInfo(original);
        assertEquals(original, highlighted);

        // Test with colors enabled
        ConsoleFormatter.setColorsEnabled(true);
        String colorHighlighted = ConsoleFormatter.highlightInfo(original);
        assertTrue(colorHighlighted.contains(original));
        ConsoleFormatter.setColorsEnabled(false);
    }

    @Test
    void testFormatLogEntry() {
        // Test with different event types
        String fireEvent = "Day 10: FIRE! A building caught fire.";
        String warningEvent = "Day 15: WARNING - Not enough schools!";
        String grantEvent = "Day 20: GRANT! The city received money.";
        String normalEvent = "Day 25: Population increased to 100 families.";

        String formattedFire = ConsoleFormatter.formatLogEntry(fireEvent);
        String formattedWarning = ConsoleFormatter.formatLogEntry(warningEvent);
        String formattedGrant = ConsoleFormatter.formatLogEntry(grantEvent);
        String formattedNormal = ConsoleFormatter.formatLogEntry(normalEvent);

        // Without colors, fire and critical events should be uppercase
        assertTrue(formattedFire.equals(fireEvent.toUpperCase()));
        // Warnings should be uppercase
        assertTrue(formattedWarning.equals(warningEvent.toUpperCase()));
        // Grant events should be unchanged
        assertEquals(grantEvent, formattedGrant);
        // Normal events should be unchanged
        assertEquals(normalEvent, formattedNormal);
    }

    @Test
    void testColorsToggle() {
        // Test enabling colors
        ConsoleFormatter.setColorsEnabled(true);
        assertTrue(ConsoleFormatter.areColorsEnabled());

        // Test disabling colors
        ConsoleFormatter.setColorsEnabled(false);
        assertFalse(ConsoleFormatter.areColorsEnabled());
    }
}
package pl.pk.citysim.ui;

import java.util.List;
import java.util.Map;

/**
 * Utility class for formatting console output in the city simulation game.
 * Provides methods for creating tables, section headers, and highlighting text.
 */
public class ConsoleFormatter {
    // ASCII box drawing characters for tables and dividers
    private static final String HORIZONTAL_LINE = "─";
    private static final String VERTICAL_LINE = "│";
    private static final String TOP_LEFT = "┌";
    private static final String TOP_RIGHT = "┐";
    private static final String BOTTOM_LEFT = "└";
    private static final String BOTTOM_RIGHT = "┘";
    private static final String T_DOWN = "┬";
    private static final String T_UP = "┴";
    private static final String T_RIGHT = "├";
    private static final String T_LEFT = "┤";
    private static final String CROSS = "┼";

    // ANSI color codes (optional)
    private static final String ANSI_RESET = "\u001B[0m";
    private static final String ANSI_RED = "\u001B[31m";
    private static final String ANSI_GREEN = "\u001B[32m";
    private static final String ANSI_YELLOW = "\u001B[33m";
    private static final String ANSI_BLUE = "\u001B[34m";
    private static final String ANSI_BOLD = "\u001B[1m";

    // Flag to enable/disable ANSI colors
    private static boolean useColors = false;

    /**
     * Creates a section header with a title.
     *
     * @param title The title of the section
     * @return A formatted section header
     */
    public static String createHeader(String title) {
        StringBuilder sb = new StringBuilder();
        String paddedTitle = " " + title + " ";
        int lineLength = 60;
        int titleLength = paddedTitle.length();
        int sideLength = (lineLength - titleLength) / 2;
        
        sb.append("\n");
        sb.append("=".repeat(sideLength));
        sb.append(useColors ? ANSI_BOLD : "");
        sb.append(paddedTitle);
        sb.append(useColors ? ANSI_RESET : "");
        sb.append("=".repeat(lineLength - sideLength - titleLength));
        sb.append("\n");
        
        return sb.toString();
    }

    /**
     * Creates a simple horizontal divider.
     *
     * @return A horizontal divider string
     */
    public static String createDivider() {
        return "-".repeat(60) + "\n";
    }

    /**
     * Creates a table from a list of rows.
     *
     * @param headers The table headers
     * @param rows The table data rows
     * @return A formatted table string
     */
    public static String createTable(String[] headers, List<String[]> rows) {
        if (headers == null || rows == null || headers.length == 0) {
            return "";
        }

        // Calculate column widths
        int[] columnWidths = new int[headers.length];
        for (int i = 0; i < headers.length; i++) {
            columnWidths[i] = headers[i].length();
        }

        for (String[] row : rows) {
            for (int i = 0; i < Math.min(row.length, headers.length); i++) {
                if (row[i] != null && row[i].length() > columnWidths[i]) {
                    columnWidths[i] = row[i].length();
                }
            }
        }

        StringBuilder sb = new StringBuilder();

        // Create top border
        sb.append(TOP_LEFT);
        for (int i = 0; i < headers.length; i++) {
            sb.append(HORIZONTAL_LINE.repeat(columnWidths[i] + 2));
            sb.append(i < headers.length - 1 ? T_DOWN : TOP_RIGHT);
        }
        sb.append("\n");

        // Create header row
        sb.append(VERTICAL_LINE);
        for (int i = 0; i < headers.length; i++) {
            sb.append(" ");
            sb.append(useColors ? ANSI_BOLD : "");
            sb.append(padRight(headers[i], columnWidths[i]));
            sb.append(useColors ? ANSI_RESET : "");
            sb.append(" ");
            sb.append(VERTICAL_LINE);
        }
        sb.append("\n");

        // Create header-data separator
        sb.append(T_RIGHT);
        for (int i = 0; i < headers.length; i++) {
            sb.append(HORIZONTAL_LINE.repeat(columnWidths[i] + 2));
            sb.append(i < headers.length - 1 ? CROSS : T_LEFT);
        }
        sb.append("\n");

        // Create data rows
        for (int rowIdx = 0; rowIdx < rows.size(); rowIdx++) {
            String[] row = rows.get(rowIdx);
            sb.append(VERTICAL_LINE);
            for (int i = 0; i < headers.length; i++) {
                sb.append(" ");
                String cell = i < row.length ? row[i] : "";
                sb.append(padRight(cell != null ? cell : "", columnWidths[i]));
                sb.append(" ");
                sb.append(VERTICAL_LINE);
            }
            sb.append("\n");

            // Add row separator except after the last row
            if (rowIdx < rows.size() - 1) {
                sb.append(T_RIGHT);
                for (int i = 0; i < headers.length; i++) {
                    sb.append(HORIZONTAL_LINE.repeat(columnWidths[i] + 2));
                    sb.append(i < headers.length - 1 ? CROSS : T_LEFT);
                }
                sb.append("\n");
            }
        }

        // Create bottom border
        sb.append(BOTTOM_LEFT);
        for (int i = 0; i < headers.length; i++) {
            sb.append(HORIZONTAL_LINE.repeat(columnWidths[i] + 2));
            sb.append(i < headers.length - 1 ? T_UP : BOTTOM_RIGHT);
        }
        sb.append("\n");

        return sb.toString();
    }

    /**
     * Formats a key-value map as a two-column table.
     *
     * @param title The title for the table
     * @param data The map of key-value pairs
     * @return A formatted table string
     */
    public static String createKeyValueTable(String title, Map<String, String> data) {
        if (data == null || data.isEmpty()) {
            return "";
        }

        StringBuilder sb = new StringBuilder();
        sb.append(createHeader(title));

        // Find the longest key for proper alignment
        int maxKeyLength = data.keySet().stream()
                .mapToInt(String::length)
                .max()
                .orElse(10);

        // Format each key-value pair
        for (Map.Entry<String, String> entry : data.entrySet()) {
            sb.append(padRight(entry.getKey() + ":", maxKeyLength + 1))
              .append(" ")
              .append(entry.getValue())
              .append("\n");
        }

        return sb.toString();
    }

    /**
     * Highlights text as a warning (uppercase and with color if enabled).
     *
     * @param text The text to highlight
     * @return The highlighted text
     */
    public static String highlightWarning(String text) {
        if (useColors) {
            return ANSI_YELLOW + text.toUpperCase() + ANSI_RESET;
        } else {
            return text.toUpperCase();
        }
    }

    /**
     * Highlights text as an error (uppercase and with color if enabled).
     *
     * @param text The text to highlight
     * @return The highlighted text
     */
    public static String highlightError(String text) {
        if (useColors) {
            return ANSI_RED + text.toUpperCase() + ANSI_RESET;
        } else {
            return text.toUpperCase();
        }
    }

    /**
     * Highlights text as a success message (with color if enabled).
     *
     * @param text The text to highlight
     * @return The highlighted text
     */
    public static String highlightSuccess(String text) {
        if (useColors) {
            return ANSI_GREEN + text + ANSI_RESET;
        } else {
            return text;
        }
    }

    /**
     * Highlights text as important information (with color if enabled).
     *
     * @param text The text to highlight
     * @return The highlighted text
     */
    public static String highlightInfo(String text) {
        if (useColors) {
            return ANSI_BLUE + text + ANSI_RESET;
        } else {
            return text;
        }
    }

    /**
     * Formats an event log entry with optional highlighting based on content.
     *
     * @param logEntry The log entry to format
     * @return The formatted log entry
     */
    public static String formatLogEntry(String logEntry) {
        // Check for event types and highlight accordingly
        if (logEntry.contains("FIRE") || logEntry.contains("EPIDEMIC") || 
            logEntry.contains("ECONOMIC CRISIS") || logEntry.contains("CRITICAL")) {
            return highlightError(logEntry);
        } else if (logEntry.contains("WARNING")) {
            return highlightWarning(logEntry);
        } else if (logEntry.contains("GRANT")) {
            return highlightSuccess(logEntry);
        } else {
            return logEntry;
        }
    }

    /**
     * Enables or disables ANSI color codes in the output.
     *
     * @param enabled Whether to enable colors
     */
    public static void setColorsEnabled(boolean enabled) {
        useColors = enabled;
    }

    /**
     * Checks if ANSI colors are enabled.
     *
     * @return true if colors are enabled, false otherwise
     */
    public static boolean areColorsEnabled() {
        return useColors;
    }

    /**
     * Pads a string on the right with spaces to reach the specified length.
     *
     * @param s The string to pad
     * @param length The desired length
     * @return The padded string
     */
    private static String padRight(String s, int length) {
        if (s == null) {
            s = "";
        }
        return s + " ".repeat(Math.max(0, length - s.length()));
    }
}
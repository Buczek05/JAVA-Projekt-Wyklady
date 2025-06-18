package pl.pk.citysim.ui;

import java.util.List;
import java.util.Map;

public class ConsoleFormatter {
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
    private static final String ANSI_RESET = "\u001B[0m";
    private static final String ANSI_RED = "\u001B[31m";
    private static final String ANSI_GREEN = "\u001B[32m";
    private static final String ANSI_YELLOW = "\u001B[33m";
    private static final String ANSI_BLUE = "\u001B[34m";
    private static final String ANSI_BOLD = "\u001B[1m";
    private static boolean useColors = true;

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

    public static String createDivider() {
        return "-".repeat(60) + "\n";
    }

    public static String createTable(String[] headers, List<String[]> rows) {
        if (headers == null || rows == null || headers.length == 0) {
            return "";
        }
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
        sb.append(TOP_LEFT);
        for (int i = 0; i < headers.length; i++) {
            sb.append(HORIZONTAL_LINE.repeat(columnWidths[i] + 2));
            sb.append(i < headers.length - 1 ? T_DOWN : TOP_RIGHT);
        }
        sb.append("\n");
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
        sb.append(T_RIGHT);
        for (int i = 0; i < headers.length; i++) {
            sb.append(HORIZONTAL_LINE.repeat(columnWidths[i] + 2));
            sb.append(i < headers.length - 1 ? CROSS : T_LEFT);
        }
        sb.append("\n");
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
            if (rowIdx < rows.size() - 1) {
                sb.append(T_RIGHT);
                for (int i = 0; i < headers.length; i++) {
                    sb.append(HORIZONTAL_LINE.repeat(columnWidths[i] + 2));
                    sb.append(i < headers.length - 1 ? CROSS : T_LEFT);
                }
                sb.append("\n");
            }
        }
        sb.append(BOTTOM_LEFT);
        for (int i = 0; i < headers.length; i++) {
            sb.append(HORIZONTAL_LINE.repeat(columnWidths[i] + 2));
            sb.append(i < headers.length - 1 ? T_UP : BOTTOM_RIGHT);
        }
        sb.append("\n");

        return sb.toString();
    }

    public static String createKeyValueTable(String title, Map<String, String> data) {
        if (data == null || data.isEmpty()) {
            return "";
        }

        StringBuilder sb = new StringBuilder();
        sb.append(createHeader(title));
        int maxKeyLength = data.keySet().stream()
                .mapToInt(String::length)
                .max()
                .orElse(10);
        for (Map.Entry<String, String> entry : data.entrySet()) {
            sb.append(padRight(entry.getKey() + ":", maxKeyLength + 1))
              .append(" ")
              .append(entry.getValue())
              .append("\n");
        }

        return sb.toString();
    }

    public static String highlightWarning(String text) {
        if (useColors) {
            return ANSI_YELLOW + text.toUpperCase() + ANSI_RESET;
        } else {
            return text.toUpperCase();
        }
    }

    public static String highlightError(String text) {
        if (useColors) {
            return ANSI_RED + text.toUpperCase() + ANSI_RESET;
        } else {
            return text.toUpperCase();
        }
    }

    public static String highlightSuccess(String text) {
        if (useColors) {
            return ANSI_GREEN + text + ANSI_RESET;
        } else {
            return text;
        }
    }

    public static String highlightInfo(String text) {
        if (useColors) {
            return ANSI_BLUE + text + ANSI_RESET;
        } else {
            return text;
        }
    }

    public static String formatLogEntry(String logEntry) {
        String upperLogEntry = logEntry.toUpperCase();
        if (upperLogEntry.contains("FIRE") || upperLogEntry.contains("EPIDEMIC") || 
            upperLogEntry.contains("ECONOMIC CRISIS") || upperLogEntry.contains("CRITICAL")) {
            return highlightError(logEntry);
        } else if (upperLogEntry.contains("WARNING")) {
            return highlightWarning(logEntry);
        } else if (upperLogEntry.contains("GRANT")) {
            return highlightSuccess(logEntry);
        } else {
            return logEntry;
        }
    }

    public static void setColorsEnabled(boolean enabled) {
        useColors = enabled;
    }

    public static boolean areColorsEnabled() {
        return useColors;
    }

    private static String padRight(String s, int length) {
        if (s == null) {
            s = "";
        }
        return s + " ".repeat(Math.max(0, length - s.length()));
    }
}

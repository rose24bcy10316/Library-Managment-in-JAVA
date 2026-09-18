import java.util.ArrayList;
import java.util.List;

/**
 * CsvUtil.java
 * Small helper for safely writing/reading simple CSV rows that may
 * contain commas inside fields (fields are wrapped in double quotes
 * when needed, following the common CSV convention).
 */
public final class CsvUtil {

    private CsvUtil() { }

    public static String escape(String field) {
        if (field == null) return "";
        boolean needsQuoting = field.contains(",") || field.contains("\"") || field.contains("\n");
        String out = field.replace("\"", "\"\"");
        return needsQuoting ? "\"" + out + "\"" : out;
    }

    /** Splits one CSV line into fields, honoring double-quoted fields. */
    public static String[] splitCsvLine(String line) {
        List<String> fields = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inQuotes = false;

        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (inQuotes) {
                if (c == '"') {
                    if (i + 1 < line.length() && line.charAt(i + 1) == '"') {
                        current.append('"');
                        i++;
                    } else {
                        inQuotes = false;
                    }
                } else {
                    current.append(c);
                }
            } else {
                if (c == '"') {
                    inQuotes = true;
                } else if (c == ',') {
                    fields.add(current.toString());
                    current.setLength(0);
                } else {
                    current.append(c);
                }
            }
        }
        fields.add(current.toString());
        return fields.toArray(new String[0]);
    }
}

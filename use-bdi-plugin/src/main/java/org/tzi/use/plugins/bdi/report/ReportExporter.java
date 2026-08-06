package org.tzi.use.plugins.bdi.report;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.format.DateTimeFormatter;

public final class ReportExporter {

    private ReportExporter() { }

    public static void exportJson(ReportData data, Path output) throws IOException {
        StringBuilder sb = new StringBuilder();
        sb.append('{');
        appendField(sb, "projectName", data.projectName());
        sb.append(',');
        appendField(sb, "pluginVersion", data.pluginVersion());
        sb.append(',');
        appendField(sb, "useVersion", data.useVersion());
        sb.append(',');
        appendField(sb, "timestamp", DateTimeFormatter.ISO_INSTANT.format(data.timestamp()));
        sb.append(',');
        appendNumberField(sb, "issuesCount", data.issuesCount());
        sb.append(',');
        appendNumberField(sb, "mappingsCount", data.mappingsCount());
        sb.append(',');
        appendField(sb, "notes", data.notes());
        sb.append('}');

        byte[] bytes = sb.toString().getBytes(StandardCharsets.UTF_8);
        Files.createDirectories(output.getParent());
        Files.write(output, bytes);
    }

    private static void appendField(StringBuilder sb, String name, String value) {
        sb.append('"').append(escape(name)).append('"').append(':');
        if (value == null) {
            sb.append("null");
            return;
        }
        sb.append('"').append(escape(value)).append('"');
    }

    private static void appendNumberField(StringBuilder sb, String name, int value) {
        sb.append('"').append(escape(name)).append('"').append(':').append(value);
    }

    private static String escape(String s) {
        StringBuilder out = new StringBuilder();
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            switch (c) {
                case '"': out.append("\\\""); break;
                case '\\': out.append("\\\\"); break;
                case '\n': out.append("\\n"); break;
                case '\r': out.append("\\r"); break;
                case '\t': out.append("\\t"); break;
                default:
                    if (c < 0x20) {
                        out.append(String.format("\\u%04x", (int)c));
                    } else {
                        out.append(c);
                    }
            }
        }
        return out.toString();
    }
}

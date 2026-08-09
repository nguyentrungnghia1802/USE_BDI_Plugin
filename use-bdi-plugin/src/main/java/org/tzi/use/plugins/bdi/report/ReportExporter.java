package org.tzi.use.plugins.bdi.report;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

import org.tzi.use.plugins.bdi.model.ir.SourceSpan;
import org.tzi.use.plugins.bdi.validation.ConsistencyIssue;

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
        appendField(sb, "modelHash", data.modelHash().orElse(null));
        sb.append(',');
        appendField(sb, "mappingHash", data.mappingHash().orElse(null));
        sb.append(',');
        appendField(sb, "notes", data.notes());
        sb.append(',');
        appendIssues(sb, data);
        sb.append('}');

        byte[] bytes = sb.toString().getBytes(StandardCharsets.UTF_8);
        Files.createDirectories(output.getParent());
        Files.write(output, bytes);
    }

    private static void appendIssues(StringBuilder sb, ReportData data) {
        sb.append("\"issues\":[");
        for (int index = 0; index < data.issues().size(); index++) {
            if (index > 0) {
                sb.append(',');
            }
            ConsistencyIssue issue = data.issues().get(index);
            sb.append('{');
            appendField(sb, "ruleId", issue.ruleId());
            sb.append(',');
            appendField(sb, "severity", issue.severity().name());
            sb.append(',');
            appendField(sb, "status", issue.status().name());
            sb.append(',');
            appendField(sb, "certainty", issue.certainty().name());
            sb.append(',');
            appendField(sb, "message", issue.message());
            sb.append(',');
            appendField(sb, "source", source(issue.sourceSpan()));
            sb.append(',');
            appendField(sb, "evidence", String.join("; ", issue.evidence()));
            sb.append('}');
        }
        sb.append(']');
    }

    private static String source(Optional<SourceSpan> span) {
        return span.map(value -> value.source() + ":" + value.beginLine() + ":" + value.beginColumn())
                .orElse(null);
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

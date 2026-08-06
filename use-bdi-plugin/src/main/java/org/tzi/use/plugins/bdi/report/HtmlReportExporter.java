package org.tzi.use.plugins.bdi.report;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.format.DateTimeFormatter;

public final class HtmlReportExporter {

    private HtmlReportExporter() { }

    public static void exportHtml(ReportData data, Path output) throws IOException {
        StringBuilder sb = new StringBuilder();
        sb.append("<!doctype html>\n<html lang=\"en\">\n<head>\n");
        sb.append("<meta charset=\"utf-8\">\n<title>BDI Report</title>\n</head>\n<body>\n");
        sb.append("<h1>BDI Report</h1>\n");
        sb.append("<table border=\"1\">\n");
        appendRow(sb, "Project", data.projectName());
        appendRow(sb, "Plugin Version", data.pluginVersion());
        appendRow(sb, "USE Version", data.useVersion());
        appendRow(sb, "Timestamp", DateTimeFormatter.ISO_INSTANT.format(data.timestamp()));
        appendRow(sb, "Issues Count", Integer.toString(data.issuesCount()));
        appendRow(sb, "Mappings Count", Integer.toString(data.mappingsCount()));
        appendRow(sb, "Notes", data.notes());
        sb.append("</table>\n</body>\n</html>\n");

        byte[] bytes = sb.toString().getBytes(StandardCharsets.UTF_8);
        Files.createDirectories(output.getParent());
        Files.write(output, bytes);
    }

    private static void appendRow(StringBuilder sb, String name, String value) {
        sb.append("<tr><th style=\"text-align:left;padding:6px;\">")
          .append(escapeHtml(name))
          .append("</th><td style=\"padding:6px;\">")
          .append(value == null ? "" : escapeHtml(value))
          .append("</td></tr>\n");
    }

    private static String escapeHtml(String s) {
        if (s == null) return "";
        StringBuilder out = new StringBuilder(Math.max(16, s.length()));
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            switch (c) {
                case '<': out.append("&lt;"); break;
                case '>': out.append("&gt;"); break;
                case '&': out.append("&amp;"); break;
                case '"': out.append("&quot;"); break;
                case '\'': out.append("&#39;"); break;
                default: out.append(c);
            }
        }
        return out.toString();
    }
}

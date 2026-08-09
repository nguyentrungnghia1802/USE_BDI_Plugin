package org.tzi.use.plugins.bdi.report;

/** Supported deterministic report serializations. */
public enum ReportFormat {
    JSON("json", "JSON report (*.json)"),
    HTML("html", "HTML report (*.html)");

    private final String extension;
    private final String description;

    ReportFormat(String extension, String description) {
        this.extension = extension;
        this.description = description;
    }

    public String extension() {
        return extension;
    }

    public String description() {
        return description;
    }

    public static ReportFormat fromFilename(String filename) {
        String normalized = filename == null ? "" : filename.toLowerCase(java.util.Locale.ROOT);
        return normalized.endsWith(".html") || normalized.endsWith(".htm") ? HTML : JSON;
    }
}

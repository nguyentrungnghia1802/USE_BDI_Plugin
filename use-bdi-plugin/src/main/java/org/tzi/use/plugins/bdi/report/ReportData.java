package org.tzi.use.plugins.bdi.report;

import java.time.Instant;

public final class ReportData {
    private final String projectName;
    private final String pluginVersion;
    private final String useVersion;
    private final Instant timestamp;
    private final int issuesCount;
    private final int mappingsCount;
    private final String notes;

    public ReportData(String projectName,
                      String pluginVersion,
                      String useVersion,
                      Instant timestamp,
                      int issuesCount,
                      int mappingsCount,
                      String notes) {
        this.projectName = projectName;
        this.pluginVersion = pluginVersion;
        this.useVersion = useVersion;
        this.timestamp = timestamp;
        this.issuesCount = issuesCount;
        this.mappingsCount = mappingsCount;
        this.notes = notes;
    }

    public String projectName() { return projectName; }
    public String pluginVersion() { return pluginVersion; }
    public String useVersion() { return useVersion; }
    public Instant timestamp() { return timestamp; }
    public int issuesCount() { return issuesCount; }
    public int mappingsCount() { return mappingsCount; }
    public String notes() { return notes; }
}

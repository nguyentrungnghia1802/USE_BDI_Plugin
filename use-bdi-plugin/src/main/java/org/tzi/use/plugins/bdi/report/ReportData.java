package org.tzi.use.plugins.bdi.report;

import java.time.Instant;
import java.util.List;
import java.util.Objects;

import org.tzi.use.plugins.bdi.validation.ConsistencyIssue;

public final class ReportData {
    private final String projectName;
    private final String pluginVersion;
    private final String useVersion;
    private final Instant timestamp;
    private final int issuesCount;
    private final int mappingsCount;
    private final String notes;
    private final List<ConsistencyIssue> issues;

    public ReportData(String projectName,
                      String pluginVersion,
                      String useVersion,
                      Instant timestamp,
                      int issuesCount,
                      int mappingsCount,
                      String notes) {
        this(projectName, pluginVersion, useVersion, timestamp, issuesCount, mappingsCount, notes, List.of());
    }

    public ReportData(String projectName,
                      String pluginVersion,
                      String useVersion,
                      Instant timestamp,
                      int issuesCount,
                      int mappingsCount,
                      String notes,
                      List<ConsistencyIssue> issues) {
        this.projectName = Objects.requireNonNull(projectName, "projectName");
        this.pluginVersion = Objects.requireNonNull(pluginVersion, "pluginVersion");
        this.useVersion = Objects.requireNonNull(useVersion, "useVersion");
        this.timestamp = Objects.requireNonNull(timestamp, "timestamp");
        if (issuesCount < 0 || mappingsCount < 0) {
            throw new IllegalArgumentException("report counts must not be negative");
        }
        this.issuesCount = issuesCount;
        this.mappingsCount = mappingsCount;
        this.notes = notes;
        this.issues = List.copyOf(Objects.requireNonNull(issues, "issues"));
    }

    public String projectName() { return projectName; }
    public String pluginVersion() { return pluginVersion; }
    public String useVersion() { return useVersion; }
    public Instant timestamp() { return timestamp; }
    public int issuesCount() { return issuesCount; }
    public int mappingsCount() { return mappingsCount; }
    public String notes() { return notes; }
    public List<ConsistencyIssue> issues() { return issues; }
}

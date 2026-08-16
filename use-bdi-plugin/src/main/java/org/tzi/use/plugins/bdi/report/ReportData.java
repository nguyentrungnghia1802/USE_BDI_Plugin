package org.tzi.use.plugins.bdi.report;

import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.tzi.use.plugins.bdi.application.AnalysisMetamodelDescriptor;
import org.tzi.use.plugins.bdi.index.BdiMetamodelVersion;
import org.tzi.use.plugins.bdi.validation.ConsistencyIssue;
import org.tzi.use.plugins.bdi.validation.Suppression;

public final class ReportData {
    private final String projectName;
    private final String pluginVersion;
    private final String useVersion;
    private final AnalysisMetamodelDescriptor analysisMetamodel;
    private final String bdiMetamodelVersion;
    private final List<String> parserVersions;
    private final Instant timestamp;
    private final int issuesCount;
    private final int mappingsCount;
    private final String notes;
    private final List<ConsistencyIssue> issues;
    private final List<Suppression> suppressions;
    private final Optional<String> modelHash;
    private final Optional<String> mappingHash;

    public ReportData(String projectName,
                      String pluginVersion,
                      String useVersion,
                      Instant timestamp,
                      int issuesCount,
                      int mappingsCount,
                      String notes) {
        this(projectName, pluginVersion, useVersion, timestamp, issuesCount, mappingsCount, notes,
                Optional.empty(), Optional.empty(), List.of(), List.of());
    }

    public ReportData(String projectName,
                      String pluginVersion,
                      String useVersion,
                      Instant timestamp,
                      int issuesCount,
                      int mappingsCount,
                      String notes,
                      List<ConsistencyIssue> issues) {
        this(projectName, pluginVersion, useVersion, timestamp, issuesCount, mappingsCount, notes,
                Optional.empty(), Optional.empty(), issues, List.of());
    }

    public ReportData(String projectName,
                      String pluginVersion,
                      String useVersion,
                      Instant timestamp,
                      int issuesCount,
                      int mappingsCount,
                      String notes,
                      Optional<String> modelHash,
                      Optional<String> mappingHash,
                      List<ConsistencyIssue> issues) {
        this(projectName, pluginVersion, useVersion, timestamp, issuesCount, mappingsCount, notes,
                modelHash, mappingHash, issues, List.of());
    }

    public ReportData(String projectName,
                      String pluginVersion,
                      String useVersion,
                      Instant timestamp,
                      int issuesCount,
                      int mappingsCount,
                      String notes,
                      Optional<String> modelHash,
                      Optional<String> mappingHash,
                      List<ConsistencyIssue> issues,
                      List<Suppression> suppressions) {
        this(projectName, pluginVersion, useVersion,
                AnalysisMetamodelDescriptor.current(), BdiMetamodelVersion.CURRENT, List.of(),
                timestamp, issuesCount, mappingsCount, notes, modelHash, mappingHash, issues, suppressions);
    }

    public ReportData(String projectName,
                      String pluginVersion,
                      String useVersion,
                      AnalysisMetamodelDescriptor analysisMetamodel,
                      String bdiMetamodelVersion,
                      List<String> parserVersions,
                      Instant timestamp,
                      int issuesCount,
                      int mappingsCount,
                      String notes,
                      Optional<String> modelHash,
                      Optional<String> mappingHash,
                      List<ConsistencyIssue> issues,
                      List<Suppression> suppressions) {
        this.projectName = Objects.requireNonNull(projectName, "projectName");
        this.pluginVersion = Objects.requireNonNull(pluginVersion, "pluginVersion");
        this.useVersion = Objects.requireNonNull(useVersion, "useVersion");
        this.analysisMetamodel = Objects.requireNonNull(analysisMetamodel, "analysisMetamodel");
        if (bdiMetamodelVersion == null || bdiMetamodelVersion.isBlank()) {
            throw new IllegalArgumentException("bdiMetamodelVersion must not be blank");
        }
        this.bdiMetamodelVersion = bdiMetamodelVersion;
        this.parserVersions = List.copyOf(Objects.requireNonNull(parserVersions, "parserVersions"));
        if (this.parserVersions.stream().anyMatch(value -> value == null || value.isBlank())
                || !this.parserVersions.equals(this.parserVersions.stream().distinct().sorted().toList())) {
            throw new IllegalArgumentException("parserVersions must be non-blank, unique, and sorted");
        }
        this.timestamp = Objects.requireNonNull(timestamp, "timestamp");
        if (issuesCount < 0 || mappingsCount < 0) {
            throw new IllegalArgumentException("report counts must not be negative");
        }
        this.issuesCount = issuesCount;
        this.mappingsCount = mappingsCount;
        this.notes = notes;
        this.issues = List.copyOf(Objects.requireNonNull(issues, "issues"));
        this.suppressions = List.copyOf(Objects.requireNonNull(suppressions, "suppressions"));
        this.modelHash = validateHash(modelHash, "modelHash");
        this.mappingHash = validateHash(mappingHash, "mappingHash");
    }

    public String projectName() { return projectName; }
    public String pluginVersion() { return pluginVersion; }
    public String useVersion() { return useVersion; }
    public AnalysisMetamodelDescriptor analysisMetamodel() { return analysisMetamodel; }
    public String bdiMetamodelVersion() { return bdiMetamodelVersion; }
    public List<String> parserVersions() { return parserVersions; }
    public Instant timestamp() { return timestamp; }
    public int issuesCount() { return issuesCount; }
    public int mappingsCount() { return mappingsCount; }
    public String notes() { return notes; }
    public List<ConsistencyIssue> issues() { return issues; }
    public List<Suppression> suppressions() { return suppressions; }
    public Optional<String> modelHash() { return modelHash; }
    public Optional<String> mappingHash() { return mappingHash; }

    private static Optional<String> validateHash(Optional<String> value, String field) {
        Objects.requireNonNull(value, field);
        value.ifPresent(hash -> {
            if (!hash.matches("[0-9a-fA-F]{64}")) {
                throw new IllegalArgumentException(field + " must be a SHA-256 hex digest");
            }
        });
        return value;
    }
}

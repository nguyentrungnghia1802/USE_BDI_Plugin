package org.tzi.use.plugins.bdi.cli;

import java.util.Objects;
import java.util.List;
import java.util.Optional;

import org.tzi.use.plugins.bdi.application.CurrentAnalysisSnapshot;
import org.tzi.use.plugins.bdi.importer.MasProjectDiagnostic;
import org.tzi.use.plugins.bdi.model.mas.MasProjectModel;

public record HeadlessAnalysisResult(
        String projectName,
        CurrentAnalysisSnapshot snapshot,
        Optional<MasProjectModel> project,
        List<MasProjectDiagnostic> projectDiagnostics) {
    public HeadlessAnalysisResult {
        if (projectName == null || projectName.isBlank()) {
            throw new IllegalArgumentException("projectName must not be blank");
        }
        Objects.requireNonNull(snapshot, "snapshot");
        project = Objects.requireNonNull(project, "project");
        projectDiagnostics = List.copyOf(Objects.requireNonNull(projectDiagnostics, "projectDiagnostics"));
    }

    public HeadlessAnalysisResult(String projectName, CurrentAnalysisSnapshot snapshot) {
        this(projectName, snapshot, Optional.empty(), List.of());
    }

    public HeadlessExitCode exitCode() {
        return HeadlessExitCode.forResult(snapshot, projectDiagnostics);
    }
}

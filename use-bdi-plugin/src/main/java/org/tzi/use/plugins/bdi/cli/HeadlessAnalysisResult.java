package org.tzi.use.plugins.bdi.cli;

import java.util.Objects;

import org.tzi.use.plugins.bdi.application.CurrentAnalysisSnapshot;

public record HeadlessAnalysisResult(String projectName, CurrentAnalysisSnapshot snapshot) {
    public HeadlessAnalysisResult {
        if (projectName == null || projectName.isBlank()) {
            throw new IllegalArgumentException("projectName must not be blank");
        }
        Objects.requireNonNull(snapshot, "snapshot");
    }

    public HeadlessExitCode exitCode() {
        return HeadlessExitCode.forSnapshot(snapshot);
    }
}

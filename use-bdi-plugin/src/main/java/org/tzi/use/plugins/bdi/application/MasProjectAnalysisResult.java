package org.tzi.use.plugins.bdi.application;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.tzi.use.plugins.bdi.importer.MasProjectDiagnostic;
import org.tzi.use.plugins.bdi.model.mas.MasProjectModel;

/** Immutable aggregate returned by the project analysis application service. */
public record MasProjectAnalysisResult(
        Optional<MasProjectModel> project,
        CurrentAnalysisSnapshot snapshot,
        List<MasProjectDiagnostic> projectDiagnostics) {
    public MasProjectAnalysisResult {
        project = Objects.requireNonNull(project, "project");
        snapshot = Objects.requireNonNull(snapshot, "snapshot");
        projectDiagnostics = sortedDiagnostics(projectDiagnostics);
    }

    public boolean hasErrors() {
        return snapshot.bdiImport().hasErrors()
                || projectDiagnostics.stream().anyMatch(item -> item.severity().name().equals("ERROR"));
    }

    private static List<MasProjectDiagnostic> sortedDiagnostics(List<MasProjectDiagnostic> diagnostics) {
        List<MasProjectDiagnostic> copy = new ArrayList<>(Objects.requireNonNull(diagnostics, "projectDiagnostics"));
        copy.sort(Comparator.comparing((MasProjectDiagnostic item) -> item.source().toString())
                .thenComparingInt(MasProjectDiagnostic::line)
                .thenComparingInt(MasProjectDiagnostic::column)
                .thenComparing(MasProjectDiagnostic::code)
                .thenComparing(MasProjectDiagnostic::message));
        return List.copyOf(copy);
    }
}

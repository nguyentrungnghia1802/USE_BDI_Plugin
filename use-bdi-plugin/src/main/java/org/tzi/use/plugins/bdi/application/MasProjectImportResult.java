package org.tzi.use.plugins.bdi.application;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.tzi.use.plugins.bdi.importer.MasProjectDiagnostic;
import org.tzi.use.plugins.bdi.importer.MasProjectDiagnosticSeverity;
import org.tzi.use.plugins.bdi.model.mas.MasProjectModel;

public record MasProjectImportResult(
        Optional<MasProjectModel> project,
        BdiImportSnapshot bdiSnapshot,
        List<MasProjectDiagnostic> diagnostics) {
    public MasProjectImportResult {
        project = Objects.requireNonNull(project, "project");
        bdiSnapshot = Objects.requireNonNull(bdiSnapshot, "bdiSnapshot");
        diagnostics = List.copyOf(Objects.requireNonNull(diagnostics, "diagnostics"));
    }

    public boolean hasErrors() {
        return diagnostics.stream()
                .anyMatch(diagnostic -> diagnostic.severity() == MasProjectDiagnosticSeverity.ERROR)
                || bdiSnapshot.hasErrors();
    }
}

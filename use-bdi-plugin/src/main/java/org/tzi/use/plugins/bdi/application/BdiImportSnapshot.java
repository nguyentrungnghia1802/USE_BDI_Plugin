package org.tzi.use.plugins.bdi.application;

import java.util.List;
import java.util.Objects;

import org.tzi.use.plugins.bdi.importer.AslDiagnostic;
import org.tzi.use.plugins.bdi.index.BdiIndex;
import org.tzi.use.plugins.bdi.model.ir.AgentModel;

/** Immutable result consumed by the explorer view after a background import. */
public record BdiImportSnapshot(
        List<AgentModel> models,
        List<AslDiagnostic> diagnostics,
        BdiIndex index) {
    public BdiImportSnapshot {
        models = List.copyOf(Objects.requireNonNull(models, "models"));
        diagnostics = List.copyOf(Objects.requireNonNull(diagnostics, "diagnostics"));
        index = Objects.requireNonNull(index, "index");
    }

    public int fileCount() {
        return models.size();
    }

    public boolean hasErrors() {
        return !diagnostics.isEmpty();
    }
}

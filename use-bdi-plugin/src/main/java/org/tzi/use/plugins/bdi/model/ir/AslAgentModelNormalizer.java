package org.tzi.use.plugins.bdi.model.ir;

import java.util.List;
import java.util.Objects;

import org.tzi.use.plugins.bdi.importer.AslImportResult;
import org.tzi.use.plugins.bdi.importer.AslParseSummary;

/** Converts importer DTOs into the Jason-independent root IR. */
public final class AslAgentModelNormalizer {
    public AgentModel normalize(AslParseSummary summary) {
        Objects.requireNonNull(summary, "summary");
        return new AgentModel(
                summary.source(),
                summary.parserVersion(),
                summary.beliefCount(),
                summary.goalCount(),
                summary.planCount());
    }

    public List<AgentModel> normalize(AslImportResult result) {
        Objects.requireNonNull(result, "result");
        return result.fileSummaries().stream()
                .map(this::normalize)
                .toList();
    }
}

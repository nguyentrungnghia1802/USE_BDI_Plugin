package org.tzi.use.plugins.bdi.importer;

import java.util.List;
import java.util.Objects;

public record AslImportResult(
        List<AslParseSummary> fileSummaries,
        List<AslDiagnostic> diagnostics) {
    public AslImportResult {
        fileSummaries = List.copyOf(Objects.requireNonNull(fileSummaries, "fileSummaries"));
        diagnostics = List.copyOf(Objects.requireNonNull(diagnostics, "diagnostics"));
    }

    public AslImportResult(List<AslParseSummary> fileSummaries) {
        this(fileSummaries, List.of());
    }

    public int fileCount() {
        return fileSummaries.size();
    }

    public int totalBeliefCount() {
        return fileSummaries.stream().mapToInt(AslParseSummary::beliefCount).sum();
    }

    public int totalGoalCount() {
        return fileSummaries.stream().mapToInt(AslParseSummary::goalCount).sum();
    }

    public int totalPlanCount() {
        return fileSummaries.stream().mapToInt(AslParseSummary::planCount).sum();
    }

    public boolean hasErrors() {
        return diagnostics.stream()
                .anyMatch(diagnostic -> diagnostic.severity() == AslDiagnosticSeverity.ERROR);
    }
}

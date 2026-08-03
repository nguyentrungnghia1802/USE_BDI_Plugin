package org.tzi.use.plugins.bdi.importer;

import java.util.List;
import java.util.Objects;

public record AslImportResult(List<AslParseSummary> fileSummaries) {
    public AslImportResult {
        fileSummaries = List.copyOf(Objects.requireNonNull(fileSummaries, "fileSummaries"));
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
}

package org.tzi.use.plugins.bdi.importer;

import java.util.List;
import java.util.Objects;

public record AslImportReport(
        AslImportResult importResult,
        List<String> parserVersions) {
    public AslImportReport {
        Objects.requireNonNull(importResult, "importResult");
        parserVersions = List.copyOf(Objects.requireNonNull(parserVersions, "parserVersions"));
    }

    public static AslImportReport from(AslImportResult result) {
        Objects.requireNonNull(result, "result");
        List<String> versions = result.fileSummaries().stream()
                .map(AslParseSummary::parserVersion)
                .distinct()
                .toList();
        return new AslImportReport(result, versions);
    }
}

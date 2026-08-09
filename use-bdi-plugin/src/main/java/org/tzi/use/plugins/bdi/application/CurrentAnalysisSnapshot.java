package org.tzi.use.plugins.bdi.application;

import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.tzi.use.plugins.bdi.model.mapping.MappingDocument;
import org.tzi.use.plugins.bdi.model.mapping.MappingFingerprint;
import org.tzi.use.plugins.bdi.use.UseModelSnapshot;
import org.tzi.use.plugins.bdi.use.UseModelFingerprint;
import org.tzi.use.plugins.bdi.validation.ConsistencyIssue;
import org.tzi.use.plugins.bdi.validation.Suppression;

/** Immutable source of truth for one Problems/export/headless analysis result. */
public record CurrentAnalysisSnapshot(
        Instant timestamp,
        BdiImportSnapshot bdiImport,
        Optional<UseModelSnapshot> useModel,
        MappingDocument mapping,
        String configurationOrigin,
        List<Suppression> suppressions,
        List<ConsistencyIssue> issues,
        int importedFileCount,
        int mappingCount,
        int issueCount,
        Optional<String> modelHash,
        String mappingHash,
        AnalysisVersionMetadata versions) {
    public CurrentAnalysisSnapshot {
        Objects.requireNonNull(timestamp, "timestamp");
        Objects.requireNonNull(bdiImport, "bdiImport");
        useModel = Objects.requireNonNull(useModel, "useModel");
        Objects.requireNonNull(mapping, "mapping");
        if (configurationOrigin == null || configurationOrigin.isBlank()) {
            throw new IllegalArgumentException("configurationOrigin must not be blank");
        }
        suppressions = List.copyOf(Objects.requireNonNull(suppressions, "suppressions"));
        issues = List.copyOf(Objects.requireNonNull(issues, "issues"));
        modelHash = Objects.requireNonNull(modelHash, "modelHash");
        Objects.requireNonNull(versions, "versions");

        requireCount(importedFileCount, bdiImport.fileCount(), "importedFileCount");
        requireCount(mappingCount, mapping.bindings().size(), "mappingCount");
        requireCount(issueCount, issues.size(), "issueCount");
        Optional<String> expectedModelHash = useModel.map(UseModelFingerprint::compute);
        if (!modelHash.equals(expectedModelHash)) {
            throw new IllegalArgumentException("modelHash does not match the USE projection");
        }
        if (!MappingFingerprint.compute(mapping).equals(mappingHash)) {
            throw new IllegalArgumentException("mappingHash does not match the mapping document");
        }
        if (!bdiImport.index().metamodelVersion().equals(versions.bdiMetamodelVersion())) {
            throw new IllegalArgumentException("BDI metamodel version does not match the import index");
        }
        List<String> parserVersions = bdiImport.models().stream()
                .map(model -> model.parserVersion())
                .distinct()
                .sorted()
                .toList();
        if (!parserVersions.equals(versions.parserVersions())) {
            throw new IllegalArgumentException("Parser versions do not match the imported models");
        }
    }

    private static void requireCount(int actual, int expected, String field) {
        if (actual != expected) {
            throw new IllegalArgumentException(field + " does not match its immutable data");
        }
    }
}

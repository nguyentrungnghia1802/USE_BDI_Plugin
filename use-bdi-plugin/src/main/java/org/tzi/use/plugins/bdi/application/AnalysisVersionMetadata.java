package org.tzi.use.plugins.bdi.application;

import java.util.List;
import java.util.Objects;

/** Version evidence captured with one consistency-analysis result. */
public record AnalysisVersionMetadata(
        String pluginVersion,
        String useVersion,
        String bdiMetamodelVersion,
        AnalysisMetamodelDescriptor analysisMetamodel,
        List<String> parserVersions) {
    public AnalysisVersionMetadata {
        requireText(pluginVersion, "pluginVersion");
        requireText(useVersion, "useVersion");
        requireText(bdiMetamodelVersion, "bdiMetamodelVersion");
        Objects.requireNonNull(analysisMetamodel, "analysisMetamodel");
        parserVersions = List.copyOf(Objects.requireNonNull(parserVersions, "parserVersions"));
        if (parserVersions.stream().anyMatch(value -> value == null || value.isBlank())) {
            throw new IllegalArgumentException("parserVersions must contain only non-blank values");
        }
        if (!parserVersions.equals(parserVersions.stream().distinct().sorted().toList())) {
            throw new IllegalArgumentException("parserVersions must be unique and sorted");
        }
    }

    public AnalysisVersionMetadata(
            String pluginVersion,
            String useVersion,
            String bdiMetamodelVersion,
            List<String> parserVersions) {
        this(pluginVersion, useVersion, bdiMetamodelVersion,
                AnalysisMetamodelDescriptor.current(), parserVersions);
    }

    private static void requireText(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(field + " must not be blank");
        }
    }
}

package org.tzi.use.plugins.bdi.importer;

import java.nio.file.Path;
import java.util.List;
import java.util.Objects;

public record AslParseSummary(
        Path source,
        String parserVersion,
        int beliefCount,
        int goalCount,
        int planCount,
        List<AslSourceLocation> sourceLocations) {
    public AslParseSummary {
        Objects.requireNonNull(source, "source");
        Objects.requireNonNull(parserVersion, "parserVersion");
        sourceLocations = List.copyOf(Objects.requireNonNull(sourceLocations, "sourceLocations"));
    }

    public AslParseSummary(
            Path source,
            String parserVersion,
            int beliefCount,
            int goalCount,
            int planCount) {
        this(source, parserVersion, beliefCount, goalCount, planCount, List.of());
    }
}

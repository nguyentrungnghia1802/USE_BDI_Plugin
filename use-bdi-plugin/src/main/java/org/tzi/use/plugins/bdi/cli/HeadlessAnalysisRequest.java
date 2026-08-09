package org.tzi.use.plugins.bdi.cli;

import java.nio.file.Path;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/** Explicit, CWD-independent inputs for one headless analysis. */
public record HeadlessAnalysisRequest(
        Path useFile,
        List<Path> aslFiles,
        Optional<Path> mappingFile,
        Optional<Path> rulesFile,
        Optional<Path> suppressionsFile,
        Instant timestamp,
        Optional<String> projectName) {
    public HeadlessAnalysisRequest {
        Objects.requireNonNull(useFile, "useFile");
        aslFiles = List.copyOf(Objects.requireNonNull(aslFiles, "aslFiles"));
        if (aslFiles.isEmpty()) {
            throw new IllegalArgumentException("At least one --asl file is required");
        }
        mappingFile = Objects.requireNonNull(mappingFile, "mappingFile");
        rulesFile = Objects.requireNonNull(rulesFile, "rulesFile");
        suppressionsFile = Objects.requireNonNull(suppressionsFile, "suppressionsFile");
        Objects.requireNonNull(timestamp, "timestamp");
        projectName = Objects.requireNonNull(projectName, "projectName");
        projectName.ifPresent(value -> {
            if (value.isBlank()) {
                throw new IllegalArgumentException("projectName must not be blank");
            }
        });
    }
}

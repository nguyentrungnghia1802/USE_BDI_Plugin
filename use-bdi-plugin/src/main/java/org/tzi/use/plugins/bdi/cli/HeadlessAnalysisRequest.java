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
        Optional<Path> projectFile,
        Optional<Path> mappingFile,
        Optional<Path> rulesFile,
        Optional<Path> suppressionsFile,
        Instant timestamp,
        Optional<String> projectName,
        Optional<HeadlessStateFixture> stateFixture) {
    public HeadlessAnalysisRequest {
        Objects.requireNonNull(useFile, "useFile");
        aslFiles = List.copyOf(Objects.requireNonNull(aslFiles, "aslFiles"));
        projectFile = Objects.requireNonNull(projectFile, "projectFile");
        if (aslFiles.isEmpty() == projectFile.isEmpty()) {
            throw new IllegalArgumentException("Exactly one of --asl or --jcm is required");
        }
        mappingFile = Objects.requireNonNull(mappingFile, "mappingFile");
        rulesFile = Objects.requireNonNull(rulesFile, "rulesFile");
        suppressionsFile = Objects.requireNonNull(suppressionsFile, "suppressionsFile");
        Objects.requireNonNull(timestamp, "timestamp");
        projectName = Objects.requireNonNull(projectName, "projectName");
        stateFixture = Objects.requireNonNull(stateFixture, "stateFixture");
        projectName.ifPresent(value -> {
            if (value.isBlank()) {
                throw new IllegalArgumentException("projectName must not be blank");
            }
        });
    }

    public HeadlessAnalysisRequest(
            Path useFile,
            List<Path> aslFiles,
            Optional<Path> projectFile,
            Optional<Path> mappingFile,
            Optional<Path> rulesFile,
            Optional<Path> suppressionsFile,
            Instant timestamp,
            Optional<String> projectName) {
        this(useFile, aslFiles, projectFile, mappingFile, rulesFile, suppressionsFile,
                timestamp, projectName, Optional.empty());
    }

    /** Compatibility constructor for the direct AgentSpeak CLI contract. */
    public HeadlessAnalysisRequest(
            Path useFile,
            List<Path> aslFiles,
            Optional<Path> mappingFile,
            Optional<Path> rulesFile,
            Optional<Path> suppressionsFile,
            Instant timestamp,
            Optional<String> projectName) {
        this(useFile, aslFiles, Optional.empty(), mappingFile, rulesFile, suppressionsFile,
                timestamp, projectName, Optional.empty());
    }

    public boolean isProjectAnalysis() {
        return projectFile.isPresent();
    }
}

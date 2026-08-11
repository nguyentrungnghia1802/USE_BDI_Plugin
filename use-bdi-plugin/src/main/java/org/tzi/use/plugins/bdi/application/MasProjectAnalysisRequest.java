package org.tzi.use.plugins.bdi.application;

import java.nio.file.Path;
import java.time.Instant;
import java.util.Objects;
import java.util.Optional;

import org.tzi.use.plugins.bdi.model.mapping.MappingDocument;
import org.tzi.use.plugins.bdi.use.UseModelSnapshot;
import org.tzi.use.plugins.bdi.validation.SnapshotOclEvaluator;

/** Immutable inputs for one static JaCaMo project analysis. */
public record MasProjectAnalysisRequest(
        Path projectRoot,
        Path projectFile,
        Instant timestamp,
        Optional<UseModelSnapshot> useModel,
        Optional<SnapshotOclEvaluator> oclEvaluator,
        MappingDocument mapping,
        BdiProjectConfiguration configuration) {
    public MasProjectAnalysisRequest {
        projectRoot = normalize(projectRoot, "projectRoot");
        projectFile = normalize(projectFile, "projectFile");
        if (!projectFile.startsWith(projectRoot) || projectFile.equals(projectRoot)) {
            throw new IllegalArgumentException("projectFile must be inside projectRoot");
        }
        Objects.requireNonNull(timestamp, "timestamp");
        useModel = Objects.requireNonNull(useModel, "useModel");
        oclEvaluator = Objects.requireNonNull(oclEvaluator, "oclEvaluator");
        if (useModel.isEmpty() && oclEvaluator.isPresent()) {
            throw new IllegalArgumentException("OCL evaluator requires a USE projection");
        }
        mapping = Objects.requireNonNull(mapping, "mapping");
        configuration = Objects.requireNonNull(configuration, "configuration");
    }

    public static MasProjectAnalysisRequest of(
            Path projectFile,
            Instant timestamp,
            Optional<UseModelSnapshot> useModel,
            Optional<SnapshotOclEvaluator> oclEvaluator,
            MappingDocument mapping,
            BdiProjectConfiguration configuration) {
        Path file = normalize(projectFile, "projectFile");
        Path root = file.getParent();
        if (root == null) {
            throw new IllegalArgumentException("projectFile must have a parent directory");
        }
        return new MasProjectAnalysisRequest(
                root, file, timestamp, useModel, oclEvaluator, mapping, configuration);
    }

    private static Path normalize(Path value, String field) {
        return Objects.requireNonNull(value, field).toAbsolutePath().normalize();
    }
}

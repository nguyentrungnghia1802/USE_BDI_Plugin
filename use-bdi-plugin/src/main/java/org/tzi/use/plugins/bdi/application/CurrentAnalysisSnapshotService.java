package org.tzi.use.plugins.bdi.application;

import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

import org.tzi.use.plugins.bdi.model.mapping.MappingDocument;
import org.tzi.use.plugins.bdi.model.mapping.MappingFingerprint;
import org.tzi.use.plugins.bdi.use.UseModelSnapshot;
import org.tzi.use.plugins.bdi.use.UseModelFingerprint;
import org.tzi.use.plugins.bdi.validation.ConsistencyIssue;
import org.tzi.use.plugins.bdi.validation.SnapshotOclEvaluator;
import org.tzi.use.plugins.bdi.validation.Suppression;
import org.tzi.use.plugins.bdi.validation.ValidationContext;
import org.tzi.use.plugins.bdi.validation.ValidationOrchestrator;

/** Composes one immutable analysis result and evaluates validation exactly once. */
public final class CurrentAnalysisSnapshotService {
    private final Function<ValidationContext, List<ConsistencyIssue>> validator;
    private final List<Suppression> suppressions;
    private final String configurationOrigin;
    private final String pluginVersion;
    private final String useVersion;

    public CurrentAnalysisSnapshotService(
            ValidationOrchestrator orchestrator,
            String configurationOrigin,
            String pluginVersion,
            String useVersion) {
        this(
                Objects.requireNonNull(orchestrator, "orchestrator")::evaluate,
                orchestrator.suppressions(),
                configurationOrigin,
                pluginVersion,
                useVersion);
    }

    CurrentAnalysisSnapshotService(
            Function<ValidationContext, List<ConsistencyIssue>> validator,
            List<Suppression> suppressions,
            String configurationOrigin,
            String pluginVersion,
            String useVersion) {
        this.validator = Objects.requireNonNull(validator, "validator");
        this.suppressions = List.copyOf(Objects.requireNonNull(suppressions, "suppressions"));
        this.configurationOrigin = requireText(configurationOrigin, "configurationOrigin");
        this.pluginVersion = requireText(pluginVersion, "pluginVersion");
        this.useVersion = requireText(useVersion, "useVersion");
    }

    public CurrentAnalysisSnapshot create(
            Instant timestamp,
            BdiImportSnapshot imported,
            Optional<UseModelSnapshot> useModel,
            Optional<SnapshotOclEvaluator> oclEvaluator,
            MappingDocument mapping) {
        Objects.requireNonNull(timestamp, "timestamp");
        Objects.requireNonNull(imported, "imported");
        Objects.requireNonNull(useModel, "useModel");
        Objects.requireNonNull(oclEvaluator, "oclEvaluator");
        Objects.requireNonNull(mapping, "mapping");
        if (useModel.isEmpty() && oclEvaluator.isPresent()) {
            throw new IllegalArgumentException("OCL evaluator requires a USE projection");
        }

        List<ConsistencyIssue> issues = List.copyOf(validator.apply(
                ValidationContext.from(imported, mapping, useModel, oclEvaluator)));
        AnalysisVersionMetadata versions = new AnalysisVersionMetadata(
                pluginVersion,
                useVersion,
                imported.index().metamodelVersion(),
                imported.models().stream()
                        .map(model -> model.parserVersion())
                        .distinct()
                        .sorted()
                        .toList());
        return new CurrentAnalysisSnapshot(
                timestamp,
                imported,
                useModel,
                mapping,
                configurationOrigin,
                suppressions,
                issues,
                imported.fileCount(),
                mapping.bindings().size(),
                issues.size(),
                useModel.map(UseModelFingerprint::compute),
                MappingFingerprint.compute(mapping),
                versions);
    }

    private static String requireText(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(field + " must not be blank");
        }
        return value;
    }
}

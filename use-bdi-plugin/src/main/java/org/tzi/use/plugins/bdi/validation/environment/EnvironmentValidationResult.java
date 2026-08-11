package org.tzi.use.plugins.bdi.validation.environment;

import java.util.List;
import java.util.Objects;

import org.tzi.use.plugins.bdi.model.environment.EnvironmentMappingDocument;

/** Revalidated environment document together with explicit rule findings. */
public record EnvironmentValidationResult(
        EnvironmentMappingDocument document,
        List<EnvironmentFinding> findings) {
    public EnvironmentValidationResult {
        document = Objects.requireNonNull(document, "document");
        findings = List.copyOf(Objects.requireNonNull(findings, "findings"));
    }
}

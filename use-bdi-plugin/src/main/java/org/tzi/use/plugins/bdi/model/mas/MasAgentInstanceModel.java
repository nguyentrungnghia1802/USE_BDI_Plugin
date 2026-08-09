package org.tzi.use.plugins.bdi.model.mas;

import java.util.Objects;

import org.tzi.use.plugins.bdi.model.source.ProjectSourceId;

/** Portable JaCaMo agent-instance declaration after parser normalization. */
public record MasAgentInstanceModel(
        String name,
        ProjectSourceId source,
        MasAgentImportStatus status) {
    public MasAgentInstanceModel {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("name must not be blank");
        }
        source = Objects.requireNonNull(source, "source");
        status = Objects.requireNonNull(status, "status");
    }
}

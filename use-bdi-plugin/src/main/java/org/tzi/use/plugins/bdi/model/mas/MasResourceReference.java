package org.tzi.use.plugins.bdi.model.mas;

import java.util.Objects;
import java.util.Optional;

import org.tzi.use.plugins.bdi.model.source.ProjectSourceId;

/** Visible project resource that is not yet semantically normalized. */
public record MasResourceReference(
        MasResourceKind kind,
        String name,
        Optional<ProjectSourceId> source,
        MasResourceStatus status) {
    public MasResourceReference {
        kind = Objects.requireNonNull(kind, "kind");
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("name must not be blank");
        }
        source = Objects.requireNonNull(source, "source");
        status = Objects.requireNonNull(status, "status");
    }
}

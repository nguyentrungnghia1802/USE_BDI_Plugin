package org.tzi.use.plugins.bdi.model.environment;

import java.util.Objects;

import org.tzi.use.plugins.bdi.model.source.ProjectSourceId;

/** Portable source evidence for one environment mapping decision. */
public record EnvironmentSourceProvenance(ProjectSourceId source, String origin) {
    public EnvironmentSourceProvenance {
        source = Objects.requireNonNull(source, "source");
        if (origin == null || origin.isBlank()) {
            throw new IllegalArgumentException("origin must not be blank");
        }
    }
}

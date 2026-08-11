package org.tzi.use.plugins.bdi.model.environment;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

/** Immutable, versioned document for persisted static environment bindings. */
public record EnvironmentMappingDocument(
        String schemaVersion,
        List<PersistedEnvironmentMapping> mappings) {
    public static final String CURRENT_SCHEMA_VERSION = "0.1.0";

    public EnvironmentMappingDocument {
        if (!CURRENT_SCHEMA_VERSION.equals(schemaVersion)) {
            throw new IllegalArgumentException("Unsupported environment mapping schema version: " + schemaVersion);
        }
        List<PersistedEnvironmentMapping> sorted = new ArrayList<>(
                Objects.requireNonNull(mappings, "mappings"));
        sorted.sort(Comparator.comparing(PersistedEnvironmentMapping::key));
        if (sorted.stream().map(PersistedEnvironmentMapping::key).distinct().count() != sorted.size()) {
            throw new IllegalArgumentException("Duplicate environment mapping key");
        }
        mappings = List.copyOf(sorted);
    }

    public static EnvironmentMappingDocument empty() {
        return new EnvironmentMappingDocument(CURRENT_SCHEMA_VERSION, List.of());
    }

    /** Returns only explicit user confirmations whose targets are current. */
    public List<EnvironmentMapping> confirmedCurrentMappings() {
        return mappings.stream()
                .filter(PersistedEnvironmentMapping::isConfirmedCurrent)
                .map(PersistedEnvironmentMapping::toRuntimeMapping)
                .toList();
    }

    public EnvironmentMappingDocument withMappings(List<PersistedEnvironmentMapping> updated) {
        return new EnvironmentMappingDocument(schemaVersion, updated);
    }

    /** Validates that every portable provenance identity can resolve under this project root. */
    public void validateProjectRoot(Path projectRoot) {
        Path root = Objects.requireNonNull(projectRoot, "projectRoot").toAbsolutePath().normalize();
        for (PersistedEnvironmentMapping mapping : mappings) {
            mapping.provenance().source().resolve(root);
        }
    }
}

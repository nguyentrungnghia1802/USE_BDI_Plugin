package org.tzi.use.plugins.bdi.importer;

import java.nio.file.Path;
import java.util.Objects;
import java.util.Optional;

import org.tzi.use.plugins.bdi.model.mas.MasResourceKind;

public record ParsedMasResource(MasResourceKind kind, String name, Optional<Path> source) {
    public ParsedMasResource {
        kind = Objects.requireNonNull(kind, "kind");
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("name must not be blank");
        }
        source = Objects.requireNonNull(source, "source")
                .map(path -> path.toAbsolutePath().normalize());
    }
}

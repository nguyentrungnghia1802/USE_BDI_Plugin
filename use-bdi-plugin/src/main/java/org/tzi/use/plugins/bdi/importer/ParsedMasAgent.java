package org.tzi.use.plugins.bdi.importer;

import java.nio.file.Path;
import java.util.Objects;

public record ParsedMasAgent(String name, Path source) {
    public ParsedMasAgent {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("name must not be blank");
        }
        source = Objects.requireNonNull(source, "source").toAbsolutePath().normalize();
    }
}

package org.tzi.use.plugins.bdi.persistence;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

import org.tzi.use.plugins.bdi.validation.RuleConfiguration;

/** Reads and writes the deterministic, versioned rules.json configuration. */
public final class RuleConfigurationRepository {
    public void save(Path file, RuleConfiguration configuration) throws IOException {
        Objects.requireNonNull(file, "file");
        Objects.requireNonNull(configuration, "configuration");
        Path normalized = file.toAbsolutePath().normalize();
        Path parent = normalized.getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }
        Files.writeString(normalized, RuleConfigurationJsonCodec.encode(configuration), StandardCharsets.UTF_8);
    }

    public RuleConfiguration load(Path file) throws IOException {
        Objects.requireNonNull(file, "file");
        String json = Files.readString(file.toAbsolutePath().normalize(), StandardCharsets.UTF_8);
        try {
            return RuleConfigurationJsonCodec.decode(json);
        } catch (RuntimeException error) {
            throw new IOException("Invalid rules.json configuration: " + error.getMessage(), error);
        }
    }
}

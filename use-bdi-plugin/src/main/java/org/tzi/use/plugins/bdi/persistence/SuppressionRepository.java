package org.tzi.use.plugins.bdi.persistence;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;

import org.tzi.use.plugins.bdi.validation.Suppression;

/** Reads and writes the deterministic, versioned suppressions.json file. */
public final class SuppressionRepository {
    public void save(Path file, List<Suppression> suppressions) throws IOException {
        Objects.requireNonNull(file, "file");
        Objects.requireNonNull(suppressions, "suppressions");
        Path normalized = file.toAbsolutePath().normalize();
        Path parent = normalized.getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }
        Files.writeString(normalized, SuppressionJsonCodec.encode(suppressions), StandardCharsets.UTF_8);
    }

    public List<Suppression> load(Path file) throws IOException {
        Objects.requireNonNull(file, "file");
        String json = Files.readString(file.toAbsolutePath().normalize(), StandardCharsets.UTF_8);
        try {
            return SuppressionJsonCodec.decode(json);
        } catch (RuntimeException error) {
            throw new IOException("Invalid suppressions.json file: " + error.getMessage(), error);
        }
    }
}

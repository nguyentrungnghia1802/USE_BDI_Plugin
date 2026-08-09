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
        throw new IOException("An explicit project root is required to save suppressions: " + file);
    }

    public void save(Path file, List<Suppression> suppressions, Path projectRoot) throws IOException {
        Objects.requireNonNull(file, "file");
        Objects.requireNonNull(suppressions, "suppressions");
        Path root;
        try {
            root = ProjectRootPolicy.requireExistingAbsoluteDirectory(projectRoot);
        } catch (RuntimeException error) {
            throw new IOException("Invalid project root for suppressions " + file + ": " + error.getMessage(), error);
        }
        Path normalized = file.toAbsolutePath().normalize();
        Path parent = normalized.getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }
        String encoded;
        try {
            encoded = SuppressionJsonCodec.encode(suppressions, root);
        } catch (RuntimeException error) {
            throw new IOException("Could not encode suppressions " + normalized + ": " + error.getMessage(), error);
        }
        Files.writeString(normalized, encoded, StandardCharsets.UTF_8);
    }

    public List<Suppression> load(Path file) throws IOException {
        throw new IOException("An explicit project root is required to load suppressions: " + file);
    }

    public List<Suppression> load(Path file, Path projectRoot) throws IOException {
        Objects.requireNonNull(file, "file");
        Path normalized = file.toAbsolutePath().normalize();
        try {
            Path root = ProjectRootPolicy.requireExistingAbsoluteDirectory(projectRoot);
            String json = Files.readString(normalized, StandardCharsets.UTF_8);
            return SuppressionJsonCodec.decode(json, root);
        } catch (RuntimeException error) {
            throw new IOException("Invalid suppressions.json file " + normalized + ": " + error.getMessage(), error);
        }
    }
}

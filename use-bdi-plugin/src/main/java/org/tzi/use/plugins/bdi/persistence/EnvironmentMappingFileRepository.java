package org.tzi.use.plugins.bdi.persistence;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

import org.tzi.use.plugins.bdi.model.environment.EnvironmentMappingDocument;

/** Reads and writes the versioned static CArtAgO mapping document. */
public final class EnvironmentMappingFileRepository {
    public void save(Path file, EnvironmentMappingDocument document) throws IOException {
        throw new IOException("An explicit project root is required to save an environment mapping file: " + file);
    }

    public void save(Path file, EnvironmentMappingDocument document, Path projectRoot) throws IOException {
        Objects.requireNonNull(file, "file");
        Objects.requireNonNull(document, "document");
        Path root = requireRoot(file, projectRoot);
        Path normalized = file.toAbsolutePath().normalize();
        String encoded;
        try {
            encoded = EnvironmentMappingJsonCodec.encode(document, root);
        } catch (RuntimeException error) {
            throw new IOException(
                    "Could not encode environment mapping file " + normalized + ": " + error.getMessage(), error);
        }
        Path parent = normalized.getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }
        Files.writeString(normalized, encoded, StandardCharsets.UTF_8);
    }

    public EnvironmentMappingDocument load(Path file) throws IOException {
        throw new IOException("An explicit project root is required to load an environment mapping file: " + file);
    }

    public EnvironmentMappingDocument load(Path file, Path projectRoot) throws IOException {
        Objects.requireNonNull(file, "file");
        Path normalized = file.toAbsolutePath().normalize();
        Path root = requireRoot(normalized, projectRoot);
        try {
            return EnvironmentMappingJsonCodec.decode(
                    Files.readString(normalized, StandardCharsets.UTF_8), root);
        } catch (RuntimeException error) {
            throw new IOException(
                    "Invalid .cartago-map.json environment mapping file "
                            + normalized + ": " + error.getMessage(), error);
        }
    }

    private static Path requireRoot(Path file, Path projectRoot) throws IOException {
        try {
            return ProjectRootPolicy.requireExistingAbsoluteDirectory(projectRoot);
        } catch (RuntimeException error) {
            throw new IOException(
                    "Invalid project root for environment mapping file " + file + ": " + error.getMessage(), error);
        }
    }
}

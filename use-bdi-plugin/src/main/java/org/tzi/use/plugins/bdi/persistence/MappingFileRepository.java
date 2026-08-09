package org.tzi.use.plugins.bdi.persistence;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

import org.tzi.use.plugins.bdi.model.mapping.MappingDocument;

/** Reads and writes the deterministic .bdimap.json mapping schema. */
public final class MappingFileRepository {
    public void save(Path file, MappingDocument document) throws IOException {
        throw new IOException("An explicit project root is required to save a mapping file: " + file);
    }

    public void save(Path file, MappingDocument document, Path projectRoot) throws IOException {
        Objects.requireNonNull(file, "file");
        Objects.requireNonNull(document, "document");
        Path root;
        try {
            root = ProjectRootPolicy.requireExistingAbsoluteDirectory(projectRoot);
        } catch (RuntimeException error) {
            throw new IOException("Invalid project root for mapping file " + file + ": " + error.getMessage(), error);
        }
        Path normalized = file.toAbsolutePath().normalize();
        Path parent = normalized.getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }
        String encoded;
        try {
            encoded = MappingJsonCodec.encode(document, root);
        } catch (RuntimeException error) {
            throw new IOException("Could not encode mapping file " + normalized + ": " + error.getMessage(), error);
        }
        Files.writeString(normalized, encoded, StandardCharsets.UTF_8);
    }

    public MappingDocument load(Path file) throws IOException {
        throw new IOException("An explicit project root is required to load a mapping file: " + file);
    }

    public MappingDocument load(Path file, Path projectRoot) throws IOException {
        Objects.requireNonNull(file, "file");
        Path normalized = file.toAbsolutePath().normalize();
        try {
            Path root = ProjectRootPolicy.requireExistingAbsoluteDirectory(projectRoot);
            String json = Files.readString(normalized, StandardCharsets.UTF_8);
            return MappingJsonCodec.decode(json, root);
        } catch (RuntimeException error) {
            throw new IOException("Invalid .bdimap.json mapping file " + normalized + ": " + error.getMessage(), error);
        }
    }
}

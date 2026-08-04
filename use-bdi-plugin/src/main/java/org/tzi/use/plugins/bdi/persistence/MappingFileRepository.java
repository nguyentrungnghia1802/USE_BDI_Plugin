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
        Objects.requireNonNull(file, "file");
        Objects.requireNonNull(document, "document");
        Path normalized = file.toAbsolutePath().normalize();
        Path parent = normalized.getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }
        Files.writeString(normalized, MappingJsonCodec.encode(document), StandardCharsets.UTF_8);
    }

    public MappingDocument load(Path file) throws IOException {
        Objects.requireNonNull(file, "file");
        String json = Files.readString(file.toAbsolutePath().normalize(), StandardCharsets.UTF_8);
        try {
            return MappingJsonCodec.decode(json);
        } catch (RuntimeException error) {
            throw new IOException("Invalid .bdimap.json mapping file: " + error.getMessage(), error);
        }
    }
}

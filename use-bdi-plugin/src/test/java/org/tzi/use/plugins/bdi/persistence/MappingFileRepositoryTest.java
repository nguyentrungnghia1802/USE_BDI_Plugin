package org.tzi.use.plugins.bdi.persistence;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.tzi.use.plugins.bdi.model.mapping.MappingBinding;
import org.tzi.use.plugins.bdi.model.mapping.MappingDocument;
import org.tzi.use.plugins.bdi.model.mapping.MappingKind;

class MappingFileRepositoryTest {
    @TempDir
    Path tempDir;

    @Test
    void savesAndLoadsTheMappingSchemaWithoutLosingEvidence() throws Exception {
        MappingDocument document = MappingDocument.empty("sha256-use")
                .upsert(new MappingBinding(
                        MappingKind.ACTION_OPERATION,
                        "agent.asl#plan:p#step:1",
                        "Queue::enqueue(item : String)",
                        Optional.of("argument[0]"),
                        List.of("Exact normalized name match", "Arity matches")))
                .upsert(new MappingBinding(
                        MappingKind.BELIEF_ATTRIBUTE,
                        "ready/0",
                        "Queue::ready"));
        Path file = tempDir.resolve("mapping.bdimap.json");

        MappingFileRepository repository = new MappingFileRepository();
        repository.save(file, document);
        String json = Files.readString(file);
        MappingDocument loaded = repository.load(file);

        assertTrue(json.contains("\"schemaVersion\":\"0.1.0\""));
        assertEquals(document, loaded);
    }

    @Test
    void rejectsMalformedMappingJson() throws Exception {
        Path file = tempDir.resolve("broken.bdimap.json");
        Files.writeString(file, "{\"schemaVersion\":");
        assertThrows(java.io.IOException.class, () -> new MappingFileRepository().load(file));
    }

    @Test
    void rejectsUnsupportedSchemaAndDuplicateJsonKeys() throws Exception {
        Path unsupported = tempDir.resolve("unsupported.bdimap.json");
        Files.writeString(unsupported, "{\"schemaVersion\":\"9.9.9\","
                + "\"bdiMetamodelVersion\":\"0.1.0\",\"useFingerprint\":\"x\","
                + "\"bindings\":[]}");
        Path duplicate = tempDir.resolve("duplicate.bdimap.json");
        Files.writeString(duplicate, "{\"schemaVersion\":\"0.1.0\",\"schemaVersion\":null}");

        MappingFileRepository repository = new MappingFileRepository();
        assertThrows(java.io.IOException.class, () -> repository.load(unsupported));
        assertThrows(java.io.IOException.class, () -> repository.load(duplicate));
    }
}

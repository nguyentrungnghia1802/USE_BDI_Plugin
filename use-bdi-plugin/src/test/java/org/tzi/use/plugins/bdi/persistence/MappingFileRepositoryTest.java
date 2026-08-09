package org.tzi.use.plugins.bdi.persistence;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.tzi.use.plugins.bdi.model.mapping.MappingBinding;
import org.tzi.use.plugins.bdi.model.mapping.MappingDocument;
import org.tzi.use.plugins.bdi.model.mapping.MappingKind;
import org.tzi.use.plugins.bdi.model.source.ProjectSourceId;

class MappingFileRepositoryTest {
    @TempDir
    Path tempDir;

    @Test
    void migratesLegacyMappingAndProducesByteStableV2() throws Exception {
        Path root = Files.createDirectories(tempDir.resolve("project")).toAbsolutePath();
        String actionSource = root.resolve("agents/bidder#1.asl").toString().replace('\\', '/')
                + "#plan:bid#step:1";
        Path legacy = tempDir.resolve("legacy.bdimap.json");
        Files.writeString(legacy, legacyMapping(actionSource));

        MappingFileRepository repository = new MappingFileRepository();
        MappingDocument migrated = repository.load(legacy, root);
        assertEquals(MappingDocument.CURRENT_SCHEMA_VERSION, migrated.schemaVersion());
        assertEquals(actionSource, migrated.bindings().get(0).source());

        Path first = tempDir.resolve("first.bdimap.json");
        Path second = tempDir.resolve("second.bdimap.json");
        repository.save(first, migrated, root);
        MappingDocument roundTripped = repository.load(first, root);
        repository.save(second, roundTripped, root);

        String encoded = Files.readString(first);
        assertTrue(encoded.contains("\"schemaVersion\":\"0.2.0\""));
        assertTrue(encoded.contains(ProjectSourceId.VERSION));
        assertTrue(encoded.contains("bidder%231.asl"));
        assertEquals(encoded, Files.readString(second));
        assertEquals(migrated, roundTripped);
    }

    @Test
    void preservesPathIndependentBindingsAndEvidence() throws Exception {
        Path root = tempDir.toAbsolutePath();
        MappingDocument document = MappingDocument.empty("sha256-use")
                .upsert(new MappingBinding(
                        MappingKind.BELIEF_ATTRIBUTE,
                        "ready/0",
                        "Queue::ready",
                        Optional.empty(),
                        List.of("Exact signature")));
        Path file = tempDir.resolve("mapping.bdimap.json");

        MappingFileRepository repository = new MappingFileRepository();
        repository.save(file, document, root);

        assertEquals(document, repository.load(file, root));
    }

    @Test
    void rejectsMalformedVersionAndAmbiguousLegacyPathWithFileEvidence() throws Exception {
        Path root = tempDir.toAbsolutePath();
        Path unsupported = tempDir.resolve("unsupported.bdimap.json");
        Files.writeString(unsupported, legacyMapping("agent.asl#plan:p#step:1")
                .replace("0.1.0", "9.9.9"));
        Path ambiguous = tempDir.resolve("ambiguous.bdimap.json");
        Files.writeString(ambiguous, legacyMapping("agent.asl#plan:p#step:1"));

        MappingFileRepository repository = new MappingFileRepository();
        IOException unsupportedError = assertThrows(IOException.class, () -> repository.load(unsupported, root));
        IOException ambiguousError = assertThrows(IOException.class, () -> repository.load(ambiguous, root));

        assertTrue(unsupportedError.getMessage().contains(unsupported.toAbsolutePath().toString()));
        assertTrue(ambiguousError.getMessage().contains("Ambiguous legacy mapping source"));
        assertTrue(ambiguousError.getMessage().contains(ambiguous.toAbsolutePath().toString()));
    }

    @Test
    void invalidOrMissingRootDoesNotOverwriteDestination() throws Exception {
        Path file = Files.writeString(tempDir.resolve("mapping.bdimap.json"), "original");
        MappingDocument document = MappingDocument.empty("fingerprint");
        MappingFileRepository repository = new MappingFileRepository();

        IOException missing = assertThrows(IOException.class, () -> repository.save(file, document));
        IOException invalid = assertThrows(
                IOException.class,
                () -> repository.save(file, document, tempDir.resolve("missing").toAbsolutePath()));

        assertTrue(missing.getMessage().contains("explicit project root"));
        assertTrue(invalid.getMessage().contains("existing directory"));
        assertEquals("original", Files.readString(file));
    }

    private static String legacyMapping(String source) {
        return "{\"schemaVersion\":\"0.1.0\","
                + "\"bdiMetamodelVersion\":\"0.1.0\",\"useFingerprint\":\"x\","
                + "\"bindings\":[{\"kind\":\"ACTION_OPERATION\",\"source\":"
                + MappingJsonCodec.quote(source)
                + ",\"target\":\"Auction::bid()\",\"expression\":null,\"evidence\":[]}]}";
    }
}

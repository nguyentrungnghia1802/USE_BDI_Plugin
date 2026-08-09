package org.tzi.use.plugins.bdi.persistence;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.tzi.use.plugins.bdi.model.ir.SourceSpan;
import org.tzi.use.plugins.bdi.model.source.ProjectSourceId;
import org.tzi.use.plugins.bdi.validation.Suppression;

class SuppressionRepositoryTest {
    @TempDir
    Path tempDir;

    @Test
    void migratesLegacyEntriesWithoutBroadeningAndWritesByteStableV2() throws Exception {
        Path root = tempDir.toAbsolutePath();
        Path legacy = tempDir.resolve("legacy.json");
        String item = "{\"ruleId\":\"ASL-001\",\"sourceFingerprint\":\""
                + "a".repeat(64) + "\",\"reason\":\"reviewed legacy\"}";
        Files.writeString(legacy, "{\"schemaVersion\":\"0.1.0\",\"suppressions\":[" + item + "]}");

        SuppressionRepository repository = new SuppressionRepository();
        List<Suppression> migrated = repository.load(legacy, root);
        assertTrue(migrated.get(0).projectSourceId().isEmpty());

        Path first = tempDir.resolve("first.json");
        Path second = tempDir.resolve("second.json");
        repository.save(first, migrated, root);
        repository.save(second, repository.load(first, root), root);

        String encoded = Files.readString(first);
        assertTrue(encoded.contains("\"schemaVersion\":\"0.2.0\""));
        assertTrue(encoded.contains("\"identityVersion\":\"bdi-source-v1\""));
        assertTrue(encoded.contains("\"sourceId\":null"));
        assertEquals(encoded, Files.readString(second));
    }

    @Test
    void roundTripsPortableSuppressionsInDeterministicOrder() throws Exception {
        Path root = tempDir.toAbsolutePath();
        Suppression asl = Suppression.projectRelative(
                "ASL-001",
                ProjectSourceId.from(root, new SourceSpan(root.resolve("agents/a.asl"), 2, 1, 2, 4)),
                "known parser fixture");
        Suppression signature = Suppression.projectRelative(
                "SIG-003",
                ProjectSourceId.from(root, new SourceSpan(root.resolve("agents/b.asl"), 3, 1, 3, 5)),
                "unknown type is intentional");
        Path file = tempDir.resolve("suppressions.json");

        SuppressionRepository repository = new SuppressionRepository();
        repository.save(file, List.of(signature, asl), root);
        String json = Files.readString(file);

        assertTrue(json.indexOf("ASL-001") < json.indexOf("SIG-003"));
        assertEquals(List.of(asl, signature), repository.load(file, root));
    }

    @Test
    void rejectsUnknownFieldsAndMalformedPortableIdentity() throws Exception {
        Path root = tempDir.toAbsolutePath();
        Path unknownField = tempDir.resolve("unknown.json");
        Files.writeString(unknownField, "{\"schemaVersion\":\"0.1.0\","
                + "\"suppressions\":[],\"pattern\":\"\"}");
        Path malformed = tempDir.resolve("malformed.json");
        Files.writeString(malformed, "{\"schemaVersion\":\"0.2.0\",\"suppressions\":["
                + "{\"ruleId\":\"ASL-001\",\"identityVersion\":\"bdi-source-v2\","
                + "\"sourceFingerprint\":\"" + "a".repeat(64) + "\","
                + "\"sourceId\":\"broken\",\"reason\":\"bad fixture\"}]}");

        SuppressionRepository repository = new SuppressionRepository();
        assertThrows(IOException.class, () -> repository.load(unknownField, root));
        IOException error = assertThrows(IOException.class, () -> repository.load(malformed, root));
        assertTrue(error.getMessage().contains(malformed.toAbsolutePath().toString()));
        assertTrue(error.getMessage().contains("Unsupported project source identity"));
    }

    @Test
    void invalidRootDoesNotOverwriteDestination() throws Exception {
        Path file = Files.writeString(tempDir.resolve("suppressions.json"), "original");
        SuppressionRepository repository = new SuppressionRepository();

        IOException error = assertThrows(
                IOException.class,
                () -> repository.save(file, List.of(), tempDir.resolve("missing").toAbsolutePath()));

        assertTrue(error.getMessage().contains("existing directory"));
        assertEquals("original", Files.readString(file));
    }
}

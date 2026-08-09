package org.tzi.use.plugins.bdi.persistence;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.tzi.use.plugins.bdi.validation.Suppression;

class SuppressionRepositoryTest {
    @TempDir
    Path tempDir;

    @Test
    void savesAndLoadsSuppressionsInDeterministicOrder() throws Exception {
        Suppression asl = new Suppression("ASL-001", "a".repeat(64), "known parser fixture");
        Suppression signature = new Suppression("SIG-003", "b".repeat(64), "unknown type is intentional");
        Path file = tempDir.resolve("suppressions.json");

        SuppressionRepository repository = new SuppressionRepository();
        repository.save(file, List.of(signature, asl));
        String json = Files.readString(file);

        assertTrue(json.indexOf("ASL-001") < json.indexOf("SIG-003"));
        assertEquals(List.of(asl, signature), repository.load(file));
    }

    @Test
    void rejectsUnknownFieldsAndDuplicateEntries() throws Exception {
        Path unknownField = tempDir.resolve("unknown.json");
        Files.writeString(unknownField, "{\"schemaVersion\":\"0.1.0\","
                + "\"suppressions\":[],\"pattern\":\"\"}");
        Path duplicate = tempDir.resolve("duplicate.json");
        String item = "{\"ruleId\":\"ASL-001\",\"sourceFingerprint\":\""
                + "a".repeat(64) + "\",\"reason\":\"same\"}";
        Files.writeString(duplicate, "{\"schemaVersion\":\"0.1.0\",\"suppressions\":["
                + item + "," + item + "]}");

        SuppressionRepository repository = new SuppressionRepository();
        assertThrows(java.io.IOException.class, () -> repository.load(unknownField));
        assertThrows(java.io.IOException.class, () -> repository.load(duplicate));
    }
}

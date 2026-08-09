package org.tzi.use.plugins.bdi.persistence;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.tzi.use.plugins.bdi.validation.RuleConfiguration;

class RuleConfigurationRepositoryTest {
    @TempDir
    Path tempDir;

    @Test
    void savesAndLoadsDeterministicEnabledRuleConfiguration() throws Exception {
        RuleConfiguration configuration = RuleConfiguration.of(List.of("SIG-003", "ASL-001"));
        Path file = tempDir.resolve("rules.json");

        RuleConfigurationRepository repository = new RuleConfigurationRepository();
        repository.save(file, configuration);
        String json = Files.readString(file);

        assertTrue(json.indexOf("\"ASL-001\"") < json.indexOf("\"SIG-003\""));
        assertEquals(configuration, repository.load(file));
    }

    @Test
    void rejectsUnknownFieldsAndDuplicateRuleIds() throws Exception {
        Path unknownField = tempDir.resolve("unknown.json");
        Files.writeString(unknownField, "{\"schemaVersion\":\"0.1.0\","
                + "\"enabledRules\":[],\"disabledRules\":[]}");
        Path duplicate = tempDir.resolve("duplicate.json");
        Files.writeString(duplicate, "{\"schemaVersion\":\"0.1.0\","
                + "\"enabledRules\":[\"ASL-001\",\"ASL-001\"]}");

        RuleConfigurationRepository repository = new RuleConfigurationRepository();
        assertThrows(java.io.IOException.class, () -> repository.load(unknownField));
        assertThrows(java.io.IOException.class, () -> repository.load(duplicate));
    }
}

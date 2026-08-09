package org.tzi.use.plugins.bdi.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.tzi.use.plugins.bdi.persistence.RuleConfigurationRepository;
import org.tzi.use.plugins.bdi.persistence.SuppressionRepository;
import org.tzi.use.plugins.bdi.validation.RuleConfiguration;
import org.tzi.use.plugins.bdi.validation.Suppression;

class BdiProjectConfigurationLoaderTest {
    @Test
    void loadsRulesAndSuppressionsBesideUseModel(@TempDir Path tempDir) throws Exception {
        Path model = Files.writeString(tempDir.resolve("Auction.use"), "model Auction");
        Path configurationDirectory = Files.createDirectories(tempDir.resolve(".bdi-plugin"));
        RuleConfiguration rules = RuleConfiguration.of(List.of("ASL-001"));
        Suppression suppression = new Suppression("ASL-001", "0".repeat(64), "Reviewed fixture");
        new RuleConfigurationRepository().save(configurationDirectory.resolve("rules.json"), rules);
        new SuppressionRepository().save(
                configurationDirectory.resolve("suppressions.json"),
                List.of(suppression),
                tempDir.toAbsolutePath());

        BdiProjectConfiguration loaded = new BdiProjectConfigurationLoader().load(model);

        assertEquals(tempDir.toAbsolutePath().normalize(), loaded.projectRoot().orElseThrow());
        assertEquals(rules, loaded.rules());
        assertEquals(List.of(suppression), loaded.suppressions());
        assertTrue(loaded.rulesLoaded());
        assertTrue(loaded.suppressionsLoaded());
        assertTrue(loaded.summary().contains("1 rule(s) [project]"));
    }

    @Test
    void usesExplicitDefaultsWhenProjectFilesAreMissing(@TempDir Path tempDir) throws Exception {
        Path model = Files.writeString(tempDir.resolve("Auction.use"), "model Auction");

        BdiProjectConfiguration loaded = new BdiProjectConfigurationLoader().load(model);

        assertEquals(RuleConfiguration.standard(), loaded.rules());
        assertTrue(loaded.suppressions().isEmpty());
        assertFalse(loaded.rulesLoaded());
        assertFalse(loaded.suppressionsLoaded());
        assertTrue(loaded.summary().contains("[default]"));
    }

    @Test
    void rejectsMalformedProjectConfiguration(@TempDir Path tempDir) throws Exception {
        Path model = Files.writeString(tempDir.resolve("Auction.use"), "model Auction");
        Path configurationDirectory = Files.createDirectories(tempDir.resolve(".bdi-plugin"));
        Files.writeString(configurationDirectory.resolve("rules.json"), "{not-json}");

        IOException error = assertThrows(
                IOException.class,
                () -> new BdiProjectConfigurationLoader().load(model));

        assertTrue(error.getMessage().contains(".bdi-plugin"));
        assertTrue(error.getMessage().contains("rules.json"));
    }

    @Test
    void rejectsUnknownConfiguredRuleBeforeOpeningExplorer(@TempDir Path tempDir) throws Exception {
        Path model = Files.writeString(tempDir.resolve("Auction.use"), "model Auction");
        Path configurationDirectory = Files.createDirectories(tempDir.resolve(".bdi-plugin"));
        new RuleConfigurationRepository().save(
                configurationDirectory.resolve("rules.json"),
                RuleConfiguration.of(List.of("UNKNOWN-999")));

        IOException error = assertThrows(
                IOException.class,
                () -> new BdiProjectConfigurationLoader().load(model));

        assertTrue(error.getMessage().contains("unknown rule IDs"));
    }

    @Test
    void usesDefaultsWhenModelHasNoFilename() throws Exception {
        BdiProjectConfiguration loaded = new BdiProjectConfigurationLoader().loadModel(" ");

        assertTrue(loaded.projectRoot().isEmpty());
        assertEquals(RuleConfiguration.standard(), loaded.rules());
    }
}

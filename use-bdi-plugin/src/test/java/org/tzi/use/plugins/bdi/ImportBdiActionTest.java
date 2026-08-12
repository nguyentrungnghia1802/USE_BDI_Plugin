package org.tzi.use.plugins.bdi;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import javax.swing.JFileChooser;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.tzi.use.plugins.bdi.application.BdiProjectConfiguration;
import org.tzi.use.plugins.bdi.persistence.RuleConfigurationRepository;
import org.tzi.use.plugins.bdi.ui.BdiFileChooserSupport;
import org.tzi.use.plugins.bdi.validation.RuleConfiguration;
import org.tzi.use.uml.mm.MModel;
import org.tzi.use.uml.mm.ModelFactory;
import org.tzi.use.uml.sys.MSystem;

class ImportBdiActionTest {
    @Test
    void configuresMultiSelectAslFileChooser() {
        JFileChooser chooser = ImportBdiAction.createFileChooser();

        assertTrue(chooser.isMultiSelectionEnabled());
        assertEquals("AgentSpeak files (*.asl)", chooser.getFileFilter().getDescription());
        assertEquals(BdiFileChooserSupport.defaultDirectory().toFile(), chooser.getCurrentDirectory());
    }

    @Test
    void configuresSingleSelectJacamoProjectChooser() {
        JFileChooser chooser = ImportJaCaMoAction.createFileChooser();

        assertTrue(!chooser.isMultiSelectionEnabled());
        assertEquals("JaCaMo projects (*.jcm)", chooser.getFileFilter().getDescription());
        assertEquals(BdiFileChooserSupport.defaultDirectory().toFile(), chooser.getCurrentDirectory());
    }

    @Test
    void discoversConfigurationFromTheActiveUseModelFilename(@TempDir Path tempDir) throws Exception {
        Path configurationDirectory = Files.createDirectories(tempDir.resolve(".bdi-plugin"));
        RuleConfiguration expected = RuleConfiguration.of(List.of("ASL-001"));
        new RuleConfigurationRepository().save(configurationDirectory.resolve("rules.json"), expected);
        MModel model = new ModelFactory().createModel("Fixture");
        model.setFilename(tempDir.resolve("Fixture.use").toString());

        BdiProjectConfiguration loaded = ImportBdiAction.loadProjectConfiguration(new MSystem(model));

        assertEquals(tempDir.toAbsolutePath().normalize(), loaded.projectRoot().orElseThrow());
        assertEquals(expected, loaded.rules());
        assertTrue(loaded.rulesLoaded());
    }
}

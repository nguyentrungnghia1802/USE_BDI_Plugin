package org.tzi.use.plugins.bdi.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.tzi.use.plugins.bdi.model.mapping.MappingDocument;
import org.tzi.use.plugins.bdi.model.mapping.MappingKind;
import org.tzi.use.plugins.bdi.model.mapping.MappingSuggestion;

class MappingEditorPanelTest {
    @TempDir
    Path tempDir;

    @Test
    void appliesSuggestionAndPersistsManualMappingDocument() throws Exception {
        Path root = tempDir.toAbsolutePath();
        String source = root.resolve("agent.asl").toString().replace('\\', '/');
        MappingEditorPanel panel = new MappingEditorPanel(
                new org.tzi.use.plugins.bdi.persistence.MappingFileRepository(),
                Optional.of(root));
        panel.setDocument(MappingDocument.empty("fingerprint"));
        panel.setSuggestions(List.of(new MappingSuggestion(
                MappingKind.AGENT_CLASS,
                source,
                "ManagerAgent",
                1.0,
                List.of("Exact normalized name match"))));
        panel.suggestionsForTest().setSelectedIndex(0);
        panel.applySelectedSuggestionForTest();

        Path file = tempDir.resolve("mapping.bdimap.json");
        panel.save(file);
        MappingEditorPanel loaded = new MappingEditorPanel(
                new org.tzi.use.plugins.bdi.persistence.MappingFileRepository(),
                Optional.of(root));
        loaded.load(file);

        assertEquals(1, panel.tableForTest().getRowCount());
        assertEquals(1, loaded.tableForTest().getRowCount());
        assertEquals("ManagerAgent", loaded.document()
                .find(MappingKind.AGENT_CLASS, source).orElseThrow().target());
    }
}

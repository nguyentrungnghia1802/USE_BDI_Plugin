package org.tzi.use.plugins.bdi.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.Component;
import java.awt.Container;
import java.awt.Rectangle;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

import javax.swing.JSplitPane;
import javax.swing.SwingUtilities;

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

    @Test
    void keepsMappingFieldsReadableInANarrowExplorerTab() throws Exception {
        MappingEditorPanel panel = new MappingEditorPanel(
                new org.tzi.use.plugins.bdi.persistence.MappingFileRepository(),
                Optional.of(tempDir.toAbsolutePath()));

        SwingUtilities.invokeAndWait(() -> {
            panel.setSize(640, 520);
            layoutRecursively(panel);
        });

        assertEquals(JSplitPane.VERTICAL_SPLIT, panel.bodyForTest().getOrientation());
        for (Component field : List.of(panel.sourceForTest(), panel.targetForTest())) {
            Rectangle bounds = SwingUtilities.convertRectangle(field.getParent(), field.getBounds(), panel);
            assertTrue(bounds.width >= 400, "mapping field is too narrow for source identities");
            assertTrue(bounds.x + bounds.width <= panel.getWidth(), "mapping field is clipped");
        }
    }

    private static void layoutRecursively(Container container) {
        container.doLayout();
        for (Component component : container.getComponents()) {
            if (component instanceof Container child) {
                layoutRecursively(child);
            }
        }
    }
}

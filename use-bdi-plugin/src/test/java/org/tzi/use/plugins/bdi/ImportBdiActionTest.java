package org.tzi.use.plugins.bdi;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import javax.swing.JFileChooser;

import org.junit.jupiter.api.Test;

class ImportBdiActionTest {
    @Test
    void configuresMultiSelectAslFileChooser() {
        JFileChooser chooser = ImportBdiAction.createFileChooser();

        assertTrue(chooser.isMultiSelectionEnabled());
        assertEquals("AgentSpeak files (*.asl)", chooser.getFileFilter().getDescription());
    }
}

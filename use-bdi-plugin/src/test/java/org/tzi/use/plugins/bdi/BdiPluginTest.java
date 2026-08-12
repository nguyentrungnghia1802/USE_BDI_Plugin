package org.tzi.use.plugins.bdi;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.tzi.use.config.Options;
import org.tzi.use.plugins.bdi.ui.BdiFileChooserSupport;

class BdiPluginTest {
    @Test
    void initializesUseFileDialogsAtThePluginDefaultDirectory() throws Exception {
        Path previous = Options.getLastDirectory();
        try {
            Options.setLastDirectory(Path.of("C:\\temporary\\previous-directory"));

            new BdiPlugin().run(null);

            assertEquals(BdiFileChooserSupport.defaultDirectory(), Options.getLastDirectory());
        } finally {
            Options.setLastDirectory(previous);
        }
    }
}

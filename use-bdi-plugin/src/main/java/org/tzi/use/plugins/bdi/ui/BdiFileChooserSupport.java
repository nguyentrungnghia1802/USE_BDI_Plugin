package org.tzi.use.plugins.bdi.ui;

import java.nio.file.Files;
import java.nio.file.Path;

import javax.swing.JFileChooser;

/** Shared file-dialog defaults for plugin-owned import, mapping, and report actions. */
public final class BdiFileChooserSupport {
    private static final Path PREFERRED_REPOSITORY_ROOT = Path.of(
            "D:\\_CODE_BANK\\Project_\\vnu-sme-lab\\use");

    private BdiFileChooserSupport() {
    }

    /**
     * Creates a chooser rooted at the checked-out USE repository when it is available.
     * A moved clone falls back to the process working directory rather than failing import.
     */
    public static JFileChooser create() {
        return new JFileChooser(defaultDirectory().toFile());
    }

    public static Path defaultDirectory() {
        if (Files.isDirectory(PREFERRED_REPOSITORY_ROOT)) {
            return PREFERRED_REPOSITORY_ROOT;
        }
        return Path.of(System.getProperty("user.dir")).toAbsolutePath().normalize();
    }
}

package org.tzi.use.plugins.bdi.ui;

import java.nio.file.Files;
import java.nio.file.Path;

import javax.swing.JFileChooser;

/** Shared file-dialog defaults for plugin-owned import, mapping, and report actions. */
public final class BdiFileChooserSupport {
    private BdiFileChooserSupport() {
    }

    /**
     * Creates a chooser rooted at the current USE checkout when it can be inferred.
     * Installed distributions fall back to the process working directory.
     */
    public static JFileChooser create() {
        return new JFileChooser(defaultDirectory().toFile());
    }

    public static Path defaultDirectory() {
        Path workingDirectory = Path.of(System.getProperty("user.dir"))
                .toAbsolutePath()
                .normalize();
        return defaultDirectory(workingDirectory);
    }

    static Path defaultDirectory(Path workingDirectory) {
        Path normalizedWorkingDirectory = workingDirectory.toAbsolutePath().normalize();
        for (Path candidate = normalizedWorkingDirectory;
                candidate != null;
                candidate = candidate.getParent()) {
            if (Files.isRegularFile(candidate.resolve("pom.xml"))
                    && Files.isDirectory(candidate.resolve("use-bdi-plugin"))) {
                return candidate;
            }
        }
        return normalizedWorkingDirectory;
    }
}

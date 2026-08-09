package org.tzi.use.plugins.bdi.persistence;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

/** Validates the explicit root required by portable persistence schemas. */
final class ProjectRootPolicy {
    private ProjectRootPolicy() {
    }

    static Path requireExistingAbsoluteDirectory(Path projectRoot) {
        Objects.requireNonNull(projectRoot, "projectRoot");
        if (!projectRoot.isAbsolute()) {
            throw new IllegalArgumentException("Project root must be an absolute path: " + projectRoot);
        }
        Path normalized = projectRoot.normalize();
        if (!Files.isDirectory(normalized)) {
            throw new IllegalArgumentException("Project root must be an existing directory: " + normalized);
        }
        return normalized;
    }
}

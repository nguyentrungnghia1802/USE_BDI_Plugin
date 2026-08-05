package org.tzi.use.plugins.bdi.problems;

import java.nio.file.Path;
import java.util.Objects;

/** A stable, UI-friendly problem row assembled from import and index evidence. */
public record BdiProblem(
        String code,
        BdiProblemSeverity severity,
        Path source,
        int line,
        int column,
        String message,
        String group) {
    public BdiProblem {
        if (code == null || code.isBlank()) {
            throw new IllegalArgumentException("code must not be blank");
        }
        Objects.requireNonNull(severity, "severity");
        source = Objects.requireNonNull(source, "source").toAbsolutePath().normalize();
        if (line < 0 || column < 0 || (line == 0) != (column == 0)) {
            throw new IllegalArgumentException("line and column must both be positive or both be zero");
        }
        if (message == null || message.isBlank()) {
            throw new IllegalArgumentException("message must not be blank");
        }
        if (group == null || group.isBlank()) {
            throw new IllegalArgumentException("group must not be blank");
        }
    }

    public String location() {
        return line == 0 ? "unknown" : line + ":" + column;
    }
}

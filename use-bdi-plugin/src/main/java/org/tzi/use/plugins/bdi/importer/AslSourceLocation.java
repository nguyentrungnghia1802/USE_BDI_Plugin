package org.tzi.use.plugins.bdi.importer;

import java.nio.file.Path;
import java.util.Objects;

public record AslSourceLocation(
        Path source,
        AslSourceElement element,
        String subject,
        int beginLine,
        int endLine) {
    public static final int UNKNOWN_POSITION = 0;

    public AslSourceLocation {
        Objects.requireNonNull(source, "source");
        Objects.requireNonNull(element, "element");
        Objects.requireNonNull(subject, "subject");
        if (beginLine < 0 || endLine < 0
                || (beginLine == UNKNOWN_POSITION) != (endLine == UNKNOWN_POSITION)
                || (beginLine > UNKNOWN_POSITION && endLine < beginLine)) {
            throw new IllegalArgumentException(
                    "Source lines must both be zero or positive with endLine >= beginLine");
        }
    }

    public boolean hasSourcePosition() {
        return beginLine > UNKNOWN_POSITION;
    }
}

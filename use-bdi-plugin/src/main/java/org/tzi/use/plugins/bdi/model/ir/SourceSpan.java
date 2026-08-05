package org.tzi.use.plugins.bdi.model.ir;

import java.nio.file.Path;
import java.util.Objects;

/** Source evidence shared by every normalized IR node. */
public record SourceSpan(
        Path source,
        int beginLine,
        int beginColumn,
        int endLine,
        int endColumn) {
    public static final int UNKNOWN_POSITION = 0;

    public SourceSpan {
        source = Objects.requireNonNull(source, "source").toAbsolutePath().normalize();
        if (beginLine < 0 || beginColumn < 0 || endLine < 0 || endColumn < 0) {
            throw new IllegalArgumentException("Source positions must not be negative");
        }
        if ((beginLine == UNKNOWN_POSITION) != (endLine == UNKNOWN_POSITION)) {
            throw new IllegalArgumentException("Begin and end lines must both be known or unknown");
        }
        if (beginLine == UNKNOWN_POSITION && (beginColumn != UNKNOWN_POSITION || endColumn != UNKNOWN_POSITION)) {
            throw new IllegalArgumentException("Unknown lines require unknown columns");
        }
        if ((beginColumn == UNKNOWN_POSITION) != (endColumn == UNKNOWN_POSITION)) {
            throw new IllegalArgumentException("Begin and end columns must both be known or unknown");
        }
        if (endLine > UNKNOWN_POSITION && endLine < beginLine) {
            throw new IllegalArgumentException("End line must not precede begin line");
        }
        if (beginColumn > UNKNOWN_POSITION && beginLine == endLine && endColumn < beginColumn) {
            throw new IllegalArgumentException("End column must not precede begin column");
        }
    }

    public static SourceSpan unknown(Path source) {
        return new SourceSpan(source, UNKNOWN_POSITION, UNKNOWN_POSITION,
                UNKNOWN_POSITION, UNKNOWN_POSITION);
    }

    public boolean hasLinePosition() {
        return beginLine > UNKNOWN_POSITION;
    }

    public boolean hasColumnPosition() {
        return beginColumn > UNKNOWN_POSITION;
    }
}

package org.tzi.use.plugins.bdi.importer;

import java.nio.file.Path;
import java.util.Objects;

public record AslDiagnostic(
        String code,
        AslDiagnosticSeverity severity,
        Path source,
        int line,
        int column,
        String message) {
    public static final String SYNTAX_ERROR_CODE = "ASL-001";
    public static final int UNKNOWN_POSITION = 0;

    public AslDiagnostic {
        Objects.requireNonNull(code, "code");
        Objects.requireNonNull(severity, "severity");
        Objects.requireNonNull(source, "source");
        Objects.requireNonNull(message, "message");
        if (line < 0 || column < 0 || (line == 0) != (column == 0)) {
            throw new IllegalArgumentException("Line and column must both be positive or both be zero");
        }
    }

    public boolean hasSourcePosition() {
        return line > UNKNOWN_POSITION;
    }
}

package org.tzi.use.plugins.bdi.importer;

import java.nio.file.Path;
import java.util.Objects;

public record MasProjectDiagnostic(
        String code,
        MasProjectDiagnosticSeverity severity,
        Path source,
        int line,
        int column,
        String message) {
    public static final String PARSE_ERROR = "JCM-001";
    public static final String MISSING_AGENT_SOURCE = "JCM-002";
    public static final String DUPLICATE_AGENT = "JCM-003";
    public static final String INVALID_AGENT_SOURCE = "JCM-004";
    public static final String UNSUPPORTED_RESOURCE = "JCM-005";
    public static final String SOURCE_OUTSIDE_ROOT = "JCM-006";
    public static final String INVALID_ORGANIZATION = "JCM-007";
    public static final String MISSING_ORGANIZATION = "JCM-008";
    public static final String DUPLICATE_ORGANIZATION = "JCM-009";
    public static final String UNSUPPORTED_ORGANIZATION_FEATURE = "JCM-010";

    public MasProjectDiagnostic {
        if (code == null || code.isBlank()) {
            throw new IllegalArgumentException("code must not be blank");
        }
        severity = Objects.requireNonNull(severity, "severity");
        source = Objects.requireNonNull(source, "source").toAbsolutePath().normalize();
        if (line < 0 || column < 0 || (line == 0) != (column == 0)) {
            throw new IllegalArgumentException("line and column must both be positive or both zero");
        }
        message = Objects.requireNonNull(message, "message");
    }
}

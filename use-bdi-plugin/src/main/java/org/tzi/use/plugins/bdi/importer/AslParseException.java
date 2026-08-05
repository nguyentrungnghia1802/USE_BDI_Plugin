package org.tzi.use.plugins.bdi.importer;

import java.util.Objects;
import java.util.Optional;

public final class AslParseException extends Exception {
    private static final long serialVersionUID = 1L;
    private final AslDiagnostic diagnostic;

    public AslParseException(String message) {
        super(message);
        diagnostic = null;
    }

    public AslParseException(String message, Throwable cause) {
        super(message, cause);
        diagnostic = null;
    }

    public AslParseException(AslDiagnostic diagnostic, Throwable cause) {
        super(Objects.requireNonNull(diagnostic, "diagnostic").message(), cause);
        this.diagnostic = diagnostic;
    }

    public Optional<AslDiagnostic> diagnostic() {
        return Optional.ofNullable(diagnostic);
    }
}

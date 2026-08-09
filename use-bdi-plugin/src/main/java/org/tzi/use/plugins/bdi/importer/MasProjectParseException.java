package org.tzi.use.plugins.bdi.importer;

public final class MasProjectParseException extends Exception {
    private static final long serialVersionUID = 1L;
    private final MasProjectDiagnostic diagnostic;

    public MasProjectParseException(MasProjectDiagnostic diagnostic, Throwable cause) {
        super(diagnostic.message(), cause);
        this.diagnostic = diagnostic;
    }

    public MasProjectDiagnostic diagnostic() {
        return diagnostic;
    }
}

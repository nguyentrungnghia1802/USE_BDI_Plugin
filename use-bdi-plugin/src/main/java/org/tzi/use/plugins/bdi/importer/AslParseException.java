package org.tzi.use.plugins.bdi.importer;

public final class AslParseException extends Exception {
    private static final long serialVersionUID = 1L;

    public AslParseException(String message) {
        super(message);
    }

    public AslParseException(String message, Throwable cause) {
        super(message, cause);
    }
}

package org.tzi.use.plugins.bdi.cli;

/** Expected invalid file/config input with evidence suitable for CI logs. */
public final class HeadlessInputException extends Exception {
    public HeadlessInputException(String message) {
        super(message);
    }

    public HeadlessInputException(String message, Throwable cause) {
        super(message, cause);
    }
}

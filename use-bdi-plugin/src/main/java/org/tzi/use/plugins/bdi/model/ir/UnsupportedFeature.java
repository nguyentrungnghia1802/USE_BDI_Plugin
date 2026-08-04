package org.tzi.use.plugins.bdi.model.ir;

import java.util.Objects;

/** Evidence for syntax that was recognized but is not normalized yet. */
public record UnsupportedFeature(
        String code,
        String kind,
        String subject,
        SourceSpan sourceSpan) {
    public static final String CODE = "ASL-002";

    public UnsupportedFeature {
        if (code == null || code.isBlank()) {
            throw new IllegalArgumentException("code must not be blank");
        }
        if (kind == null || kind.isBlank()) {
            throw new IllegalArgumentException("kind must not be blank");
        }
        Objects.requireNonNull(subject, "subject");
        Objects.requireNonNull(sourceSpan, "sourceSpan");
    }

    public String message() {
        return "Unsupported AgentSpeak feature: " + kind + " (" + subject + ")";
    }
}

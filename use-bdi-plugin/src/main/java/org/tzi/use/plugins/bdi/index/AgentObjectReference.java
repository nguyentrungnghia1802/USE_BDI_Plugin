package org.tzi.use.plugins.bdi.index;

import java.util.Objects;

import org.tzi.use.plugins.bdi.model.ir.SourceSpan;

/** Syntactic agent/object reference retained for later USE resolution. */
public record AgentObjectReference(
        ReferenceKind kind,
        String name,
        String rendered,
        boolean dynamic,
        String origin,
        SourceSpan sourceSpan) {
    public AgentObjectReference {
        Objects.requireNonNull(kind, "kind");
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("name must not be blank");
        }
        if (rendered == null || rendered.isBlank()) {
            throw new IllegalArgumentException("rendered must not be blank");
        }
        if (origin == null || origin.isBlank()) {
            throw new IllegalArgumentException("origin must not be blank");
        }
        Objects.requireNonNull(sourceSpan, "sourceSpan");
    }

    public enum ReferenceKind {
        AGENT,
        OBJECT
    }
}

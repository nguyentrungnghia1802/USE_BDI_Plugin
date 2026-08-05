package org.tzi.use.plugins.bdi.index;

import java.util.Objects;

import org.tzi.use.plugins.bdi.model.ir.SourceSpan;

/** A source-traceable occurrence of a predicate-like term in the IR. */
public record PredicateReference(
        PredicateSignature signature,
        PredicateReferenceKind kind,
        String planLabel,
        String rendered,
        SourceSpan sourceSpan) {
    public PredicateReference {
        Objects.requireNonNull(signature, "signature");
        Objects.requireNonNull(kind, "kind");
        Objects.requireNonNull(planLabel, "planLabel");
        if (rendered == null || rendered.isBlank()) {
            throw new IllegalArgumentException("rendered must not be blank");
        }
        Objects.requireNonNull(sourceSpan, "sourceSpan");
    }

    public enum PredicateReferenceKind {
        INITIAL_BELIEF,
        INITIAL_GOAL,
        PLAN_TRIGGER,
        PLAN_CONTEXT,
        ACTION,
        INTERNAL_ACTION,
        ACHIEVE_GOAL,
        TEST,
        CONSTRAINT,
        BELIEF_UPDATE
    }
}

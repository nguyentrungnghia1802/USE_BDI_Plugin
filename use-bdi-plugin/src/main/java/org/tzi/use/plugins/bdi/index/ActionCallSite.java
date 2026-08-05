package org.tzi.use.plugins.bdi.index;

import java.util.Objects;
import java.util.Optional;

import org.tzi.use.plugins.bdi.model.ir.SourceSpan;

/** A source-traceable action step in a plan. Step indexes are one-based. */
public record ActionCallSite(
        String planLabel,
        int stepIndex,
        ActionKind kind,
        String rendered,
        Optional<PredicateSignature> signature,
        SourceSpan sourceSpan) {
    public ActionCallSite {
        Objects.requireNonNull(planLabel, "planLabel");
        if (stepIndex < 1) {
            throw new IllegalArgumentException("stepIndex must be one-based");
        }
        Objects.requireNonNull(kind, "kind");
        if (rendered == null || rendered.isBlank()) {
            throw new IllegalArgumentException("rendered must not be blank");
        }
        signature = Objects.requireNonNull(signature, "signature");
        Objects.requireNonNull(sourceSpan, "sourceSpan");
    }

    public enum ActionKind {
        EXTERNAL_ACTION,
        INTERNAL_ACTION
    }
}

package org.tzi.use.plugins.bdi.model.ir;

import java.util.Objects;

public record BeliefModel(LiteralTermModel literal, SourceSpan sourceSpan) {
    public BeliefModel {
        Objects.requireNonNull(literal, "literal");
        Objects.requireNonNull(sourceSpan, "sourceSpan");
    }
}

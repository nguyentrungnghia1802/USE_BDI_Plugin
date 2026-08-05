package org.tzi.use.plugins.bdi.model.ir;

import java.util.Objects;

public record GoalModel(LiteralTermModel literal, SourceSpan sourceSpan) {
    public GoalModel {
        Objects.requireNonNull(literal, "literal");
        Objects.requireNonNull(sourceSpan, "sourceSpan");
    }
}

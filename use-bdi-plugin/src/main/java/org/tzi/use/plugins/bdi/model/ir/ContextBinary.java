package org.tzi.use.plugins.bdi.model.ir;

import java.util.Objects;

public record ContextBinary(
        String operator,
        ContextExpr left,
        ContextExpr right,
        SourceSpan sourceSpan) implements ContextExpr {
    public ContextBinary {
        if (operator == null || operator.isBlank()) {
            throw new IllegalArgumentException("operator must not be blank");
        }
        Objects.requireNonNull(left, "left");
        Objects.requireNonNull(right, "right");
        Objects.requireNonNull(sourceSpan, "sourceSpan");
    }
}

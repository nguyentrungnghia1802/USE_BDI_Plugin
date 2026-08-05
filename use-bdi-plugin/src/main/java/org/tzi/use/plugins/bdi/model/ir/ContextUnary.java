package org.tzi.use.plugins.bdi.model.ir;

import java.util.Objects;

public record ContextUnary(String operator, ContextExpr operand, SourceSpan sourceSpan) implements ContextExpr {
    public ContextUnary {
        if (operator == null || operator.isBlank()) {
            throw new IllegalArgumentException("operator must not be blank");
        }
        Objects.requireNonNull(operand, "operand");
        Objects.requireNonNull(sourceSpan, "sourceSpan");
    }
}

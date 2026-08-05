package org.tzi.use.plugins.bdi.model.ir;

import java.util.Objects;

public record ContextLiteral(LiteralTermModel literal, SourceSpan sourceSpan) implements ContextExpr {
    public ContextLiteral {
        Objects.requireNonNull(literal, "literal");
        Objects.requireNonNull(sourceSpan, "sourceSpan");
    }
}

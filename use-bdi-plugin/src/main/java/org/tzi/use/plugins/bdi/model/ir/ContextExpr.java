package org.tzi.use.plugins.bdi.model.ir;

public sealed interface ContextExpr
        permits ContextLiteral, ContextUnary, ContextBinary, ContextUnsupported {
    SourceSpan sourceSpan();
}

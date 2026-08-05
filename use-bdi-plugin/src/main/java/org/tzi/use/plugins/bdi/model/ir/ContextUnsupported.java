package org.tzi.use.plugins.bdi.model.ir;

import java.util.Objects;

public record ContextUnsupported(UnsupportedFeature feature) implements ContextExpr {
    public ContextUnsupported {
        Objects.requireNonNull(feature, "feature");
    }

    @Override
    public SourceSpan sourceSpan() {
        return feature.sourceSpan();
    }
}

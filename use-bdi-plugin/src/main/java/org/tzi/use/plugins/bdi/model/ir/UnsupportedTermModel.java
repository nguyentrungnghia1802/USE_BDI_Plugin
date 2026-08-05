package org.tzi.use.plugins.bdi.model.ir;

import java.util.Objects;

public record UnsupportedTermModel(UnsupportedFeature feature) implements TermModel {
    public UnsupportedTermModel {
        Objects.requireNonNull(feature, "feature");
    }

    @Override
    public SourceSpan sourceSpan() {
        return feature.sourceSpan();
    }

    @Override
    public String render() {
        return feature.subject();
    }
}

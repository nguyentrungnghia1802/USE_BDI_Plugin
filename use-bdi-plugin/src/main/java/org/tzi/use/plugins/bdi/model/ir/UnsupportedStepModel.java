package org.tzi.use.plugins.bdi.model.ir;

import java.util.Objects;

public record UnsupportedStepModel(UnsupportedFeature feature) implements PlanStepModel {
    public UnsupportedStepModel {
        Objects.requireNonNull(feature, "feature");
    }

    @Override
    public SourceSpan sourceSpan() {
        return feature.sourceSpan();
    }
}

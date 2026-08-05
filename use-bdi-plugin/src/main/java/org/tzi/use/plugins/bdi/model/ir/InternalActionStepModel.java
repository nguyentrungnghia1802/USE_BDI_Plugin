package org.tzi.use.plugins.bdi.model.ir;

import java.util.Objects;

public record InternalActionStepModel(TermModel action, SourceSpan sourceSpan) implements PlanStepModel {
    public InternalActionStepModel {
        Objects.requireNonNull(action, "action");
        Objects.requireNonNull(sourceSpan, "sourceSpan");
    }
}

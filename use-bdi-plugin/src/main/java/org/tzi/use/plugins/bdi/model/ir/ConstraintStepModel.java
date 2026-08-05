package org.tzi.use.plugins.bdi.model.ir;

import java.util.Objects;

public record ConstraintStepModel(ContextExpr condition, SourceSpan sourceSpan) implements PlanStepModel {
    public ConstraintStepModel {
        Objects.requireNonNull(condition, "condition");
        Objects.requireNonNull(sourceSpan, "sourceSpan");
    }
}

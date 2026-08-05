package org.tzi.use.plugins.bdi.model.ir;

import java.util.Objects;

public record TestStepModel(ContextExpr condition, SourceSpan sourceSpan) implements PlanStepModel {
    public TestStepModel {
        Objects.requireNonNull(condition, "condition");
        Objects.requireNonNull(sourceSpan, "sourceSpan");
    }
}

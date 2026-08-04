package org.tzi.use.plugins.bdi.model.ir;

import java.util.Objects;

public record ActionStepModel(TermModel action, SourceSpan sourceSpan) implements PlanStepModel {
    public ActionStepModel {
        Objects.requireNonNull(action, "action");
        Objects.requireNonNull(sourceSpan, "sourceSpan");
    }
}

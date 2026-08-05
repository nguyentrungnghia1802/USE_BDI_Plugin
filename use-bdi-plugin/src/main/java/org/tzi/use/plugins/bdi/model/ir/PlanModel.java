package org.tzi.use.plugins.bdi.model.ir;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

public record PlanModel(
        String label,
        TriggerModel trigger,
        Optional<ContextExpr> context,
        List<PlanStepModel> steps,
        SourceSpan sourceSpan) {
    public PlanModel {
        Objects.requireNonNull(label, "label");
        Objects.requireNonNull(trigger, "trigger");
        context = Objects.requireNonNull(context, "context");
        steps = List.copyOf(Objects.requireNonNull(steps, "steps"));
        Objects.requireNonNull(sourceSpan, "sourceSpan");
    }
}

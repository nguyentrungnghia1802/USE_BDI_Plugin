package org.tzi.use.plugins.bdi.model.ir;

import java.util.Objects;

public record AchieveGoalStepModel(
        LiteralTermModel goal,
        boolean newFocus,
        SourceSpan sourceSpan) implements PlanStepModel {
    public AchieveGoalStepModel {
        Objects.requireNonNull(goal, "goal");
        Objects.requireNonNull(sourceSpan, "sourceSpan");
    }
}

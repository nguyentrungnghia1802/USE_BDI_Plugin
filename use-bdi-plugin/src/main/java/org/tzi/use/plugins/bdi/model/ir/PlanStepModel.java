package org.tzi.use.plugins.bdi.model.ir;

public sealed interface PlanStepModel
        permits ActionStepModel, InternalActionStepModel, AchieveGoalStepModel,
        TestStepModel, BeliefUpdateStepModel, ConstraintStepModel, UnsupportedStepModel {
    SourceSpan sourceSpan();
}

package org.tzi.use.plugins.bdi.model.ir;

import java.util.Objects;

public record BeliefUpdateStepModel(
        UpdateOperator operator,
        FocusPolicy focusPolicy,
        LiteralTermModel belief,
        SourceSpan sourceSpan) implements PlanStepModel {
    public BeliefUpdateStepModel {
        Objects.requireNonNull(operator, "operator");
        Objects.requireNonNull(focusPolicy, "focusPolicy");
        Objects.requireNonNull(belief, "belief");
        Objects.requireNonNull(sourceSpan, "sourceSpan");
    }

    public enum UpdateOperator {
        ADD,
        DELETE,
        DELETE_AND_ADD
    }

    public enum FocusPolicy {
        DEFAULT,
        NEW_FOCUS,
        BEGIN_FOCUS,
        END_FOCUS
    }
}

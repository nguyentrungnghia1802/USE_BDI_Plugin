package org.tzi.use.plugins.bdi.model.ir;

import java.util.Objects;

public record TriggerModel(
        TriggerOperator operator,
        TriggerType type,
        TermModel term,
        SourceSpan sourceSpan) {
    public TriggerModel {
        Objects.requireNonNull(operator, "operator");
        Objects.requireNonNull(type, "type");
        Objects.requireNonNull(term, "term");
        Objects.requireNonNull(sourceSpan, "sourceSpan");
    }

    public enum TriggerOperator {
        ADD,
        DELETE,
        GOAL_STATE
    }

    public enum TriggerType {
        BELIEF,
        ACHIEVE,
        TEST,
        SIGNAL
    }
}

package org.tzi.use.plugins.bdi.use;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

public record OclEvaluationResult(
        String expression,
        OclEvaluationStatus status,
        Optional<String> value,
        Optional<String> valueType,
        List<String> diagnostics) {
    public OclEvaluationResult {
        Objects.requireNonNull(expression, "expression");
        Objects.requireNonNull(status, "status");
        value = Objects.requireNonNull(value, "value");
        valueType = Objects.requireNonNull(valueType, "valueType");
        diagnostics = List.copyOf(Objects.requireNonNull(diagnostics, "diagnostics"));
    }

    public boolean isEvaluated() {
        return status == OclEvaluationStatus.EVALUATED;
    }
}

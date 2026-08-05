package org.tzi.use.plugins.bdi.model.ir;

import java.util.Objects;
import java.util.Optional;

public record ArithmeticTermModel(
        String operator,
        Optional<TermModel> left,
        Optional<TermModel> right,
        SourceSpan sourceSpan) implements TermModel {
    public ArithmeticTermModel {
        if (operator == null || operator.isBlank()) {
            throw new IllegalArgumentException("operator must not be blank");
        }
        left = Objects.requireNonNull(left, "left");
        right = Objects.requireNonNull(right, "right");
        Objects.requireNonNull(sourceSpan, "sourceSpan");
    }

    @Override
    public String render() {
        if (left.isEmpty()) {
            return operator + right.map(TermModel::render).orElse("");
        }
        if (right.isEmpty()) {
            return operator + left.map(TermModel::render).orElse("");
        }
        return left.orElseThrow().render() + operator + right.orElseThrow().render();
    }
}

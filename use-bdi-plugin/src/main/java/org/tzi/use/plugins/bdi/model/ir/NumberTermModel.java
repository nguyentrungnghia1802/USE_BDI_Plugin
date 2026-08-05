package org.tzi.use.plugins.bdi.model.ir;

import java.util.Objects;

public record NumberTermModel(String value, SourceSpan sourceSpan) implements TermModel {
    public NumberTermModel {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("value must not be blank");
        }
        Objects.requireNonNull(sourceSpan, "sourceSpan");
    }

    @Override
    public String render() {
        return value;
    }
}

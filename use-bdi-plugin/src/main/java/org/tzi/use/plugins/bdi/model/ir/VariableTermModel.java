package org.tzi.use.plugins.bdi.model.ir;

import java.util.Objects;

public record VariableTermModel(String name, SourceSpan sourceSpan) implements TermModel {
    public VariableTermModel {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("name must not be blank");
        }
        Objects.requireNonNull(sourceSpan, "sourceSpan");
    }

    @Override
    public String render() {
        return name;
    }
}

package org.tzi.use.plugins.bdi.model.ir;

import java.util.Objects;

public record StringTermModel(String value, SourceSpan sourceSpan) implements TermModel {
    public StringTermModel {
        Objects.requireNonNull(value, "value");
        Objects.requireNonNull(sourceSpan, "sourceSpan");
    }

    @Override
    public String render() {
        return "\"" + value.replace("\\", "\\\\").replace("\"", "\\\"") + "\"";
    }
}

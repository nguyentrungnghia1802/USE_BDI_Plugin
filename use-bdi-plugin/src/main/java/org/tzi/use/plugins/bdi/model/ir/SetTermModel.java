package org.tzi.use.plugins.bdi.model.ir;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public record SetTermModel(List<TermModel> elements, SourceSpan sourceSpan) implements TermModel {
    public SetTermModel {
        elements = List.copyOf(Objects.requireNonNull(elements, "elements"));
        Objects.requireNonNull(sourceSpan, "sourceSpan");
    }

    @Override
    public String render() {
        return elements.stream().map(TermModel::render).collect(Collectors.joining(",", "{", "}"));
    }
}

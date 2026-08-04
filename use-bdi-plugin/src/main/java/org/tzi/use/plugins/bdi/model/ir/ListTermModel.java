package org.tzi.use.plugins.bdi.model.ir;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

public record ListTermModel(
        List<TermModel> elements,
        Optional<TermModel> tail,
        SourceSpan sourceSpan) implements TermModel {
    public ListTermModel {
        elements = List.copyOf(Objects.requireNonNull(elements, "elements"));
        tail = Objects.requireNonNull(tail, "tail");
        Objects.requireNonNull(sourceSpan, "sourceSpan");
    }

    @Override
    public String render() {
        String values = elements.stream().map(TermModel::render).collect(Collectors.joining(","));
        return tail.map(term -> values.isEmpty()
                        ? "[|" + term.render() + "]"
                        : "[" + values + "|" + term.render() + "]")
                .orElse("[" + values + "]");
    }
}

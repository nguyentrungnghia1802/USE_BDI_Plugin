package org.tzi.use.plugins.bdi.model.ir;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public record LiteralTermModel(
        String functor,
        List<TermModel> arguments,
        boolean negated,
        List<TermModel> annotations,
        SourceSpan sourceSpan) implements TermModel {
    public LiteralTermModel {
        if (functor == null || functor.isBlank()) {
            throw new IllegalArgumentException("functor must not be blank");
        }
        arguments = List.copyOf(Objects.requireNonNull(arguments, "arguments"));
        annotations = List.copyOf(Objects.requireNonNull(annotations, "annotations"));
        Objects.requireNonNull(sourceSpan, "sourceSpan");
    }

    @Override
    public String render() {
        String prefix = negated ? "~" : "";
        String args = arguments.isEmpty()
                ? ""
                : arguments.stream().map(TermModel::render).collect(Collectors.joining(",", "(", ")"));
        String annots = annotations.isEmpty()
                ? ""
                : annotations.stream().map(TermModel::render).collect(Collectors.joining(",", "[", "]"));
        return prefix + functor + args + annots;
    }
}

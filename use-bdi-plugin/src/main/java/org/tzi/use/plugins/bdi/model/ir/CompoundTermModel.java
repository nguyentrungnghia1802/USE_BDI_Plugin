package org.tzi.use.plugins.bdi.model.ir;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public record CompoundTermModel(
        String functor,
        List<TermModel> arguments,
        SourceSpan sourceSpan) implements TermModel {
    public CompoundTermModel {
        if (functor == null || functor.isBlank()) {
            throw new IllegalArgumentException("functor must not be blank");
        }
        arguments = List.copyOf(Objects.requireNonNull(arguments, "arguments"));
        Objects.requireNonNull(sourceSpan, "sourceSpan");
    }

    @Override
    public String render() {
        return functor + arguments.stream()
                .map(TermModel::render)
                .collect(Collectors.joining(",", "(", ")"));
    }
}

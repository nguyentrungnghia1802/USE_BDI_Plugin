package org.tzi.use.plugins.bdi.model.mapping;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/** One user-confirmed BDI-to-UML mapping relation. */
public record MappingBinding(
        MappingKind kind,
        String source,
        String target,
        Optional<String> expression,
        List<String> evidence) {
    public MappingBinding {
        Objects.requireNonNull(kind, "kind");
        requireText(source, "source");
        requireText(target, "target");
        expression = Objects.requireNonNull(expression, "expression");
        evidence = List.copyOf(Objects.requireNonNull(evidence, "evidence"));
    }

    public MappingBinding(MappingKind kind, String source, String target) {
        this(kind, source, target, Optional.empty(), List.of());
    }

    public String key() {
        return kind.name() + "\u0000" + source;
    }

    private static void requireText(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(field + " must not be blank");
        }
    }
}

package org.tzi.use.plugins.bdi.model.mapping;

import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;

/** A deterministic candidate shown to the user before it becomes a binding. */
public record MappingSuggestion(
        MappingKind kind,
        String source,
        String target,
        double score,
        List<String> reasons,
        Optional<String> expression) {
    public MappingSuggestion {
        Objects.requireNonNull(kind, "kind");
        requireText(source, "source");
        requireText(target, "target");
        if (Double.isNaN(score) || score < 0.0 || score > 1.0) {
            throw new IllegalArgumentException("score must be between 0 and 1");
        }
        reasons = List.copyOf(Objects.requireNonNull(reasons, "reasons"));
        expression = Objects.requireNonNull(expression, "expression");
    }

    public MappingSuggestion(
            MappingKind kind,
            String source,
            String target,
            double score,
            List<String> reasons) {
        this(kind, source, target, score, reasons, Optional.empty());
    }

    public MappingBinding toBinding() {
        return new MappingBinding(kind, source, target, expression, reasons);
    }

    @Override
    public String toString() {
        return kind + ": " + source + " -> " + target
                + " (" + String.format(Locale.ROOT, "%.2f", score) + ")";
    }

    private static void requireText(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(field + " must not be blank");
        }
    }
}

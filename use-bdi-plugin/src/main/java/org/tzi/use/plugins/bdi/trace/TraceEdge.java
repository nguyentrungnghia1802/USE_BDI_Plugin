package org.tzi.use.plugins.bdi.trace;

import java.util.List;
import java.util.Objects;

import org.tzi.use.plugins.bdi.validation.IssueCertainty;

public record TraceEdge(
        String id,
        String from,
        String to,
        TraceRelationKind relation,
        IssueCertainty certainty,
        List<String> evidence) {
    public TraceEdge {
        requireText(id, "id");
        requireText(from, "from");
        requireText(to, "to");
        relation = Objects.requireNonNull(relation, "relation");
        certainty = Objects.requireNonNull(certainty, "certainty");
        evidence = List.copyOf(Objects.requireNonNull(evidence, "evidence"));
    }

    private static void requireText(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(field + " must not be blank");
        }
    }
}

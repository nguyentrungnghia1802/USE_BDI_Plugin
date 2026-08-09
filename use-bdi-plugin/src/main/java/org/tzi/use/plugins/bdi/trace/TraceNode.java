package org.tzi.use.plugins.bdi.trace;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.tzi.use.plugins.bdi.model.source.ProjectSourceId;
import org.tzi.use.plugins.bdi.validation.IssueCertainty;
import org.tzi.use.plugins.bdi.validation.IssueStatus;

public record TraceNode(
        String id,
        TraceNodeKind kind,
        String label,
        Optional<ProjectSourceId> source,
        Optional<IssueStatus> status,
        Optional<IssueCertainty> certainty,
        List<String> evidence) {
    public TraceNode {
        requireText(id, "id");
        kind = Objects.requireNonNull(kind, "kind");
        requireText(label, "label");
        source = Objects.requireNonNull(source, "source");
        status = Objects.requireNonNull(status, "status");
        certainty = Objects.requireNonNull(certainty, "certainty");
        evidence = List.copyOf(Objects.requireNonNull(evidence, "evidence"));
    }

    private static void requireText(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(field + " must not be blank");
        }
    }
}

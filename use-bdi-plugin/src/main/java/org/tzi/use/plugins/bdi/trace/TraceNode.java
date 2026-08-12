package org.tzi.use.plugins.bdi.trace;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.tzi.use.plugins.bdi.model.source.ProjectSourceId;
import org.tzi.use.plugins.bdi.validation.IssueCertainty;
import org.tzi.use.plugins.bdi.validation.IssueSeverity;
import org.tzi.use.plugins.bdi.validation.IssueStatus;

public record TraceNode(
        String id,
        TraceNodeKind kind,
        String label,
        Optional<ProjectSourceId> source,
        Optional<IssueStatus> status,
        Optional<IssueCertainty> certainty,
        Optional<String> ruleId,
        Optional<IssueSeverity> severity,
        List<String> evidence) {
    public TraceNode {
        requireText(id, "id");
        kind = Objects.requireNonNull(kind, "kind");
        requireText(label, "label");
        source = Objects.requireNonNull(source, "source");
        status = Objects.requireNonNull(status, "status");
        certainty = Objects.requireNonNull(certainty, "certainty");
        ruleId = Objects.requireNonNull(ruleId, "ruleId");
        ruleId.ifPresent(value -> requireText(value, "ruleId"));
        severity = Objects.requireNonNull(severity, "severity");
        evidence = List.copyOf(Objects.requireNonNull(evidence, "evidence"));
    }

    /** Compatibility constructor for non-issue trace nodes created by older callers. */
    public TraceNode(
            String id,
            TraceNodeKind kind,
            String label,
            Optional<ProjectSourceId> source,
            Optional<IssueStatus> status,
            Optional<IssueCertainty> certainty,
            List<String> evidence) {
        this(id, kind, label, source, status, certainty, Optional.empty(), Optional.empty(), evidence);
    }

    private static void requireText(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(field + " must not be blank");
        }
    }
}

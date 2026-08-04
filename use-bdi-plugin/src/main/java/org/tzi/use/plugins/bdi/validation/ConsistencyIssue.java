package org.tzi.use.plugins.bdi.validation;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.tzi.use.plugins.bdi.model.ir.SourceSpan;

/** Immutable rule result with the evidence needed by UI and reporting layers. */
public record ConsistencyIssue(
        String ruleId,
        IssueSeverity severity,
        IssueStatus status,
        String message,
        Optional<String> agentId,
        Optional<String> planId,
        Optional<SourceSpan> sourceSpan,
        Optional<String> umlElementRef,
        List<String> evidence,
        Optional<String> suggestedFix,
        IssueCertainty certainty) {
    public ConsistencyIssue {
        requireText(ruleId, "ruleId");
        Objects.requireNonNull(severity, "severity");
        Objects.requireNonNull(status, "status");
        requireText(message, "message");
        agentId = Objects.requireNonNull(agentId, "agentId");
        planId = Objects.requireNonNull(planId, "planId");
        sourceSpan = Objects.requireNonNull(sourceSpan, "sourceSpan");
        umlElementRef = Objects.requireNonNull(umlElementRef, "umlElementRef");
        evidence = List.copyOf(Objects.requireNonNull(evidence, "evidence"));
        suggestedFix = Objects.requireNonNull(suggestedFix, "suggestedFix");
        Objects.requireNonNull(certainty, "certainty");
    }

    private static void requireText(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(field + " must not be blank");
        }
    }
}

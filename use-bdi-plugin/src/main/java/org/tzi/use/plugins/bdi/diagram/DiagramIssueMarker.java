package org.tzi.use.plugins.bdi.diagram;

import java.util.List;
import java.util.Objects;

import org.tzi.use.plugins.bdi.validation.IssueCertainty;
import org.tzi.use.plugins.bdi.validation.IssueSeverity;
import org.tzi.use.plugins.bdi.validation.IssueStatus;

/** Issue state retained by a presentation node without carrying validator objects. */
public record DiagramIssueMarker(
        String ruleId,
        IssueSeverity severity,
        IssueStatus status,
        IssueCertainty certainty,
        List<String> evidence) {
    public DiagramIssueMarker {
        DiagramValues.requireText(ruleId, "ruleId");
        severity = Objects.requireNonNull(severity, "severity");
        status = Objects.requireNonNull(status, "status");
        certainty = Objects.requireNonNull(certainty, "certainty");
        evidence = DiagramValues.immutableTextList(evidence, "evidence");
    }
}

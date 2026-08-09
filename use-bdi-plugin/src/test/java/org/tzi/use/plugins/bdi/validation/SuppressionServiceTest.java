package org.tzi.use.plugins.bdi.validation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.tzi.use.plugins.bdi.model.ir.SourceSpan;

class SuppressionServiceTest {
    @Test
    void marksOnlyMatchingOpenIssueAsSuppressedWithReasonEvidence() {
        SourceSpan source = new SourceSpan(Path.of("agent.asl"), 4, 2, 4, 8);
        ConsistencyIssue issue = issue("ASL-001", source, IssueStatus.OPEN);
        Suppression suppression = new Suppression(
                "ASL-001",
                IssueFingerprint.forSource(source),
                "accepted baseline diagnostic");

        List<ConsistencyIssue> result = SuppressionService.apply(List.of(issue), List.of(suppression));

        assertEquals(IssueStatus.SUPPRESSED, result.get(0).status());
        assertTrue(result.get(0).evidence().contains("Suppression reason: accepted baseline diagnostic"));
    }

    @Test
    void rejectsDuplicateSuppressionKeys() {
        Suppression first = new Suppression("ASL-001", "a".repeat(64), "first");
        Suppression duplicate = new Suppression("ASL-001", "A".repeat(64), "duplicate");

        assertThrows(IllegalArgumentException.class, () -> SuppressionService.apply(
                List.of(), List.of(first, duplicate)));
    }

    private static ConsistencyIssue issue(String ruleId, SourceSpan source, IssueStatus status) {
        return new ConsistencyIssue(
                ruleId,
                IssueSeverity.ERROR,
                status,
                "test issue",
                Optional.empty(),
                Optional.empty(),
                Optional.of(source),
                Optional.empty(),
                List.of("source evidence"),
                Optional.empty(),
                IssueCertainty.CONFIRMED);
    }
}

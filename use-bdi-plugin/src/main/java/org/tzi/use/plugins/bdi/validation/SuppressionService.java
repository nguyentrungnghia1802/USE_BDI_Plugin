package org.tzi.use.plugins.bdi.validation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/** Applies persisted suppressions without mutating issue or model inputs. */
public final class SuppressionService {
    private SuppressionService() {
    }

    public static List<ConsistencyIssue> apply(
            List<ConsistencyIssue> issues,
            List<Suppression> suppressions) {
        Objects.requireNonNull(issues, "issues");
        Objects.requireNonNull(suppressions, "suppressions");
        Map<String, Suppression> byKey = new HashMap<>();
        for (Suppression suppression : suppressions) {
            Objects.requireNonNull(suppression, "suppression");
            if (byKey.put(suppression.key(), suppression) != null) {
                throw new IllegalArgumentException("Duplicate suppression: " + suppression.key());
            }
        }

        List<ConsistencyIssue> result = new ArrayList<>();
        for (ConsistencyIssue issue : issues) {
            Objects.requireNonNull(issue, "issue");
            Suppression suppression = byKey.get(issue.ruleId() + "\u0000" + IssueFingerprint.forIssue(issue));
            if (suppression == null || issue.status() != IssueStatus.OPEN) {
                result.add(issue);
                continue;
            }
            List<String> evidence = new ArrayList<>(issue.evidence());
            evidence.add("Suppression reason: " + suppression.reason());
            result.add(new ConsistencyIssue(
                    issue.ruleId(),
                    issue.severity(),
                    IssueStatus.SUPPRESSED,
                    issue.message(),
                    issue.agentId(),
                    issue.planId(),
                    issue.sourceSpan(),
                    issue.umlElementRef(),
                    evidence,
                    issue.suggestedFix(),
                    issue.certainty()));
        }
        return List.copyOf(result);
    }
}

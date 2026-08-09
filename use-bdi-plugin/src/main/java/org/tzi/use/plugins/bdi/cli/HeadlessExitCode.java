package org.tzi.use.plugins.bdi.cli;

import org.tzi.use.plugins.bdi.application.CurrentAnalysisSnapshot;
import org.tzi.use.plugins.bdi.validation.IssueCertainty;
import org.tzi.use.plugins.bdi.validation.IssueStatus;

/** Stable process exit contract for the headless quality gate. */
public enum HeadlessExitCode {
    CLEAN(0),
    CONFIRMED_FINDINGS(1),
    REVIEW_FINDINGS(2),
    INVALID_INPUT(3),
    INFRASTRUCTURE_FAILURE(4);

    private final int code;

    HeadlessExitCode(int code) {
        this.code = code;
    }

    public int code() {
        return code;
    }

    public static HeadlessExitCode forSnapshot(CurrentAnalysisSnapshot snapshot) {
        var open = snapshot.issues().stream()
                .filter(issue -> issue.status() == IssueStatus.OPEN)
                .toList();
        if (open.isEmpty()) {
            return CLEAN;
        }
        if (open.stream().anyMatch(issue -> issue.certainty() == IssueCertainty.CONFIRMED)) {
            return CONFIRMED_FINDINGS;
        }
        return REVIEW_FINDINGS;
    }
}

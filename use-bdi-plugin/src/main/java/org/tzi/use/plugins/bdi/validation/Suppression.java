package org.tzi.use.plugins.bdi.validation;

import java.util.Locale;
import java.util.Objects;
import java.util.regex.Pattern;

/** One user-confirmed suppression keyed by rule ID and source fingerprint. */
public record Suppression(String ruleId, String sourceFingerprint, String reason) {
    private static final Pattern RULE_ID = Pattern.compile("[A-Z][A-Z0-9-]*");
    private static final Pattern SOURCE_FINGERPRINT = Pattern.compile("[0-9a-fA-F]{64}");

    public Suppression {
        if (ruleId == null || !RULE_ID.matcher(ruleId).matches()) {
            throw new IllegalArgumentException("Invalid suppression rule ID: " + ruleId);
        }
        if (sourceFingerprint == null || !SOURCE_FINGERPRINT.matcher(sourceFingerprint).matches()) {
            throw new IllegalArgumentException("sourceFingerprint must be a SHA-256 hex digest");
        }
        sourceFingerprint = sourceFingerprint.toLowerCase(Locale.ROOT);
        if (reason == null || reason.isBlank()) {
            throw new IllegalArgumentException("suppression reason must not be blank");
        }
    }

    public String key() {
        return ruleId + "\u0000" + sourceFingerprint;
    }

    public boolean matches(ConsistencyIssue issue) {
        Objects.requireNonNull(issue, "issue");
        return ruleId.equals(issue.ruleId())
                && sourceFingerprint.equals(IssueFingerprint.forIssue(issue));
    }
}

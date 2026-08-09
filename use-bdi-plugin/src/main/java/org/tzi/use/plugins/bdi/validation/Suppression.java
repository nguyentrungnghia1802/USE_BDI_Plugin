package org.tzi.use.plugins.bdi.validation;

import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Pattern;

import java.nio.file.Path;

import org.tzi.use.plugins.bdi.model.source.ProjectSourceId;

/** One user-confirmed suppression keyed by rule ID and source fingerprint. */
public record Suppression(
        String ruleId,
        String sourceFingerprint,
        String reason,
        Optional<ProjectSourceId> projectSourceId) {
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
        projectSourceId = Objects.requireNonNull(projectSourceId, "projectSourceId");
        if (projectSourceId.isPresent()
                && !sourceFingerprint.equals(IssueFingerprint.forProjectSource(projectSourceId.orElseThrow()))) {
            throw new IllegalArgumentException("sourceFingerprint does not match projectSourceId");
        }
    }

    public Suppression(String ruleId, String sourceFingerprint, String reason) {
        this(ruleId, sourceFingerprint, reason, Optional.empty());
    }

    public static Suppression projectRelative(String ruleId, ProjectSourceId sourceId, String reason) {
        Objects.requireNonNull(sourceId, "sourceId");
        return new Suppression(
                ruleId,
                IssueFingerprint.forProjectSource(sourceId),
                reason,
                Optional.of(sourceId));
    }

    public String key() {
        return ruleId + "\u0000" + identityVersion() + "\u0000" + sourceFingerprint;
    }

    public String identityVersion() {
        return projectSourceId.isPresent() ? ProjectSourceId.VERSION : "bdi-source-v1";
    }

    public boolean matches(ConsistencyIssue issue) {
        Objects.requireNonNull(issue, "issue");
        return projectSourceId.isEmpty()
                && ruleId.equals(issue.ruleId())
                && sourceFingerprint.equals(IssueFingerprint.forIssue(issue));
    }

    public boolean matches(ConsistencyIssue issue, Path projectRoot) {
        Objects.requireNonNull(issue, "issue");
        Objects.requireNonNull(projectRoot, "projectRoot");
        if (projectSourceId.isEmpty()) {
            return matches(issue);
        }
        return ruleId.equals(issue.ruleId())
                && issue.sourceSpan()
                        .map(span -> ProjectSourceId.from(projectRoot, span).equals(projectSourceId.orElseThrow()))
                        .orElse(false);
    }
}

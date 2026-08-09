package org.tzi.use.plugins.bdi.validation;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.Objects;

import org.tzi.use.plugins.bdi.model.ir.SourceSpan;
import org.tzi.use.plugins.bdi.model.source.ProjectSourceId;

/** Computes the stable source identity used by suppression entries. */
public final class IssueFingerprint {
    private IssueFingerprint() {
    }

    public static String forIssue(ConsistencyIssue issue) {
        Objects.requireNonNull(issue, "issue");
        return forSource(issue.sourceSpan().orElse(null));
    }

    public static String forSource(SourceSpan sourceSpan) {
        StringBuilder canonical = new StringBuilder();
        append(canonical, "bdi-source-v1");
        if (sourceSpan == null) {
            append(canonical, "unknown");
        } else {
            append(canonical, sourceSpan.source().toString().replace('\\', '/'));
            append(canonical, Integer.toString(sourceSpan.beginLine()));
            append(canonical, Integer.toString(sourceSpan.beginColumn()));
            append(canonical, Integer.toString(sourceSpan.endLine()));
            append(canonical, Integer.toString(sourceSpan.endColumn()));
        }
        return sha256(canonical.toString());
    }

    public static String forProjectSource(ProjectSourceId sourceId) {
        return sha256(Objects.requireNonNull(sourceId, "sourceId").canonical());
    }

    private static void append(StringBuilder canonical, String value) {
        canonical.append(value.length()).append(':').append(value).append('\n');
    }

    private static String sha256(String value) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
                    .digest(value.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException error) {
            throw new AssertionError("JVM must provide SHA-256", error);
        }
    }
}

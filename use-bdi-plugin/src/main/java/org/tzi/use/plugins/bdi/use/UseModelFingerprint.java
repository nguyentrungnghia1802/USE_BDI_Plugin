package org.tzi.use.plugins.bdi.use;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Map;

/** Computes a deterministic SHA-256 identity from normalized model/state data. */
public final class UseModelFingerprint {
    private UseModelFingerprint() {
    }

    public static String compute(UseModelSnapshot snapshot) {
        StringBuilder canonical = new StringBuilder();
        append(canonical, snapshot.modelName());
        append(canonical, snapshot.filename());
        snapshot.classes().forEach(value -> {
            append(canonical, "class");
            append(canonical, value.name());
            append(canonical, Boolean.toString(value.abstractType()));
            value.parentNames().forEach(parent -> append(canonical, parent));
        });
        snapshot.attributes().forEach(value -> {
            append(canonical, "attribute");
            append(canonical, value.reference());
            append(canonical, value.type());
            append(canonical, Boolean.toString(value.derived()));
            append(canonical, value.initExpression().orElse(""));
            append(canonical, value.deriveExpression().orElse(""));
        });
        snapshot.associations().forEach(value -> {
            append(canonical, "association");
            append(canonical, value.name());
            append(canonical, Boolean.toString(value.derived()));
            append(canonical, Boolean.toString(value.union()));
            value.ends().forEach(end -> {
                append(canonical, end.reference());
                append(canonical, end.className());
                append(canonical, end.multiplicity());
                append(canonical, end.aggregationKind());
                append(canonical, Boolean.toString(end.ordered()));
                append(canonical, Boolean.toString(end.navigable()));
                append(canonical, Boolean.toString(end.explicitNavigable()));
                append(canonical, Boolean.toString(end.derived()));
                append(canonical, Boolean.toString(end.union()));
            });
        });
        snapshot.operations().forEach(value -> {
            append(canonical, "operation");
            append(canonical, value.reference());
            value.parameters().forEach(parameter -> {
                append(canonical, parameter.name());
                append(canonical, parameter.type());
            });
            append(canonical, value.resultType().orElse(""));
            value.preconditions().forEach(constraint -> appendConstraint(canonical, constraint));
            value.postconditions().forEach(constraint -> appendConstraint(canonical, constraint));
            append(canonical, Boolean.toString(value.expressionBody()));
            append(canonical, Boolean.toString(value.statementBody()));
        });
        snapshot.classInvariants().forEach(constraint -> appendConstraint(canonical, constraint));
        snapshot.objects().forEach(value -> {
            append(canonical, "object");
            append(canonical, value.name());
            append(canonical, value.className());
            append(canonical, Boolean.toString(value.exists()));
            value.attributeValues().entrySet().stream()
                    .sorted(Map.Entry.comparingByKey())
                    .forEach(entry -> {
                        append(canonical, entry.getKey());
                        append(canonical, entry.getValue());
                    });
        });
        snapshot.links().forEach(value -> {
            append(canonical, "link");
            append(canonical, value.associationName());
            value.objectNames().forEach(object -> append(canonical, object));
            append(canonical, Boolean.toString(value.virtual()));
        });
        return sha256(canonical.toString());
    }

    private static void appendConstraint(StringBuilder canonical, UmlConstraintRef constraint) {
        append(canonical, constraint.reference());
        append(canonical, constraint.kind());
        append(canonical, constraint.expression());
    }

    private static void append(StringBuilder target, String value) {
        String text = value == null ? "" : value;
        target.append(text.length()).append(':').append(text).append('\n');
    }

    private static String sha256(String canonical) {
        try {
            return java.util.HexFormat.of().formatHex(
                    MessageDigest.getInstance("SHA-256")
                            .digest(canonical.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException error) {
            throw new AssertionError("JVM must provide SHA-256", error);
        }
    }
}

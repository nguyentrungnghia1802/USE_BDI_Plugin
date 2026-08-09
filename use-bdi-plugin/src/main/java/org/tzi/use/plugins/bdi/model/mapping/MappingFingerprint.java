package org.tzi.use.plugins.bdi.model.mapping;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Comparator;
import java.util.HexFormat;
import java.util.Objects;

/** Computes a deterministic SHA-256 identity for a mapping document. */
public final class MappingFingerprint {
    private MappingFingerprint() {
    }

    public static String compute(MappingDocument document) {
        Objects.requireNonNull(document, "document");
        StringBuilder canonical = new StringBuilder();
        append(canonical, "mapping-document");
        append(canonical, document.schemaVersion());
        append(canonical, document.bdiMetamodelVersion());
        append(canonical, document.useFingerprint());
        append(canonical, "binding-count");
        append(canonical, Integer.toString(document.bindings().size()));
        document.bindings().stream()
                .sorted(Comparator.comparing(MappingBinding::key))
                .forEach(binding -> {
                    append(canonical, "binding");
                    append(canonical, binding.kind().name());
                    append(canonical, binding.source());
                    append(canonical, binding.target());
                    append(canonical, "expression");
                    append(canonical, binding.expression().orElse(""));
                    append(canonical, "evidence-count");
                    append(canonical, Integer.toString(binding.evidence().size()));
                    binding.evidence().forEach(value -> {
                        append(canonical, "evidence");
                        append(canonical, value);
                    });
                });
        return sha256(canonical.toString());
    }

    private static void append(StringBuilder canonical, String value) {
        String text = value == null ? "" : value;
        canonical.append(text.length()).append(':').append(text).append('\n');
    }

    private static String sha256(String canonical) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
                    .digest(canonical.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException error) {
            throw new AssertionError("JVM must provide SHA-256", error);
        }
    }
}

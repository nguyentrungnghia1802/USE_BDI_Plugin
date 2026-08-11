package org.tzi.use.plugins.bdi.evaluation;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.List;

final class EvaluationHashing {
    private EvaluationHashing() {
    }

    static String sha256(byte[] bytes) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(bytes));
        } catch (NoSuchAlgorithmException error) {
            throw new IllegalStateException("SHA-256 is not available", error);
        }
    }

    static String sha256Text(String value) {
        return sha256(value.getBytes(StandardCharsets.UTF_8));
    }

    static String corpusHash(Path root, List<String> relativeFiles) throws IOException {
        MessageDigest digest = algorithm();
        for (String relative : relativeFiles.stream().sorted().toList()) {
            Path file = resolve(root, relative);
            byte[] bytes = Files.readAllBytes(file);
            digest.update(relative.replace('\\', '/').getBytes(StandardCharsets.UTF_8));
            digest.update((byte) 0);
            digest.update(bytes);
            digest.update((byte) 0);
        }
        return HexFormat.of().formatHex(digest.digest());
    }

    static Path resolve(Path root, String relative) {
        Path normalizedRoot = root.toAbsolutePath().normalize();
        Path resolved = normalizedRoot.resolve(relative.replace('/', java.io.File.separatorChar)).normalize();
        if (!resolved.startsWith(normalizedRoot)) {
            throw new IllegalArgumentException("Path escapes evaluation root: " + relative);
        }
        return resolved;
    }

    private static MessageDigest algorithm() {
        try {
            return MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException error) {
            throw new IllegalStateException("SHA-256 is not available", error);
        }
    }
}

package org.tzi.use.plugins.bdi.model.source;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.tzi.use.plugins.bdi.model.ir.SourceSpan;

/** Portable source identity relative to an explicit project root. */
public record ProjectSourceId(
        String projectPath,
        int beginLine,
        int beginColumn,
        int endLine,
        int endColumn) {
    public static final String VERSION = "bdi-source-v2";
    private static final String PREFIX = VERSION + ":";

    public ProjectSourceId {
        if (projectPath == null || projectPath.isBlank()) {
            throw new IllegalArgumentException("projectPath must not be blank");
        }
        if (projectPath.startsWith("/") || projectPath.endsWith("/")
                || projectPath.contains("//")) {
            throw new IllegalArgumentException("projectPath must be a normalized relative path: " + projectPath);
        }
        for (String segment : projectPath.split("/")) {
            if (segment.equals(".") || segment.equals("..")) {
                throw new IllegalArgumentException("projectPath must not contain traversal segments: " + projectPath);
            }
        }
        validatePositions(beginLine, beginColumn, endLine, endColumn);
    }

    public static ProjectSourceId from(Path projectRoot, SourceSpan sourceSpan) {
        Objects.requireNonNull(sourceSpan, "sourceSpan");
        return new ProjectSourceId(
                portablePath(projectRoot, sourceSpan.source()),
                sourceSpan.beginLine(),
                sourceSpan.beginColumn(),
                sourceSpan.endLine(),
                sourceSpan.endColumn());
    }

    public static ProjectSourceId fromPath(Path projectRoot, Path source) {
        return new ProjectSourceId(
                portablePath(projectRoot, source),
                SourceSpan.UNKNOWN_POSITION,
                SourceSpan.UNKNOWN_POSITION,
                SourceSpan.UNKNOWN_POSITION,
                SourceSpan.UNKNOWN_POSITION);
    }

    public static ProjectSourceId parse(String canonical) {
        if (canonical == null || !canonical.startsWith(PREFIX)) {
            throw new IllegalArgumentException("Unsupported project source identity: " + canonical);
        }
        int lengthSeparator = canonical.indexOf(':', PREFIX.length());
        if (lengthSeparator < 0) {
            throw new IllegalArgumentException("Malformed project source identity: missing path length");
        }
        int pathLength;
        try {
            pathLength = Integer.parseInt(canonical.substring(PREFIX.length(), lengthSeparator));
        } catch (NumberFormatException error) {
            throw new IllegalArgumentException("Malformed project source identity: invalid path length", error);
        }
        int pathStart = lengthSeparator + 1;
        int pathEnd = pathStart + pathLength;
        if (pathLength <= 0 || pathEnd >= canonical.length() || canonical.charAt(pathEnd) != ':') {
            throw new IllegalArgumentException("Malformed project source identity: invalid path boundary");
        }
        String[] positions = canonical.substring(pathEnd + 1).split(":", -1);
        if (positions.length != 4) {
            throw new IllegalArgumentException("Malformed project source identity: expected four positions");
        }
        try {
            return new ProjectSourceId(
                    canonical.substring(pathStart, pathEnd),
                    Integer.parseInt(positions[0]),
                    Integer.parseInt(positions[1]),
                    Integer.parseInt(positions[2]),
                    Integer.parseInt(positions[3]));
        } catch (NumberFormatException error) {
            throw new IllegalArgumentException("Malformed project source identity: invalid position", error);
        }
    }

    public String canonical() {
        return PREFIX + projectPath.length() + ":" + projectPath + ":"
                + beginLine + ":" + beginColumn + ":" + endLine + ":" + endColumn;
    }

    public Path resolve(Path projectRoot) {
        Path root = normalizedRoot(projectRoot);
        Path resolved = root;
        for (String segment : projectPath.split("/")) {
            resolved = resolved.resolve(decodeSegment(segment));
        }
        resolved = resolved.normalize();
        if (!resolved.startsWith(root)) {
            throw new IllegalArgumentException("Project source identity resolves outside project root: " + projectPath);
        }
        return resolved;
    }

    public SourceSpan toSourceSpan(Path projectRoot) {
        return new SourceSpan(resolve(projectRoot), beginLine, beginColumn, endLine, endColumn);
    }

    private static String portablePath(Path projectRoot, Path source) {
        Path root = normalizedRoot(projectRoot);
        Path normalizedSource = Objects.requireNonNull(source, "source").toAbsolutePath().normalize();
        if (!normalizedSource.startsWith(root) || normalizedSource.equals(root)) {
            throw new IllegalArgumentException(
                    "Source is outside project root: " + normalizedSource + " (root " + root + ")");
        }
        List<String> segments = new ArrayList<>();
        for (Path segment : root.relativize(normalizedSource)) {
            segments.add(encodeSegment(segment.toString()));
        }
        return String.join("/", segments);
    }

    private static Path normalizedRoot(Path projectRoot) {
        return Objects.requireNonNull(projectRoot, "projectRoot").toAbsolutePath().normalize();
    }

    private static String encodeSegment(String segment) {
        return segment.replace("%", "%25").replace("\\", "%5C");
    }

    private static String decodeSegment(String segment) {
        StringBuilder decoded = new StringBuilder(segment.length());
        for (int index = 0; index < segment.length(); index++) {
            char character = segment.charAt(index);
            if (character != '%') {
                decoded.append(character);
                continue;
            }
            if (index + 2 >= segment.length()) {
                throw new IllegalArgumentException("Malformed percent escape in project path: " + segment);
            }
            try {
                decoded.append((char) Integer.parseInt(segment.substring(index + 1, index + 3), 16));
            } catch (NumberFormatException error) {
                throw new IllegalArgumentException("Malformed percent escape in project path: " + segment, error);
            }
            index += 2;
        }
        return decoded.toString();
    }

    private static void validatePositions(int beginLine, int beginColumn, int endLine, int endColumn) {
        if (beginLine < 0 || beginColumn < 0 || endLine < 0 || endColumn < 0) {
            throw new IllegalArgumentException("Source positions must not be negative");
        }
        if ((beginLine == 0) != (endLine == 0)) {
            throw new IllegalArgumentException("Begin and end lines must both be known or unknown");
        }
        if (beginLine == 0 && (beginColumn != 0 || endColumn != 0)) {
            throw new IllegalArgumentException("Unknown lines require unknown columns");
        }
        if ((beginColumn == 0) != (endColumn == 0)) {
            throw new IllegalArgumentException("Begin and end columns must both be known or unknown");
        }
        if (endLine > 0 && endLine < beginLine) {
            throw new IllegalArgumentException("End line must not precede begin line");
        }
        if (beginColumn > 0 && beginLine == endLine && endColumn < beginColumn) {
            throw new IllegalArgumentException("End column must not precede begin column");
        }
    }
}

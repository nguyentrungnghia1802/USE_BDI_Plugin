package org.tzi.use.plugins.bdi.persistence;

import java.nio.file.Path;

import org.tzi.use.plugins.bdi.model.mapping.MappingBinding;
import org.tzi.use.plugins.bdi.model.mapping.MappingKind;
import org.tzi.use.plugins.bdi.model.source.ProjectSourceId;

/** Converts path-bearing mapping sources at the persistence boundary. */
final class MappingSourceMigration {
    private MappingSourceMigration() {
    }

    static String toPortable(MappingBinding binding, Path projectRoot) {
        if (!isPathBearing(binding.kind())) {
            return binding.source();
        }
        int marker = suffixMarker(binding.kind(), binding.source());
        Path source = absolutePath(binding.source().substring(0, marker), binding.source());
        String suffix = binding.source().substring(marker);
        return ProjectSourceId.fromPath(projectRoot, source).canonical() + suffix;
    }

    static String toRuntime(MappingKind kind, String persistedSource, Path projectRoot, boolean legacy) {
        if (!isPathBearing(kind)) {
            return persistedSource;
        }
        if (legacy) {
            int marker = suffixMarker(kind, persistedSource);
            Path source = absolutePath(persistedSource.substring(0, marker), persistedSource);
            ProjectSourceId.fromPath(projectRoot, source);
            return runtimePath(source) + persistedSource.substring(marker);
        }
        int marker = persistedSource.indexOf('#');
        String canonical = marker < 0 ? persistedSource : persistedSource.substring(0, marker);
        String suffix = marker < 0 ? "" : persistedSource.substring(marker);
        return runtimePath(ProjectSourceId.parse(canonical).resolve(projectRoot)) + suffix;
    }

    private static int suffixMarker(MappingKind kind, String source) {
        String marker = switch (kind) {
            case AGENT_CLASS, AGENT_OBJECT -> null;
            case ACTION_OPERATION, PARAMETER -> "#plan:";
            case RECEIVER_OBJECT -> "#receiver:";
            case BELIEF_ATTRIBUTE -> throw new IllegalArgumentException("Belief mappings do not contain paths");
        };
        if (marker == null) {
            return source.length();
        }
        int offset = source.indexOf(marker);
        if (offset < 0) {
            throw new IllegalArgumentException("Ambiguous path-bearing mapping source: " + source);
        }
        return offset;
    }

    private static Path absolutePath(String candidate, String source) {
        try {
            Path path = Path.of(candidate);
            if (!path.isAbsolute()) {
                throw new IllegalArgumentException("Ambiguous legacy mapping source: " + source);
            }
            return path.toAbsolutePath().normalize();
        } catch (java.nio.file.InvalidPathException error) {
            throw new IllegalArgumentException("Invalid mapping source path: " + source, error);
        }
    }

    private static String runtimePath(Path source) {
        return source.toAbsolutePath().normalize().toString().replace('\\', '/');
    }

    private static boolean isPathBearing(MappingKind kind) {
        return kind != MappingKind.BELIEF_ATTRIBUTE;
    }
}

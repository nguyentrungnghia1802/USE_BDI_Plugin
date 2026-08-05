package org.tzi.use.plugins.bdi.application;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/** Tracks selected AgentSpeak files without coupling re-import to Swing. */
public final class BdiSourceTracker {
    private List<Path> sources = List.of();
    private Map<Path, FileStamp> importedStamps = Map.of();
    private boolean imported;

    public void track(List<Path> selectedSources) {
        Objects.requireNonNull(selectedSources, "selectedSources");
        List<Path> normalized = selectedSources.stream()
                .map(BdiSourceTracker::normalize)
                .distinct()
                .toList();
        Map<Path, FileStamp> retained = new LinkedHashMap<>();
        for (Path source : normalized) {
            FileStamp previous = importedStamps.get(source);
            if (previous != null) {
                retained.put(source, previous);
            }
        }
        sources = List.copyOf(normalized);
        importedStamps = Map.copyOf(retained);
    }

    public List<Path> sources() {
        return sources;
    }

    public boolean hasImportedSnapshot() {
        return imported;
    }

    public void markImported() {
        Map<Path, FileStamp> current = new LinkedHashMap<>();
        for (Path source : sources) {
            current.put(source, FileStamp.read(source));
        }
        importedStamps = Map.copyOf(current);
        imported = true;
    }

    public List<Path> changedSources() {
        if (!imported) {
            return List.of();
        }
        List<Path> changed = new ArrayList<>();
        for (Path source : sources) {
            if (!Objects.equals(importedStamps.get(source), FileStamp.read(source))) {
                changed.add(source);
            }
        }
        return List.copyOf(changed);
    }

    private static Path normalize(Path source) {
        return Objects.requireNonNull(source, "source").toAbsolutePath().normalize();
    }

    private record FileStamp(boolean exists, long size, long lastModifiedMillis) {
        private static FileStamp read(Path source) {
            try {
                return new FileStamp(
                        Files.exists(source),
                        Files.exists(source) ? Files.size(source) : -1L,
                        Files.exists(source) ? Files.getLastModifiedTime(source).toMillis() : -1L);
            } catch (IOException error) {
                return new FileStamp(false, -1L, -1L);
            }
        }
    }
}

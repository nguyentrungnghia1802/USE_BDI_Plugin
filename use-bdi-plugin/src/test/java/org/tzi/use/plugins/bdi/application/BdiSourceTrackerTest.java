package org.tzi.use.plugins.bdi.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class BdiSourceTrackerTest {
    @TempDir
    Path tempDir;

    @Test
    void reportsOnlyFilesChangedSinceLastSuccessfulImport() throws Exception {
        Path source = tempDir.resolve("agent.asl");
        Files.writeString(source, "+ready.");

        BdiSourceTracker tracker = new BdiSourceTracker();
        tracker.track(List.of(source));
        tracker.markImported();
        assertTrue(tracker.changedSources().isEmpty());

        Files.writeString(source, "+ready.\n+changed.");
        assertEquals(List.of(source.toAbsolutePath().normalize()), tracker.changedSources());

        tracker.markImported();
        Files.delete(source);
        assertEquals(List.of(source.toAbsolutePath().normalize()), tracker.changedSources());
    }
}

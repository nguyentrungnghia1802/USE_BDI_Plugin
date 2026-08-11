package org.tzi.use.plugins.bdi.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.Test;
import org.tzi.use.plugins.bdi.application.BdiImportService;
import org.tzi.use.plugins.bdi.application.BdiImportSnapshot;
import org.tzi.use.plugins.bdi.application.BdiProjectConfiguration;
import org.tzi.use.plugins.bdi.application.MasProjectAnalysisRequest;
import org.tzi.use.plugins.bdi.application.MasProjectAnalysisResult;
import org.tzi.use.plugins.bdi.model.mapping.MappingDocument;

class BdiImportWorkerTest {
    @Test
    void completesImportOnSwingWorkerCallback() throws Exception {
        CountDownLatch completed = new CountDownLatch(1);
        AtomicReference<BdiImportSnapshot> result = new AtomicReference<>();
        AtomicReference<Throwable> failure = new AtomicReference<>();
        BdiImportWorker worker = new BdiImportWorker(
                new BdiImportService(),
                List.of(fixture("fixtures/asl/valid/minimal.asl")),
                snapshot -> {
                    result.set(snapshot);
                    completed.countDown();
                },
                error -> {
                    failure.set(error);
                    completed.countDown();
                });

        worker.execute();

        if (!completed.await(10, TimeUnit.SECONDS)) {
            throw new AssertionError("SwingWorker did not complete in time");
        }
        assertNull(failure.get());
        assertEquals(1, result.get().fileCount());
        assertEquals(1, result.get().models().get(0).plans().size());
    }

    @Test
    void completesJacamoProjectAnalysisOffTheEdt() throws Exception {
        CountDownLatch completed = new CountDownLatch(1);
        AtomicReference<MasProjectAnalysisResult> result = new AtomicReference<>();
        AtomicReference<Throwable> failure = new AtomicReference<>();
        MasProjectAnalysisRequest request = MasProjectAnalysisRequest.of(
                fixture("fixtures/casestudy/auction/auction.jcm"),
                java.time.Instant.EPOCH,
                java.util.Optional.empty(),
                java.util.Optional.empty(),
                MappingDocument.empty("unknown"),
                BdiProjectConfiguration.defaults());

        BdiProjectImportWorker worker = new BdiProjectImportWorker(
                new org.tzi.use.plugins.bdi.application.MasProjectAnalysisService(),
                request,
                imported -> {
                    result.set(imported);
                    completed.countDown();
                },
                error -> {
                    failure.set(error);
                    completed.countDown();
                });

        worker.execute();

        assertTrue(completed.await(10, TimeUnit.SECONDS));
        assertNull(failure.get());
        assertEquals(2, result.get().snapshot().importedFileCount());
        assertEquals(3, result.get().project().orElseThrow().agents().size());
        assertTrue(result.get().projectDiagnostics().stream()
                .anyMatch(diagnostic -> "JCM-005".equals(diagnostic.code())));
    }

    private static Path fixture(String name) throws URISyntaxException {
        URL resource = BdiImportWorkerTest.class.getClassLoader().getResource(name);
        if (resource == null) {
            throw new IllegalStateException("Missing test fixture: " + name);
        }
        return Path.of(resource.toURI());
    }
}

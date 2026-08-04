package org.tzi.use.plugins.bdi.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

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

    private static Path fixture(String name) throws URISyntaxException {
        URL resource = BdiImportWorkerTest.class.getClassLoader().getResource(name);
        if (resource == null) {
            throw new IllegalStateException("Missing test fixture: " + name);
        }
        return Path.of(resource.toURI());
    }
}

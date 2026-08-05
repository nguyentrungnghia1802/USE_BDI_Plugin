package org.tzi.use.plugins.bdi.ui;

import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;

import javax.swing.SwingWorker;

import org.tzi.use.plugins.bdi.application.BdiImportService;
import org.tzi.use.plugins.bdi.application.BdiImportSnapshot;

/** Swing worker that keeps Jason parsing and index construction off the EDT. */
public final class BdiImportWorker extends SwingWorker<BdiImportSnapshot, Void> {
    private final BdiImportService service;
    private final List<Path> sources;
    private final Consumer<BdiImportSnapshot> onSuccess;
    private final Consumer<Throwable> onFailure;

    public BdiImportWorker(
            BdiImportService service,
            List<Path> sources,
            Consumer<BdiImportSnapshot> onSuccess,
            Consumer<Throwable> onFailure) {
        this.service = Objects.requireNonNull(service, "service");
        this.sources = List.copyOf(Objects.requireNonNull(sources, "sources"));
        this.onSuccess = Objects.requireNonNull(onSuccess, "onSuccess");
        this.onFailure = Objects.requireNonNull(onFailure, "onFailure");
    }

    @Override
    protected BdiImportSnapshot doInBackground() {
        return service.importFiles(sources);
    }

    @Override
    protected void done() {
        try {
            onSuccess.accept(get());
        } catch (InterruptedException error) {
            Thread.currentThread().interrupt();
            onFailure.accept(error);
        } catch (CancellationException ignored) {
            // A detached or superseded view intentionally cancels its worker.
        } catch (ExecutionException error) {
            onFailure.accept(error.getCause() == null ? error : error.getCause());
        }
    }
}

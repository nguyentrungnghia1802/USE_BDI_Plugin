package org.tzi.use.plugins.bdi.ui;

import java.util.Objects;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;

import javax.swing.SwingWorker;

import org.tzi.use.plugins.bdi.application.MasProjectAnalysisRequest;
import org.tzi.use.plugins.bdi.application.MasProjectAnalysisResult;
import org.tzi.use.plugins.bdi.application.MasProjectAnalysisService;

/** Runs static `.jcm` composition away from the Swing event dispatch thread. */
public final class BdiProjectImportWorker extends SwingWorker<MasProjectAnalysisResult, Void> {
    private final MasProjectAnalysisService service;
    private final MasProjectAnalysisRequest request;
    private final Consumer<MasProjectAnalysisResult> onSuccess;
    private final Consumer<Throwable> onFailure;

    public BdiProjectImportWorker(
            MasProjectAnalysisService service,
            MasProjectAnalysisRequest request,
            Consumer<MasProjectAnalysisResult> onSuccess,
            Consumer<Throwable> onFailure) {
        this.service = Objects.requireNonNull(service, "service");
        this.request = Objects.requireNonNull(request, "request");
        this.onSuccess = Objects.requireNonNull(onSuccess, "onSuccess");
        this.onFailure = Objects.requireNonNull(onFailure, "onFailure");
    }

    @Override
    protected MasProjectAnalysisResult doInBackground() {
        return service.analyze(request);
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

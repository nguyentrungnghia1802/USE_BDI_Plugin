package org.tzi.use.plugins.bdi.application;

import java.util.Objects;

/** Composes the verified static project importer with the shared analysis snapshot pipeline. */
public final class MasProjectAnalysisService {
    private static final String PLUGIN_VERSION = "0.1.0";
    private static final String USE_VERSION = "USE-7.1.1";

    private final MasProjectImportService importService;

    public MasProjectAnalysisService() {
        this(new MasProjectImportService());
    }

    MasProjectAnalysisService(MasProjectImportService importService) {
        this.importService = Objects.requireNonNull(importService, "importService");
    }

    public MasProjectAnalysisResult analyze(MasProjectAnalysisRequest request) {
        Objects.requireNonNull(request, "request");
        MasProjectImportResult imported = importService.importProject(
                request.projectRoot(), request.projectFile());
        CurrentAnalysisSnapshot snapshot = new CurrentAnalysisSnapshotService(
                request.configuration().newOrchestrator(),
                request.configuration().summary(),
                PLUGIN_VERSION,
                USE_VERSION).create(
                        request.timestamp(),
                        imported.bdiSnapshot(),
                        request.useModel(),
                        request.oclEvaluator(),
                        request.mapping());
        return new MasProjectAnalysisResult(
                imported.project(), snapshot, imported.diagnostics());
    }
}

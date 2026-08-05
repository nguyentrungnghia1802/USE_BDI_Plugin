package org.tzi.use.plugins.bdi.importer;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class JasonAslImporter implements AslImporter {
    private final JasonAslParserAdapter parser = new JasonAslParserAdapter();

    @Override
    public AslImportResult importFiles(List<Path> sources) {
        List<Path> validatedSources = List.copyOf(Objects.requireNonNull(sources, "sources"));
        List<AslParseSummary> summaries = new ArrayList<>(validatedSources.size());
        List<AslDiagnostic> diagnostics = new ArrayList<>();
        for (Path source : validatedSources) {
            try {
                summaries.add(parser.parse(source));
            } catch (AslParseException error) {
                diagnostics.add(toDiagnostic(source, error));
            }
        }
        return new AslImportResult(summaries, diagnostics);
    }

    private static AslDiagnostic toDiagnostic(Path source, AslParseException error) {
        return error.diagnostic().orElseGet(() -> new AslDiagnostic(
                AslDiagnostic.IMPORT_ERROR_CODE,
                AslDiagnosticSeverity.ERROR,
                source.toAbsolutePath().normalize(),
                AslDiagnostic.UNKNOWN_POSITION,
                AslDiagnostic.UNKNOWN_POSITION,
                error.getMessage() == null ? "Could not import AgentSpeak source" : error.getMessage()));
    }
}

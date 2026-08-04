package org.tzi.use.plugins.bdi.application;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.tzi.use.plugins.bdi.importer.AslDiagnostic;
import org.tzi.use.plugins.bdi.importer.AslDiagnosticSeverity;
import org.tzi.use.plugins.bdi.importer.AslParseException;
import org.tzi.use.plugins.bdi.importer.JasonAslParserAdapter;
import org.tzi.use.plugins.bdi.index.BdiIndex;
import org.tzi.use.plugins.bdi.index.BdiIndexBuilder;
import org.tzi.use.plugins.bdi.model.ir.AgentModel;

/** Imports full normalized IR while preserving successful files after errors. */
public final class BdiImportService {
    private final JasonAslParserAdapter parser;
    private final BdiIndexBuilder indexBuilder;

    public BdiImportService() {
        this(new JasonAslParserAdapter(), new BdiIndexBuilder());
    }

    BdiImportService(JasonAslParserAdapter parser, BdiIndexBuilder indexBuilder) {
        this.parser = Objects.requireNonNull(parser, "parser");
        this.indexBuilder = Objects.requireNonNull(indexBuilder, "indexBuilder");
    }

    public BdiImportSnapshot importFiles(List<Path> sources) {
        List<Path> inputs = List.copyOf(Objects.requireNonNull(sources, "sources"));
        List<AgentModel> models = new ArrayList<>(inputs.size());
        List<AslDiagnostic> diagnostics = new ArrayList<>();
        for (Path source : inputs) {
            try {
                models.add(parser.parseModel(source));
            } catch (AslParseException error) {
                diagnostics.add(toDiagnostic(source, error));
            }
        }
        BdiIndex index = indexBuilder.build(models);
        return new BdiImportSnapshot(models, diagnostics, index);
    }

    private static AslDiagnostic toDiagnostic(Path source, AslParseException error) {
        return error.diagnostic().orElseGet(() -> new AslDiagnostic(
                AslDiagnostic.IMPORT_ERROR_CODE,
                AslDiagnosticSeverity.ERROR,
                source.toAbsolutePath().normalize(),
                AslDiagnostic.UNKNOWN_POSITION,
                AslDiagnostic.UNKNOWN_POSITION,
                error.getMessage() == null
                        ? "Could not import AgentSpeak source"
                        : error.getMessage()));
    }
}

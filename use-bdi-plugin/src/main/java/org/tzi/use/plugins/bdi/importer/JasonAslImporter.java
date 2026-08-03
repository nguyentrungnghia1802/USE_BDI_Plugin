package org.tzi.use.plugins.bdi.importer;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class JasonAslImporter implements AslImporter {
    private final JasonAslParserAdapter parser = new JasonAslParserAdapter();

    @Override
    public AslImportResult importFiles(List<Path> sources) throws AslParseException {
        List<Path> validatedSources = List.copyOf(Objects.requireNonNull(sources, "sources"));
        List<AslParseSummary> summaries = new ArrayList<>(validatedSources.size());
        for (Path source : validatedSources) {
            summaries.add(parser.parse(source));
        }
        return new AslImportResult(summaries);
    }
}

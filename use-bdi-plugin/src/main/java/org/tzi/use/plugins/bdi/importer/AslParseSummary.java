package org.tzi.use.plugins.bdi.importer;

import java.nio.file.Path;

public record AslParseSummary(
        Path source,
        String parserVersion,
        int beliefCount,
        int goalCount,
        int planCount) {
}

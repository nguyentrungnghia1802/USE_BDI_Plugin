package org.tzi.use.plugins.bdi.importer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.Test;

class AslImportReportTest {
    @Test
    void reportsDistinctParserVersionsInEncounterOrder() {
        AslParseSummary first = summary("first.asl", "3.3.0");
        AslParseSummary second = summary("second.asl", "3.3.0");
        AslParseSummary future = summary("future.asl", "3.4.0");
        AslImportResult result = new AslImportResult(List.of(first, second, future));

        AslImportReport report = result.toReport();

        assertSame(result, report.importResult());
        assertEquals(List.of("3.3.0", "3.4.0"), report.parserVersions());
        assertThrows(UnsupportedOperationException.class, () -> report.parserVersions().clear());
    }

    @Test
    void reportsNoParserVersionWhenNoFileSucceeded() {
        AslImportReport report = new AslImportResult(List.of()).toReport();

        assertEquals(List.of(), report.parserVersions());
    }

    private static AslParseSummary summary(String fileName, String parserVersion) {
        return new AslParseSummary(Path.of(fileName), parserVersion, 0, 0, 0, List.of());
    }
}

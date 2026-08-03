package org.tzi.use.plugins.bdi.importer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.Test;

class JasonAslImporterTest {
    private final AslImporter importer = new JasonAslImporter();

    @Test
    void importsEmptyInputAsEmptyResult() throws Exception {
        AslImportResult result = importer.importFiles(List.of());

        assertEquals(0, result.fileCount());
        assertEquals(0, result.totalBeliefCount());
        assertEquals(0, result.totalGoalCount());
        assertEquals(0, result.totalPlanCount());
    }

    @Test
    void importsMultipleFilesInInputOrder() throws Exception {
        Path minimal = fixture("fixtures/asl/valid/minimal.asl");
        Path review = fixture("fixtures/asl/valid/review-agent.asl");

        AslImportResult result = importer.importFiles(List.of(minimal, review));

        assertEquals(2, result.fileCount());
        assertEquals(minimal.toAbsolutePath().normalize(), result.fileSummaries().get(0).source());
        assertEquals(review.toAbsolutePath().normalize(), result.fileSummaries().get(1).source());
        assertEquals(3, result.totalBeliefCount());
        assertEquals(2, result.totalGoalCount());
        assertEquals(2, result.totalPlanCount());
        assertThrows(UnsupportedOperationException.class, () -> result.fileSummaries().clear());
    }

    @Test
    void propagatesFirstSyntaxDiagnosticWithoutPartialResult() throws Exception {
        Path valid = fixture("fixtures/asl/valid/minimal.asl");
        Path invalid = fixture("fixtures/asl/invalid/missing-plan-body.asl");

        AslParseException error = assertThrows(
                AslParseException.class,
                () -> importer.importFiles(List.of(valid, invalid)));

        AslDiagnostic diagnostic = error.diagnostic().orElseThrow();
        assertEquals(AslDiagnostic.SYNTAX_ERROR_CODE, diagnostic.code());
        assertEquals(invalid.toAbsolutePath().normalize(), diagnostic.source());
    }

    private static Path fixture(String name) throws URISyntaxException {
        URL resource = JasonAslImporterTest.class.getClassLoader().getResource(name);
        if (resource == null) {
            throw new IllegalStateException("Missing test fixture: " + name);
        }
        return Path.of(resource.toURI());
    }
}

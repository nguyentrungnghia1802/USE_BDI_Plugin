package org.tzi.use.plugins.bdi.importer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
        assertTrue(result.diagnostics().isEmpty());
        assertFalse(result.hasErrors());
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
        assertTrue(result.diagnostics().isEmpty());
        assertFalse(result.hasErrors());
        assertThrows(UnsupportedOperationException.class, () -> result.fileSummaries().clear());
    }

    @Test
    void keepsSuccessfulFilesAndReportsSyntaxDiagnostic() throws Exception {
        Path firstValid = fixture("fixtures/asl/valid/minimal.asl");
        Path invalid = fixture("fixtures/asl/invalid/missing-plan-body.asl");
        Path secondValid = fixture("fixtures/asl/valid/review-agent.asl");

        AslImportResult result = importer.importFiles(List.of(firstValid, invalid, secondValid));

        assertEquals(2, result.fileCount());
        assertEquals(firstValid.toAbsolutePath().normalize(), result.fileSummaries().get(0).source());
        assertEquals(secondValid.toAbsolutePath().normalize(), result.fileSummaries().get(1).source());
        assertEquals(3, result.totalBeliefCount());
        assertEquals(2, result.totalGoalCount());
        assertEquals(2, result.totalPlanCount());
        assertEquals(1, result.diagnostics().size());
        AslDiagnostic diagnostic = result.diagnostics().get(0);
        assertEquals(AslDiagnostic.SYNTAX_ERROR_CODE, diagnostic.code());
        assertEquals(invalid.toAbsolutePath().normalize(), diagnostic.source());
        assertTrue(result.hasErrors());
        assertThrows(UnsupportedOperationException.class, () -> result.diagnostics().clear());
    }

    @Test
    void continuesAfterMissingFileAndReportsImportDiagnostic() throws Exception {
        Path firstValid = fixture("fixtures/asl/valid/minimal.asl");
        Path missing = firstValid.resolveSibling("does-not-exist.asl");
        Path secondValid = fixture("fixtures/asl/valid/review-agent.asl");

        AslImportResult result = importer.importFiles(List.of(firstValid, missing, secondValid));

        assertEquals(2, result.fileCount());
        assertEquals(1, result.diagnostics().size());
        AslDiagnostic diagnostic = result.diagnostics().get(0);
        assertEquals(AslDiagnostic.IMPORT_ERROR_CODE, diagnostic.code());
        assertEquals(AslDiagnosticSeverity.ERROR, diagnostic.severity());
        assertEquals(missing.toAbsolutePath().normalize(), diagnostic.source());
        assertEquals(AslDiagnostic.UNKNOWN_POSITION, diagnostic.line());
        assertEquals(AslDiagnostic.UNKNOWN_POSITION, diagnostic.column());
        assertFalse(diagnostic.hasSourcePosition());
        assertTrue(result.hasErrors());
    }

    private static Path fixture(String name) throws URISyntaxException {
        URL resource = JasonAslImporterTest.class.getClassLoader().getResource(name);
        if (resource == null) {
            throw new IllegalStateException("Missing test fixture: " + name);
        }
        return Path.of(resource.toURI());
    }
}

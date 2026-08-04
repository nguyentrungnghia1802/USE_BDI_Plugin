package org.tzi.use.plugins.bdi.importer;

import java.nio.file.Path;
import java.util.List;

public final class PackagedParserSmoke {
    private PackagedParserSmoke() {
    }

    public static void main(String[] args) throws Exception {
        if (args.length != 3) {
            System.err.println("Expected paths to a valid, invalid, and second valid AgentSpeak fixture.");
            System.exit(2);
        }

        Path firstValid = Path.of(args[0]).toAbsolutePath().normalize();
        Path invalid = Path.of(args[1]).toAbsolutePath().normalize();
        Path secondValid = Path.of(args[2]).toAbsolutePath().normalize();
        AslImportResult result = new JasonAslImporter().importFiles(
                List.of(firstValid, invalid, secondValid));
        if (result.fileCount() != 2
                || !firstValid.equals(result.fileSummaries().get(0).source())
                || !secondValid.equals(result.fileSummaries().get(1).source())
                || result.fileSummaries().stream()
                        .anyMatch(summary -> !"3.3.0".equals(summary.parserVersion()))
                || result.totalBeliefCount() != 3
                || result.totalGoalCount() != 2
                || result.totalPlanCount() != 2
                || result.diagnostics().size() != 1) {
            throw new IllegalStateException("Unexpected packaged importer result: " + result);
        }

        AslDiagnostic diagnostic = result.diagnostics().get(0);
        if (!AslDiagnostic.SYNTAX_ERROR_CODE.equals(diagnostic.code())
                || diagnostic.severity() != AslDiagnosticSeverity.ERROR
                || !invalid.equals(diagnostic.source())
                || diagnostic.line() != 3
                || diagnostic.column() != 8) {
            throw new IllegalStateException("Unexpected packaged importer diagnostic: " + diagnostic);
        }

        System.out.println("PARTIAL_IMPORT_SMOKE_OK: preserved 2 successful summaries and 1 diagnostic");
        System.out.println("DIAGNOSTIC_SMOKE_OK: ASL-001 at line 3, column 8");
    }
}

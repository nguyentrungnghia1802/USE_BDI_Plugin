package org.tzi.use.plugins.bdi.importer;

import java.nio.file.Path;
import java.util.List;

public final class PackagedParserSmoke {
    private PackagedParserSmoke() {
    }

    public static void main(String[] args) throws Exception {
        if (args.length != 3) {
            System.err.println("Expected paths to two valid and one invalid AgentSpeak fixtures.");
            System.exit(2);
        }

        AslImportResult result = new JasonAslImporter().importFiles(
                List.of(Path.of(args[0]), Path.of(args[1])));
        if (result.fileCount() != 2
                || result.fileSummaries().stream()
                        .anyMatch(summary -> !"3.3.0".equals(summary.parserVersion()))
                || result.totalBeliefCount() != 3
                || result.totalGoalCount() != 2
                || result.totalPlanCount() != 2) {
            throw new IllegalStateException("Unexpected packaged importer result: " + result);
        }

        System.out.println("MULTI_FILE_SMOKE_OK: parsed 2 files, 3 beliefs, 2 goals, 2 plans");

        JasonAslParserAdapter parser = new JasonAslParserAdapter();
        try {
            parser.parse(Path.of(args[2]));
            throw new IllegalStateException("Invalid fixture unexpectedly parsed successfully");
        } catch (AslParseException error) {
            AslDiagnostic diagnostic = error.diagnostic().orElseThrow(
                    () -> new IllegalStateException("Invalid fixture did not produce a diagnostic", error));
            if (!AslDiagnostic.SYNTAX_ERROR_CODE.equals(diagnostic.code())
                    || diagnostic.severity() != AslDiagnosticSeverity.ERROR
                    || diagnostic.line() != 3
                    || diagnostic.column() != 8) {
                throw new IllegalStateException("Unexpected packaged parser diagnostic: " + diagnostic);
            }
        }

        System.out.println("DIAGNOSTIC_SMOKE_OK: ASL-001 at line 3, column 8");
    }
}

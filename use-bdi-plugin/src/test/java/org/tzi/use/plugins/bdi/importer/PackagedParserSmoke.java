package org.tzi.use.plugins.bdi.importer;

import java.nio.file.Path;

public final class PackagedParserSmoke {
    private PackagedParserSmoke() {
    }

    public static void main(String[] args) throws Exception {
        if (args.length != 2) {
            System.err.println("Expected paths to valid and invalid AgentSpeak fixtures.");
            System.exit(2);
        }

        JasonAslParserAdapter parser = new JasonAslParserAdapter();
        AslParseSummary result = parser.parse(Path.of(args[0]));
        if (!"3.3.0".equals(result.parserVersion())
                || result.beliefCount() != 1
                || result.goalCount() != 1
                || result.planCount() != 1) {
            throw new IllegalStateException("Unexpected packaged parser result: " + result);
        }

        System.out.println("PARSER_SMOKE_OK: Jason 3.3.0 parsed 1 belief, 1 goal, 1 plan");

        try {
            parser.parse(Path.of(args[1]));
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

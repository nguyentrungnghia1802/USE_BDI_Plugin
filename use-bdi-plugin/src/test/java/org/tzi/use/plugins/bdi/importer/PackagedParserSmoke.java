package org.tzi.use.plugins.bdi.importer;

import java.nio.file.Path;
import java.util.List;

import org.tzi.use.plugins.bdi.model.ir.AgentModel;
import org.tzi.use.plugins.bdi.model.ir.AslAgentModelNormalizer;

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
        AslImportReport report = result.toReport();
        List<AgentModel> agentModels = new AslAgentModelNormalizer().normalize(result);
        AgentModel materializedModel = new JasonAslParserAdapter().parseModel(firstValid);
        if (result.fileCount() != 2
                || agentModels.size() != 2
                || !materializedModel.isMaterialized()
                || materializedModel.beliefs().size() != 1
                || materializedModel.goals().size() != 1
                || materializedModel.plans().size() != 1
                || materializedModel.plans().get(0).steps().size() != 1
                || !materializedModel.unsupportedFeatures().isEmpty()
                || !firstValid.equals(result.fileSummaries().get(0).source())
                || !secondValid.equals(result.fileSummaries().get(1).source())
                || !firstValid.equals(agentModels.get(0).source())
                || !secondValid.equals(agentModels.get(1).source())
                || agentModels.get(0).beliefCount() != 1
                || agentModels.get(0).goalCount() != 1
                || agentModels.get(0).planCount() != 1
                || agentModels.get(0).elementCount() != 3
                || !List.of("3.3.0").equals(report.parserVersions())
                || result.fileSummaries().stream()
                        .anyMatch(summary -> !"3.3.0".equals(summary.parserVersion()))
                || result.fileSummaries().get(0).sourceLocations().size() != 3
                || result.fileSummaries().get(0).sourceLocations().get(0).beginLine() != 1
                || result.fileSummaries().get(0).sourceLocations().get(1).beginLine() != 2
                || result.fileSummaries().get(0).sourceLocations().get(2).beginLine() != 4
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
        System.out.println("SOURCE_LOCATION_SMOKE_OK: minimal.asl locations at lines 1, 2, and 4-5");
        System.out.println("REPORT_VERSION_SMOKE_OK: parser version 3.3.0");
        System.out.println("AGENT_MODEL_SMOKE_OK: normalized 2 successful files into root IR models");
        System.out.println("IR_TREE_SMOKE_OK: materialized minimal belief-goal-plan tree");
        System.out.println("DIAGNOSTIC_SMOKE_OK: ASL-001 at line 3, column 8");
    }
}

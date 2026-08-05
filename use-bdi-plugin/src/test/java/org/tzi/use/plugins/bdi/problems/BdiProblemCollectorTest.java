package org.tzi.use.plugins.bdi.problems;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.tzi.use.plugins.bdi.application.BdiImportSnapshot;
import org.tzi.use.plugins.bdi.importer.AslDiagnostic;
import org.tzi.use.plugins.bdi.importer.AslDiagnosticSeverity;
import org.tzi.use.plugins.bdi.index.BdiIndex;
import org.tzi.use.plugins.bdi.model.ir.AgentModel;
import org.tzi.use.plugins.bdi.model.ir.SourceSpan;
import org.tzi.use.plugins.bdi.model.ir.UnsupportedFeature;

class BdiProblemCollectorTest {
    @Test
    void preservesImportAndUnsupportedEvidenceAsProblemRows() {
        Path source = Path.of("agent.asl").toAbsolutePath();
        AgentModel model = new AgentModel(
                source,
                "test-parser",
                0,
                0,
                0,
                List.of(),
                List.of(),
                List.of(),
                List.of(new UnsupportedFeature(
                        UnsupportedFeature.CODE,
                        "label",
                        "label",
                        new SourceSpan(source, 3, 4, 3, 8))));
        BdiImportSnapshot snapshot = new BdiImportSnapshot(
                List.of(model),
                List.of(new AslDiagnostic(
                        AslDiagnostic.SYNTAX_ERROR_CODE,
                        AslDiagnosticSeverity.ERROR,
                        source,
                        1,
                        2,
                        "syntax error")),
                BdiIndex.empty());

        List<BdiProblem> problems = BdiProblemCollector.collect(snapshot);
        assertEquals(2, problems.size());
        assertEquals(BdiProblemSeverity.ERROR, problems.get(0).severity());
        assertTrue(problems.stream().anyMatch(problem -> problem.code().equals(UnsupportedFeature.CODE)));
    }
}

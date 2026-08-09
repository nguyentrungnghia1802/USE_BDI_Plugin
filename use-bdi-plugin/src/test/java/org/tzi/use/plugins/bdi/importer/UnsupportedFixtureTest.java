package org.tzi.use.plugins.bdi.importer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.tzi.use.plugins.bdi.application.BdiImportService;
import org.tzi.use.plugins.bdi.application.BdiImportSnapshot;
import org.tzi.use.plugins.bdi.model.ir.AgentModel;
import org.tzi.use.plugins.bdi.model.ir.ContextUnsupported;
import org.tzi.use.plugins.bdi.model.ir.UnsupportedFeature;
import org.tzi.use.plugins.bdi.problems.BdiProblem;
import org.tzi.use.plugins.bdi.problems.BdiProblemCollector;
import org.tzi.use.plugins.bdi.problems.BdiProblemSeverity;

class UnsupportedFixtureTest {
    @Test
    void retainsRecognizedRelationalContextAsUnsupportedEvidence() throws Exception {
        Path source = fixture("fixtures/asl/unsupported/relational-context.asl");

        BdiImportSnapshot snapshot = new BdiImportService().importFiles(List.of(source));
        AgentModel model = snapshot.models().get(0);

        assertTrue(snapshot.diagnostics().isEmpty(), "fixture must parse successfully");
        assertTrue(model.plans().get(0).context().orElseThrow() instanceof ContextUnsupported);
        assertEquals(1, model.unsupportedFeatures().size());
        UnsupportedFeature feature = model.unsupportedFeatures().get(0);
        assertEquals(UnsupportedFeature.CODE, feature.code());
        assertEquals("relational-expression", feature.kind());
        assertTrue(feature.subject().contains(">"));
        assertEquals(4, feature.sourceSpan().beginLine());
        assertEquals(4, feature.sourceSpan().endLine());

        List<BdiProblem> problems = BdiProblemCollector.collect(snapshot);
        BdiProblem problem = problems.stream()
                .filter(value -> value.code().equals(UnsupportedFeature.CODE))
                .findFirst()
                .orElseThrow();
        assertEquals(BdiProblemSeverity.WARNING, problem.severity());
        assertEquals(4, problem.line());
        assertEquals("Unsupported feature", problem.group());
        assertTrue(problem.message().contains("relational-expression"));
        assertTrue(problems.stream().noneMatch(value -> value.code().equals(AslDiagnostic.SYNTAX_ERROR_CODE)));
    }

    private static Path fixture(String name) throws URISyntaxException {
        URL resource = UnsupportedFixtureTest.class.getClassLoader().getResource(name);
        if (resource == null) {
            throw new IllegalStateException("Missing test fixture: " + name);
        }
        return Path.of(resource.toURI());
    }
}

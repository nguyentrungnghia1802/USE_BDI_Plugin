package org.tzi.use.plugins.bdi.model.ir;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.tzi.use.plugins.bdi.importer.AslImportResult;
import org.tzi.use.plugins.bdi.importer.AslParseSummary;
import org.tzi.use.plugins.bdi.importer.JasonAslParserAdapter;

class AgentModelTest {
    private final JasonAslParserAdapter parser = new JasonAslParserAdapter();
    private final AslAgentModelNormalizer normalizer = new AslAgentModelNormalizer();

    @Test
    void normalizesParserSummaryIntoRootModel() throws Exception {
        Path source = fixture("fixtures/asl/valid/minimal.asl");
        AslParseSummary summary = parser.parse(source);

        AgentModel model = normalizer.normalize(summary);

        assertEquals(source.toAbsolutePath().normalize(), model.source());
        assertEquals("3.3.0", model.parserVersion());
        assertEquals(1, model.beliefCount());
        assertEquals(1, model.goalCount());
        assertEquals(1, model.planCount());
        assertEquals(3, model.elementCount());
    }

    @Test
    void normalizesSuccessfulFilesInOrderWithoutFailedFileModels() throws Exception {
        AslParseSummary first = parser.parse(fixture("fixtures/asl/valid/minimal.asl"));
        AslParseSummary second = parser.parse(fixture("fixtures/asl/valid/review-agent.asl"));

        List<AgentModel> models = normalizer.normalize(new AslImportResult(List.of(first, second)));

        assertEquals(List.of(first.source(), second.source()), models.stream().map(AgentModel::source).toList());
        assertEquals(2, models.size());
        assertThrows(UnsupportedOperationException.class, () -> models.clear());
    }

    @Test
    void materializesJasonAstIntoBeliefGoalPlanTree() throws Exception {
        Path source = fixture("fixtures/asl/valid/minimal.asl");

        AgentModel model = parser.parseModel(source);

        assertTrue(model.isMaterialized());
        assertEquals(1, model.beliefs().size());
        assertEquals("ready", model.beliefs().get(0).literal().render());
        assertEquals(1, model.goals().size());
        assertEquals("start", model.goals().get(0).literal().render());
        assertEquals(1, model.plans().size());
        PlanModel plan = model.plans().get(0);
        assertEquals("", plan.label());
        assertEquals(TriggerModel.TriggerOperator.ADD, plan.trigger().operator());
        assertEquals(TriggerModel.TriggerType.ACHIEVE, plan.trigger().type());
        assertEquals("start", plan.trigger().term().render());
        assertTrue(plan.context().isPresent());
        assertEquals("ready", ((ContextLiteral) plan.context().orElseThrow()).literal().render());
        assertEquals(1, plan.steps().size());
        assertTrue(plan.steps().get(0) instanceof InternalActionStepModel);
        assertEquals(4, plan.sourceSpan().beginLine());
        assertEquals(5, plan.sourceSpan().endLine());
        assertTrue(model.unsupportedFeatures().isEmpty());
    }

    @Test
    void materializesSmartQueueFixtureWithoutUnsupportedNodes() throws Exception {
        AgentModel model = parser.parseModel(fixture("fixtures/smartqueue/Smart_manager_agent.asl"));

        assertTrue(model.isMaterialized());
        assertEquals(9, model.beliefCount());
        assertEquals(1, model.goalCount());
        assertEquals(5, model.planCount());
        assertEquals(List.of(3, 3, 2, 9, 2), model.plans().stream().map(plan -> plan.steps().size()).toList());
        assertTrue(model.unsupportedFeatures().isEmpty());
    }

    private static Path fixture(String name) throws URISyntaxException {
        URL resource = AgentModelTest.class.getClassLoader().getResource(name);
        if (resource == null) {
            throw new IllegalStateException("Missing test fixture: " + name);
        }
        return Path.of(resource.toURI());
    }
}

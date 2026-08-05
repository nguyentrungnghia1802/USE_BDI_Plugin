package org.tzi.use.plugins.bdi.index;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.tzi.use.plugins.bdi.importer.JasonAslParserAdapter;
import org.tzi.use.plugins.bdi.model.ir.AgentModel;
import org.tzi.use.plugins.bdi.model.ir.PlanModel;

class BdiIndexTest {
    private final JasonAslParserAdapter parser = new JasonAslParserAdapter();
    private final BdiIndexBuilder builder = new BdiIndexBuilder();

    @Test
    void indexesGoalsActionsAndPredicateOccurrences() throws Exception {
        AgentModel model = parser.parseModel(fixture("fixtures/asl/valid/minimal.asl"));
        BdiIndex index = builder.build(model);

        assertEquals(BdiMetamodelVersion.CURRENT, index.metamodelVersion());
        assertEquals(1, index.supportingPlans(new PredicateSignature("start", 0)).size());
        assertEquals(1, index.actionCalls(new PredicateSignature(".print", 1)).size());
        assertEquals(ActionCallSite.ActionKind.INTERNAL_ACTION,
                index.actionCalls(new PredicateSignature(".print", 1)).get(0).kind());
        assertEquals(2, index.predicateReferences(new PredicateSignature("ready", 0)).size());
        assertEquals(2, index.predicateReferences(new PredicateSignature("start", 0)).size());
        assertTrue(index.objectReferencesByName().isEmpty());
        assertTrue(index.duplicatePlanLabels().isEmpty());
    }

    @Test
    void indexesSendReceiversAndGroundObjectSymbols() throws Exception {
        AgentModel model = parser.parseModel(fixture("fixtures/smartqueue/Smart_manager_agent.asl"));
        BdiIndex index = builder.build(model);

        assertFalse(index.actionCalls(new PredicateSignature(".send", 3)).isEmpty());
        assertFalse(index.agentReferences("counter_agent").isEmpty());
        assertFalse(index.agentReferences("staff_agent").isEmpty());
        assertFalse(index.objectReferences("queue1").isEmpty());
        assertFalse(index.objectReferences("customer1").isEmpty());
        assertTrue(index.agentReferences("counter_agent").stream().allMatch(reference -> !reference.dynamic()));
    }

    @Test
    void detectsDuplicateExplicitLabelsInNormalizedIr() throws Exception {
        AgentModel parsed = parser.parseModel(fixture("fixtures/asl/valid/minimal.asl"));
        PlanModel original = parsed.plans().get(0);
        PlanModel first = new PlanModel(
                "duplicate",
                original.trigger(),
                original.context(),
                original.steps(),
                original.sourceSpan());
        PlanModel second = new PlanModel(
                "duplicate",
                original.trigger(),
                original.context(),
                original.steps(),
                original.sourceSpan());
        AgentModel model = new AgentModel(
                parsed.source(),
                parsed.parserVersion(),
                parsed.beliefCount(),
                parsed.goalCount(),
                2,
                parsed.beliefs(),
                parsed.goals(),
                java.util.List.of(first, second),
                parsed.unsupportedFeatures());
        BdiIndex index = builder.build(model);

        assertEquals(1, index.duplicatePlanLabels().size());
        DuplicatePlanLabel duplicate = index.duplicatePlanLabels().get(0);
        assertEquals("duplicate", duplicate.label());
        assertEquals(2, duplicate.occurrences().size());
        assertEquals(2, model.plans().size());
    }

    @Test
    void exposesImmutableIndexCollections() throws Exception {
        BdiIndex index = builder.build(parser.parseModel(fixture("fixtures/asl/valid/minimal.asl")));

        assertThrows(UnsupportedOperationException.class, () -> index.models().clear());
        assertThrows(UnsupportedOperationException.class, () -> index.allActionCallSites().clear());
        assertThrows(UnsupportedOperationException.class, () -> index
                .predicateReferencesBySignature()
                .get(new PredicateSignature("ready", 0))
                .clear());
    }

    private static Path fixture(String name) throws URISyntaxException {
        URL resource = BdiIndexTest.class.getClassLoader().getResource(name);
        if (resource == null) {
            throw new IllegalStateException("Missing test fixture: " + name);
        }
        return Path.of(resource.toURI());
    }
}

package org.tzi.use.plugins.bdi.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.tzi.use.plugins.bdi.diagram.DiagramEdge;
import org.tzi.use.plugins.bdi.diagram.DiagramEdgeType;
import org.tzi.use.plugins.bdi.diagram.DiagramModel;
import org.tzi.use.plugins.bdi.diagram.DiagramNode;
import org.tzi.use.plugins.bdi.diagram.DiagramNodeType;
import org.tzi.use.plugins.bdi.diagram.DiagramSelectionRef;

class DiagramModeProjectorTest {
    @Test
    void bdiPlanModeKeepsPlanStepsAndTheirOneBasedOrder() {
        DiagramNode agent = node(DiagramNodeType.AGENT, "agent", "auctioneer");
        DiagramNode goal = node(DiagramNodeType.GOAL, "goal", "sell");
        DiagramNode plan = node(DiagramNodeType.PLAN, "plan", "sell-plan");
        DiagramNode action = node(DiagramNodeType.ACTION, "action", "sell-step");
        DiagramNode uml = node(DiagramNodeType.UML_OPERATION, "uml", "Auctioneer.sell");
        DiagramModel model = new DiagramModel(
                List.of(agent, goal, plan, action, uml),
                List.of(
                        edge("pursues", DiagramEdgeType.PURSUES_GOAL, agent, goal, Map.of()),
                        edge("owns", DiagramEdgeType.OWNS, agent, plan, Map.of()),
                        edge("step-2", DiagramEdgeType.EXECUTES, plan, action, Map.of("order", "2")),
                        edge("maps", DiagramEdgeType.MAPS_TO, action, uml, Map.of())),
                List.of());

        DiagramModel projected = DiagramModeProjector.project(model, DiagramViewMode.BDI_PLAN);

        assertFalse(projected.nodes().contains(uml));
        assertEquals(4, projected.nodes().size());
        assertTrue(projected.edges().stream().anyMatch(edge -> edge.type() == DiagramEdgeType.EXECUTES
                && edge.attributes().get("order").equals("2")));
        assertTrue(projected.edges().stream().noneMatch(edge -> edge.type() == DiagramEdgeType.MAPS_TO));
    }

    @Test
    void mappingModeKeepsConfirmedMappingsExplicitGapsAndRelatedIssues() {
        DiagramNode action = node(DiagramNodeType.ACTION, "action", "sell-step");
        DiagramNode uml = node(DiagramNodeType.UML_OPERATION, "uml", "Auctioneer.sell");
        DiagramNode gap = node(DiagramNodeType.GAP, "gap", "missing receiver mapping");
        DiagramNode issue = node(DiagramNodeType.ISSUE, "issue", "MAP-002");
        DiagramModel model = new DiagramModel(
                List.of(action, uml, gap, issue),
                List.of(
                        edge("confirmed", DiagramEdgeType.MAPS_TO, action, uml, Map.of()),
                        edge("missing", DiagramEdgeType.MISSING_MAPPING, action, gap, Map.of()),
                        edge("issue", DiagramEdgeType.HAS_ISSUE, gap, issue, Map.of())),
                List.of());

        DiagramModel projected = DiagramModeProjector.project(model, DiagramViewMode.MAPPING);

        assertEquals(4, projected.nodes().size());
        assertEquals(3, projected.edges().size());
        assertTrue(projected.edges().stream().anyMatch(edge -> edge.type() == DiagramEdgeType.MAPS_TO));
        assertTrue(projected.edges().stream().anyMatch(edge -> edge.type() == DiagramEdgeType.MISSING_MAPPING));
        assertTrue(projected.edges().stream().anyMatch(edge -> edge.type() == DiagramEdgeType.HAS_ISSUE));
    }

    @Test
    void emptySourceRemainsEmptyForEveryMode() {
        DiagramModel empty = DiagramModel.empty();
        for (DiagramViewMode mode : DiagramViewMode.values()) {
            assertEquals(empty, DiagramModeProjector.project(empty, mode));
        }
    }

    private static DiagramNode node(DiagramNodeType type, String namespace, String reference) {
        return new DiagramNode(type, DiagramSelectionRef.of(namespace, reference), reference,
                Optional.empty(), Optional.empty(), Map.of());
    }

    private static DiagramEdge edge(
            String reference,
            DiagramEdgeType type,
            DiagramNode source,
            DiagramNode target,
            Map<String, String> attributes) {
        return new DiagramEdge(type, source.id(), target.id(),
                DiagramSelectionRef.of("mode-edge", reference), Optional.empty(), attributes);
    }
}

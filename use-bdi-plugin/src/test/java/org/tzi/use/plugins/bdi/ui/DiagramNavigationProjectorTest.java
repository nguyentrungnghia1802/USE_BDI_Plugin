package org.tzi.use.plugins.bdi.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.tzi.use.plugins.bdi.diagram.DiagramEdge;
import org.tzi.use.plugins.bdi.diagram.DiagramEdgeType;
import org.tzi.use.plugins.bdi.diagram.DiagramModel;
import org.tzi.use.plugins.bdi.diagram.DiagramNode;
import org.tzi.use.plugins.bdi.diagram.DiagramNodeType;
import org.tzi.use.plugins.bdi.diagram.DiagramSelectionRef;

class DiagramNavigationProjectorTest {
    @Test
    void hidesSelectedLayersWithoutChangingTheSource() {
        DiagramNode agent = node(DiagramNodeType.AGENT, "agent", Map.of("layer", "BDI"));
        DiagramNode role = node(DiagramNodeType.ROLE, "role", Map.of("layer", "ORGANIZATION"));
        DiagramNode artifact = node(DiagramNodeType.ARTIFACT, "artifact", Map.of("layer", "ENVIRONMENT"));
        DiagramNode uml = node(DiagramNodeType.UML_CLASS, "uml", Map.of("layer", "UML"));
        DiagramNode issue = node(DiagramNodeType.ISSUE, "issue", Map.of("layer", "ISSUE"));
        DiagramModel source = new DiagramModel(List.of(agent, role, artifact, uml, issue), List.of(), List.of());

        DiagramModel visible = DiagramNavigationProjector.project(
                source, Set.of(DiagramLayer.ORGANIZATION, DiagramLayer.ISSUES), Optional.empty());

        assertEquals(3, visible.nodes().size());
        assertFalse(visible.nodes().contains(role));
        assertFalse(visible.nodes().contains(issue));
        assertEquals(5, source.nodes().size());
    }

    @Test
    void focusesOneHopNeighborhoodAndShortestIssueEvidencePath() {
        DiagramNode agent = node(DiagramNodeType.AGENT, "agent", Map.of());
        DiagramNode plan = node(DiagramNodeType.PLAN, "plan", Map.of());
        DiagramNode action = node(DiagramNodeType.ACTION, "action", Map.of());
        DiagramNode uml = node(DiagramNodeType.UML_OPERATION, "uml", Map.of());
        DiagramNode issue = node(DiagramNodeType.ISSUE, "issue", Map.of());
        DiagramNode unrelated = node(DiagramNodeType.BELIEF, "unrelated", Map.of());
        DiagramModel source = new DiagramModel(
                List.of(agent, plan, action, uml, issue, unrelated),
                List.of(
                        edge("owns", DiagramEdgeType.OWNS, agent, plan),
                        edge("executes", DiagramEdgeType.EXECUTES, plan, action),
                        edge("maps", DiagramEdgeType.MAPS_TO, action, uml),
                        edge("issue", DiagramEdgeType.HAS_ISSUE, uml, issue)),
                List.of());

        DiagramModel focused = DiagramNavigationProjector.project(source, Set.of(), Optional.of(plan.id()));

        assertTrue(focused.nodes().containsAll(List.of(agent, plan, action, uml, issue)));
        assertFalse(focused.nodes().contains(unrelated));
        assertEquals(source, new DiagramModel(source.nodes(), source.edges(), source.groups()));
    }

    @Test
    void missingOrHiddenFocusFallsBackToTheVisibleLayerProjection() {
        DiagramNode agent = node(DiagramNodeType.AGENT, "agent", Map.of());
        DiagramNode role = node(DiagramNodeType.ROLE, "role", Map.of("layer", "ORGANIZATION"));
        DiagramModel source = new DiagramModel(List.of(agent, role), List.of(), List.of());

        DiagramModel hiddenFocus = DiagramNavigationProjector.project(
                source, Set.of(DiagramLayer.ORGANIZATION), Optional.of(role.id()));
        DiagramModel unknownFocus = DiagramNavigationProjector.project(
                source, Set.of(), Optional.of("unknown"));

        assertEquals(List.of(agent), hiddenFocus.nodes());
        assertEquals(source, unknownFocus);
    }

    private static DiagramNode node(DiagramNodeType type, String reference, Map<String, String> attributes) {
        return new DiagramNode(type, DiagramSelectionRef.of("navigation-node", reference), reference,
                Optional.empty(), Optional.empty(), attributes);
    }

    private static DiagramEdge edge(String reference, DiagramEdgeType type, DiagramNode source, DiagramNode target) {
        return new DiagramEdge(type, source.id(), target.id(),
                DiagramSelectionRef.of("navigation-edge", reference), Optional.empty(), Map.of());
    }
}

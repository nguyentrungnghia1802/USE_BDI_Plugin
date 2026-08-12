package org.tzi.use.plugins.bdi.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import javax.swing.SwingUtilities;

import org.junit.jupiter.api.Test;
import org.tzi.use.plugins.bdi.diagram.DiagramEdge;
import org.tzi.use.plugins.bdi.diagram.DiagramEdgeType;
import org.tzi.use.plugins.bdi.diagram.DiagramIssueMarker;
import org.tzi.use.plugins.bdi.diagram.DiagramModel;
import org.tzi.use.plugins.bdi.diagram.DiagramNode;
import org.tzi.use.plugins.bdi.diagram.DiagramNodeType;
import org.tzi.use.plugins.bdi.diagram.DiagramSelectionRef;
import org.tzi.use.plugins.bdi.validation.IssueCertainty;
import org.tzi.use.plugins.bdi.validation.IssueSeverity;
import org.tzi.use.plugins.bdi.validation.IssueStatus;

class BdiDiagramPanelTest {
    @Test
    void startsWithAnExplicitEmptyState() {
        BdiDiagramPanel panel = new BdiDiagramPanel();

        assertTrue(panel.modelForTest().nodes().isEmpty());
        assertTrue(panel.stateForTest().getText().contains("No diagram data"));
        assertTrue(panel.canvasForTest().getPreferredSize().width >= 640);
    }

    @Test
    void laysOutPopulatedModelAndSupportsSelectionZoomFitAndReset() throws Exception {
        DiagramNode agent = node(DiagramNodeType.AGENT, "agent", "auctioneer");
        DiagramNode goal = node(DiagramNodeType.GOAL, "goal", "sell(Item)");
        DiagramModel model = new DiagramModel(
                List.of(agent, goal),
                List.of(new DiagramEdge(
                        DiagramEdgeType.PURSUES_GOAL,
                        agent.id(), goal.id(),
                        DiagramSelectionRef.of("test-edge", "agent-goal"),
                        Optional.of("pursues"), Map.of())),
                List.of());
        BdiDiagramPanel panel = new BdiDiagramPanel();
        AtomicReference<DiagramNode> selected = new AtomicReference<>();
        panel.setSelectionListener(selected::set);

        SwingUtilities.invokeAndWait(() -> panel.setDiagram(model));
        waitForLayout(panel);
        assertEquals(model, panel.modelForTest());
        assertTrue(panel.canvasForTest().layoutReadyForTest());
        assertTrue(panel.stateForTest().getText().contains("2 node(s)"));

        SwingUtilities.invokeAndWait(() -> panel.canvasForTest().selectNodeForTest(agent.id()));
        assertEquals(agent, selected.get());
        assertEquals(agent, panel.canvasForTest().selectedNodeForTest().orElseThrow());

        for (int index = 0; index < 20; index++) {
            SwingUtilities.invokeAndWait(() -> panel.zoomOutForTest().doClick());
        }
        assertEquals(BdiDiagramCanvas.MIN_ZOOM, panel.canvasForTest().zoomForTest(), 0.0001);
        for (int index = 0; index < 40; index++) {
            SwingUtilities.invokeAndWait(() -> panel.zoomInForTest().doClick());
        }
        assertEquals(BdiDiagramCanvas.MAX_ZOOM, panel.canvasForTest().zoomForTest(), 0.0001);
        SwingUtilities.invokeAndWait(() -> {
            panel.fitForTest().doClick();
            panel.resetForTest().doClick();
        });
        assertEquals(1.0, panel.canvasForTest().zoomForTest(), 0.0001);
    }

    @Test
    void keepsLargeModelsScrollableThroughPreferredSize() throws Exception {
        List<DiagramNode> nodes = new ArrayList<>();
        for (int index = 0; index < 30; index++) {
            nodes.add(node(DiagramNodeType.ACTION, "action", "step-" + index));
        }
        BdiDiagramPanel panel = new BdiDiagramPanel();

        SwingUtilities.invokeAndWait(() -> panel.setDiagram(new DiagramModel(nodes, List.of(), List.of())));
        waitForLayout(panel);

        assertTrue(panel.canvasForTest().getPreferredSize().height > 900);
        assertFalse(panel.stateForTest().getText().contains("failed"));
    }

    @Test
    void switchesModeWithoutDroppingSelectionOrSourceModel() throws Exception {
        DiagramNode agent = node(DiagramNodeType.AGENT, "agent", "auctioneer");
        DiagramNode action = node(DiagramNodeType.ACTION, "action", "sell-step");
        DiagramModel source = new DiagramModel(List.of(agent, action), List.of(), List.of());
        BdiDiagramPanel panel = new BdiDiagramPanel();

        SwingUtilities.invokeAndWait(() -> panel.setDiagram(source));
        waitForLayout(panel);
        SwingUtilities.invokeAndWait(() -> panel.canvasForTest().selectNodeForTest(agent.id()));
        SwingUtilities.invokeAndWait(() -> panel.setViewMode(DiagramViewMode.BDI_PLAN));
        waitForLayout(panel);

        assertEquals(DiagramViewMode.BDI_PLAN, panel.modeForTest());
        assertEquals(source, panel.sourceModelForTest());
        assertEquals(agent, panel.canvasForTest().selectedNodeForTest().orElseThrow());
        SwingUtilities.invokeAndWait(() -> panel.fitForTest().doClick());
    }

    @Test
    void highlightsIssuePathAndSelectsTheIssueNode() throws Exception {
        DiagramNode element = node(DiagramNodeType.TRACE_ELEMENT, "element", "action");
        DiagramNode target = node(DiagramNodeType.TRACE_TARGET, "target", "Auction.sell");
        DiagramNode issue = new DiagramNode(
                DiagramNodeType.ISSUE,
                DiagramSelectionRef.of("issue", "MAP-003"),
                "MAP-003: mismatch",
                Optional.empty(),
                Optional.of(new DiagramIssueMarker(
                        "MAP-003", IssueSeverity.ERROR, IssueStatus.OPEN,
                        IssueCertainty.CONFIRMED, List.of("precondition evidence"))),
                Map.of());
        DiagramModel model = new DiagramModel(
                List.of(element, target, issue),
                List.of(
                        new DiagramEdge(DiagramEdgeType.MAPS_TO, element.id(), target.id(),
                                DiagramSelectionRef.of("edge", "mapping"), Optional.empty(), Map.of()),
                        new DiagramEdge(DiagramEdgeType.HAS_ISSUE, target.id(), issue.id(),
                                DiagramSelectionRef.of("edge", "issue"), Optional.empty(), Map.of())),
                List.of());
        BdiDiagramPanel panel = new BdiDiagramPanel();

        SwingUtilities.invokeAndWait(() -> panel.setDiagram(model));
        waitForLayout(panel);
        SwingUtilities.invokeAndWait(() -> {
            assertTrue(panel.highlightIssue("MAP-003"));
        });

        assertEquals(3, panel.highlightedNodeIdsForTest().size());
        assertEquals(2, panel.highlightedEdgeIdsForTest().size());
        assertEquals(issue, panel.canvasForTest().selectedNodeForTest().orElseThrow());
        assertTrue(panel.stateForTest().getText().contains("MAP-003"));
    }

    @Test
    void focusesSelectedPlanAndRestoresFullGraphOnReset() throws Exception {
        DiagramNode agent = node(DiagramNodeType.AGENT, "agent", "auctioneer");
        DiagramNode plan = node(DiagramNodeType.PLAN, "plan", "sell-plan");
        DiagramNode action = node(DiagramNodeType.ACTION, "action", "sell-step");
        DiagramNode uml = layeredNode(DiagramNodeType.UML_OPERATION, "uml", "Auctioneer.sell", "UML");
        DiagramNode issue = node(DiagramNodeType.ISSUE, "issue", "SIG-001");
        DiagramNode unrelated = node(DiagramNodeType.BELIEF, "belief", "unrelated");
        DiagramModel source = new DiagramModel(
                List.of(agent, plan, action, uml, issue, unrelated),
                List.of(
                        edge("owns", DiagramEdgeType.OWNS, agent, plan),
                        edge("step", DiagramEdgeType.EXECUTES, plan, action),
                        edge("mapping", DiagramEdgeType.MAPS_TO, action, uml),
                        edge("issue", DiagramEdgeType.HAS_ISSUE, uml, issue)),
                List.of());
        BdiDiagramPanel panel = new BdiDiagramPanel();

        SwingUtilities.invokeAndWait(() -> panel.setDiagram(source));
        waitForLayout(panel);
        SwingUtilities.invokeAndWait(() -> panel.canvasForTest().selectNodeForTest(plan.id()));
        assertTrue(panel.focusGoalPlanForTest().isEnabled());
        SwingUtilities.invokeAndWait(() -> panel.focusGoalPlanForTest().doClick());
        waitForLayout(panel);

        assertEquals(source, panel.sourceModelForTest());
        assertTrue(panel.modelForTest().nodes().containsAll(List.of(agent, plan, action, uml, issue)));
        assertFalse(panel.modelForTest().nodes().contains(unrelated));
        assertTrue(panel.modelForTest().nodes().size() < source.nodes().size());

        SwingUtilities.invokeAndWait(() -> panel.resetForTest().doClick());
        waitForLayout(panel);
        assertEquals(source, panel.modelForTest());
        assertEquals(DiagramViewMode.ALL, panel.modeForTest());
        assertTrue(panel.showIssuesForTest().isSelected());
        assertTrue(panel.showUmlOclForTest().isSelected());
    }

    @Test
    void layerTogglesFilterOnlyTheVisibleProjection() throws Exception {
        DiagramNode agent = node(DiagramNodeType.AGENT, "agent", "auctioneer");
        DiagramNode role = layeredNode(DiagramNodeType.ROLE, "role", "auctioneer-role", "ORGANIZATION");
        DiagramNode artifact = layeredNode(DiagramNodeType.ARTIFACT, "artifact", "board", "ENVIRONMENT");
        DiagramNode uml = layeredNode(DiagramNodeType.UML_CLASS, "uml", "Auctioneer", "UML");
        DiagramNode issue = layeredNode(DiagramNodeType.ISSUE, "issue", "MAP-001", "ISSUE");
        DiagramModel source = new DiagramModel(List.of(agent, role, artifact, uml, issue), List.of(), List.of());
        BdiDiagramPanel panel = new BdiDiagramPanel();

        SwingUtilities.invokeAndWait(() -> panel.setDiagram(source));
        waitForLayout(panel);
        assertTrue(panel.showOrganizationForTest().isEnabled());
        assertTrue(panel.showEnvironmentForTest().isEnabled());
        SwingUtilities.invokeAndWait(() -> {
            panel.showOrganizationForTest().doClick();
            panel.showEnvironmentForTest().doClick();
            panel.showUmlOclForTest().doClick();
            panel.showIssuesForTest().doClick();
        });
        waitForLayout(panel);

        assertEquals(List.of(agent), panel.modelForTest().nodes());
        assertEquals(source, panel.sourceModelForTest());
    }

    private static DiagramNode node(DiagramNodeType type, String namespace, String reference) {
        return new DiagramNode(type, DiagramSelectionRef.of(namespace, reference), reference,
                Optional.empty(), Optional.empty(), Map.of());
    }

    private static DiagramNode layeredNode(
            DiagramNodeType type, String namespace, String reference, String layer) {
        return new DiagramNode(type, DiagramSelectionRef.of(namespace, reference), reference,
                Optional.empty(), Optional.empty(), Map.of("layer", layer));
    }

    private static DiagramEdge edge(
            String reference, DiagramEdgeType type, DiagramNode source, DiagramNode target) {
        return new DiagramEdge(type, source.id(), target.id(),
                DiagramSelectionRef.of("panel-edge", reference), Optional.empty(), Map.of());
    }

    private static void waitForLayout(BdiDiagramPanel panel) throws Exception {
        long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(10);
        while (!panel.canvasForTest().layoutReadyForTest() && System.nanoTime() < deadline) {
            Thread.sleep(20);
        }
        SwingUtilities.invokeAndWait(() -> {
        });
        assertTrue(panel.canvasForTest().layoutReadyForTest(), "diagram layout did not complete");
    }
}

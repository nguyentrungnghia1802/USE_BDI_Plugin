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
import org.tzi.use.plugins.bdi.diagram.DiagramModel;
import org.tzi.use.plugins.bdi.diagram.DiagramNode;
import org.tzi.use.plugins.bdi.diagram.DiagramNodeType;
import org.tzi.use.plugins.bdi.diagram.DiagramSelectionRef;

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

    private static DiagramNode node(DiagramNodeType type, String namespace, String reference) {
        return new DiagramNode(type, DiagramSelectionRef.of(namespace, reference), reference,
                Optional.empty(), Optional.empty(), Map.of());
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

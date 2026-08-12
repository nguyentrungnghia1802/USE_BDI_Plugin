package org.tzi.use.plugins.bdi.ui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;

import org.tzi.use.plugins.bdi.diagram.DiagramModel;
import org.tzi.use.plugins.bdi.diagram.DiagramNode;

/** Read-only diagram controls and asynchronous canvas layout. */
@SuppressWarnings("serial")
public final class BdiDiagramPanel extends JPanel {
    private final BdiDiagramCanvas canvas;
    private final JLabel state;
    private final JButton zoomIn;
    private final JButton zoomOut;
    private final JButton fit;
    private final JButton reset;
    private final JComboBox<DiagramViewMode> modeSelector;
    private final AtomicLong generation = new AtomicLong();
    private DiagramModel model = DiagramModel.empty();
    private DiagramModel sourceModel = DiagramModel.empty();
    private DiagramViewMode mode = DiagramViewMode.ALL;
    private String highlightedIssueRuleId;
    private SwingWorker<BdiDiagramLayout.Layout, Void> layoutWorker;

    public BdiDiagramPanel() {
        super(new BorderLayout(6, 6));
        canvas = new BdiDiagramCanvas();
        state = new JLabel("No diagram data");
        zoomIn = button("+", "Zoom in");
        zoomOut = button("-", "Zoom out");
        fit = button("Fit", "Fit diagram to the viewport");
        reset = button("Reset", "Reset zoom and pan");
        modeSelector = new JComboBox<>(DiagramViewMode.values());
        modeSelector.setToolTipText("Choose a presentation-only diagram view");
        modeSelector.addActionListener(event -> {
            DiagramViewMode selected = (DiagramViewMode) modeSelector.getSelectedItem();
            if (selected != null) {
                setViewMode(selected);
            }
        });
        zoomIn.addActionListener(event -> canvas.zoomIn());
        zoomOut.addActionListener(event -> canvas.zoomOut());
        fit.addActionListener(event -> canvas.fitToScreen());
        reset.addActionListener(event -> canvas.resetView());

        JPanel controls = new JPanel(new BorderLayout(6, 0));
        JPanel actions = new JPanel();
        actions.add(zoomIn);
        actions.add(zoomOut);
        actions.add(fit);
        actions.add(reset);
        actions.add(new JLabel("View:"));
        actions.add(modeSelector);
        controls.add(actions, BorderLayout.WEST);
        controls.add(state, BorderLayout.CENTER);
        controls.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
        add(controls, BorderLayout.NORTH);

        JScrollPane scroll = new JScrollPane(canvas);
        scroll.setPreferredSize(new Dimension(900, 520));
        add(scroll, BorderLayout.CENTER);
    }

    public void setDiagram(DiagramModel diagram) {
        Objects.requireNonNull(diagram, "diagram");
        if (!SwingUtilities.isEventDispatchThread()) {
            SwingUtilities.invokeLater(() -> setDiagram(diagram));
            return;
        }
        sourceModel = diagram;
        publishCurrentMode();
    }

    public void setViewMode(DiagramViewMode requestedMode) {
        Objects.requireNonNull(requestedMode, "requestedMode");
        if (!SwingUtilities.isEventDispatchThread()) {
            SwingUtilities.invokeLater(() -> setViewMode(requestedMode));
            return;
        }
        mode = requestedMode;
        if (modeSelector.getSelectedItem() != requestedMode) {
            modeSelector.setSelectedItem(requestedMode);
        }
        publishCurrentMode();
    }

    private void publishCurrentMode() {
        DiagramModel visibleDiagram = DiagramModeProjector.project(sourceModel, mode);
        model = visibleDiagram;
        canvas.setModel(visibleDiagram);
        applyCurrentHighlight();
        long request = generation.incrementAndGet();
        state.setText(visibleDiagram.nodes().isEmpty()
                ? "No diagram data"
                : mode + ": laying out " + visibleDiagram.nodes().size() + " node(s)...");
        if (layoutWorker != null && !layoutWorker.isDone()) {
            layoutWorker.cancel(true);
        }
        layoutWorker = new SwingWorker<>() {
            @Override
            protected BdiDiagramLayout.Layout doInBackground() {
                return BdiDiagramLayout.compute(visibleDiagram);
            }

            @Override
            protected void done() {
                if (request != generation.get() || isCancelled()) {
                    return;
                }
                try {
                    canvas.setLayoutSnapshot(get());
                    state.setText(visibleDiagram.nodes().isEmpty()
                            ? "No diagram data"
                            : mode + ": " + visibleDiagram.nodes().size() + " node(s), "
                                    + visibleDiagram.edges().size() + " edge(s)");
                } catch (Exception error) {
                    state.setText("Diagram layout failed: " + error.getMessage());
                }
            }
        };
        layoutWorker.execute();
    }

    public void setUnavailable(String message) {
        Objects.requireNonNull(message, "message");
        if (!SwingUtilities.isEventDispatchThread()) {
            SwingUtilities.invokeLater(() -> setUnavailable(message));
            return;
        }
        generation.incrementAndGet();
        if (layoutWorker != null && !layoutWorker.isDone()) {
            layoutWorker.cancel(true);
        }
        sourceModel = DiagramModel.empty();
        model = sourceModel;
        highlightedIssueRuleId = null;
        canvas.setModel(model);
        canvas.setHighlights(Set.of(), Set.of());
        state.setText("Diagram unavailable: " + message);
    }

    /** Highlights an issue evidence path from the current immutable diagram projection. */
    public boolean highlightIssue(String ruleId) {
        Objects.requireNonNull(ruleId, "ruleId");
        if (!SwingUtilities.isEventDispatchThread()) {
            SwingUtilities.invokeLater(() -> highlightIssue(ruleId));
            return false;
        }
        highlightedIssueRuleId = ruleId;
        DiagramHighlightPath.Highlight highlight = DiagramHighlightPath.forIssue(model, ruleId);
        canvas.setHighlights(highlight.nodeIds(), highlight.edgeIds());
        if (highlight.isEmpty()) {
            state.setText("No diagram evidence for issue " + ruleId);
            return false;
        }
        model.nodes().stream()
                .filter(node -> highlight.nodeIds().contains(node.id()))
                .filter(node -> node.type() == org.tzi.use.plugins.bdi.diagram.DiagramNodeType.ISSUE)
                .findFirst()
                .ifPresent(node -> canvas.selectNode(node.id()));
        state.setText("Highlighted evidence for " + ruleId + " (" + highlight.nodeIds().size()
                + " node(s), " + highlight.edgeIds().size() + " edge(s))");
        return true;
    }

    public void clearHighlight() {
        if (!SwingUtilities.isEventDispatchThread()) {
            SwingUtilities.invokeLater(this::clearHighlight);
            return;
        }
        highlightedIssueRuleId = null;
        canvas.setHighlights(Set.of(), Set.of());
    }

    public void setSelectionListener(Consumer<DiagramNode> listener) {
        canvas.setSelectionListener(Objects.requireNonNull(listener, "listener"));
    }

    DiagramModel modelForTest() {
        return model;
    }

    DiagramModel sourceModelForTest() {
        return sourceModel;
    }

    DiagramViewMode modeForTest() {
        return mode;
    }

    JComboBox<DiagramViewMode> modeSelectorForTest() {
        return modeSelector;
    }

    BdiDiagramCanvas canvasForTest() {
        return canvas;
    }

    JLabel stateForTest() {
        return state;
    }

    Set<String> highlightedNodeIdsForTest() {
        return canvas.highlightedNodeIdsForTest();
    }

    Set<String> highlightedEdgeIdsForTest() {
        return canvas.highlightedEdgeIdsForTest();
    }

    JButton zoomInForTest() {
        return zoomIn;
    }

    JButton zoomOutForTest() {
        return zoomOut;
    }

    JButton fitForTest() {
        return fit;
    }

    JButton resetForTest() {
        return reset;
    }

    private void applyCurrentHighlight() {
        if (highlightedIssueRuleId == null) {
            canvas.setHighlights(Set.of(), Set.of());
            return;
        }
        DiagramHighlightPath.Highlight highlight = DiagramHighlightPath.forIssue(model, highlightedIssueRuleId);
        canvas.setHighlights(highlight.nodeIds(), highlight.edgeIds());
    }

    private static JButton button(String label, String tooltip) {
        JButton button = new JButton(label);
        button.setToolTipText(tooltip);
        return button;
    }
}

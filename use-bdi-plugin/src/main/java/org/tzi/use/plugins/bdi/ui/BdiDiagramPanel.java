package org.tzi.use.plugins.bdi.ui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.EnumSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToggleButton;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.tzi.use.plugins.bdi.diagram.DiagramModel;
import org.tzi.use.plugins.bdi.diagram.DiagramNode;
import org.tzi.use.plugins.bdi.diagram.DiagramNodeType;

/** Read-only diagram controls and asynchronous canvas layout. */
@SuppressWarnings("serial")
public final class BdiDiagramPanel extends JPanel {
    private final BdiDiagramCanvas canvas;
    private final JLabel state;
    private final JButton zoomIn;
    private final JButton zoomOut;
    private final JButton fit;
    private final JButton reset;
    private final JButton exportSvg;
    private final JButton focusAgent;
    private final JButton focusGoalPlan;
    private final JToggleButton showIssues;
    private final JToggleButton showUmlOcl;
    private final JToggleButton showOrganization;
    private final JToggleButton showEnvironment;
    private final JComboBox<DiagramViewMode> modeSelector;
    private final AtomicLong generation = new AtomicLong();
    private DiagramModel model = DiagramModel.empty();
    private DiagramModel sourceModel = DiagramModel.empty();
    private DiagramViewMode mode = DiagramViewMode.ALL;
    private final Set<DiagramLayer> hiddenLayers = EnumSet.noneOf(DiagramLayer.class);
    private Consumer<DiagramNode> selectionListener = ignored -> {
    };
    private String selectedSourceNodeId;
    private String focusNodeId;
    private String highlightedIssueRuleId;
    private boolean updatingControls;
    private boolean fitAfterLayout;
    private SwingWorker<BdiDiagramLayout.Layout, Void> layoutWorker;

    public BdiDiagramPanel() {
        super(new BorderLayout(6, 6));
        canvas = new BdiDiagramCanvas();
        state = new JLabel("No diagram data");
        zoomIn = button("Zoom +", "Zoom in");
        zoomOut = button("Zoom -", "Zoom out");
        fit = button("Fit", "Fit diagram to the viewport");
        reset = button("Reset", "Restore the full diagram, layers, zoom, and pan");
        exportSvg = button("Export SVG...", "Export the current filtered diagram as deterministic SVG");
        focusAgent = button("Focus Agent", "Show the selected agent and its bounded neighborhood");
        focusGoalPlan = button("Focus Goal/Plan", "Show the selected goal or plan and its bounded neighborhood");
        showIssues = toggle("Issues", "Show or hide issue nodes");
        showUmlOcl = toggle("UML/OCL", "Show or hide UML and OCL targets");
        showOrganization = toggle("Organization", "Show or hide static organization nodes");
        showEnvironment = toggle("Environment", "Show or hide static environment nodes");
        modeSelector = new JComboBox<>(DiagramViewMode.values());
        modeSelector.setToolTipText("Choose a presentation-only diagram view");
        modeSelector.addActionListener(event -> {
            if (updatingControls) {
                return;
            }
            DiagramViewMode selected = (DiagramViewMode) modeSelector.getSelectedItem();
            if (selected != null) {
                setViewMode(selected);
            }
        });
        canvas.setSelectionListener(this::handleSelection);
        zoomIn.addActionListener(event -> canvas.zoomIn());
        zoomOut.addActionListener(event -> canvas.zoomOut());
        fit.addActionListener(event -> canvas.fitToScreen());
        reset.addActionListener(event -> resetPresentation());
        exportSvg.addActionListener(event -> chooseExportSvg());
        focusAgent.addActionListener(event -> focusSelected(Set.of(DiagramNodeType.AGENT)));
        focusGoalPlan.addActionListener(event -> focusSelected(Set.of(
                DiagramNodeType.GOAL,
                DiagramNodeType.PLAN)));
        showIssues.addActionListener(event -> setLayerVisible(DiagramLayer.ISSUES, showIssues.isSelected()));
        showUmlOcl.addActionListener(event -> setLayerVisible(DiagramLayer.UML_OCL, showUmlOcl.isSelected()));
        showOrganization.addActionListener(
                event -> setLayerVisible(DiagramLayer.ORGANIZATION, showOrganization.isSelected()));
        showEnvironment.addActionListener(
                event -> setLayerVisible(DiagramLayer.ENVIRONMENT, showEnvironment.isSelected()));

        JPanel controls = new JPanel();
        controls.setLayout(new BoxLayout(controls, BoxLayout.Y_AXIS));
        JPanel actions = new JPanel();
        actions.add(fit);
        actions.add(reset);
        actions.add(exportSvg);
        actions.add(zoomIn);
        actions.add(zoomOut);
        actions.add(focusAgent);
        actions.add(focusGoalPlan);
        actions.add(new JLabel("View:"));
        actions.add(modeSelector);
        JPanel layers = new JPanel();
        layers.add(new JLabel("Show:"));
        layers.add(showIssues);
        layers.add(showUmlOcl);
        layers.add(showOrganization);
        layers.add(showEnvironment);
        controls.add(actions);
        controls.add(layers);
        controls.add(state);
        controls.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
        add(controls, BorderLayout.NORTH);
        updateControlAvailability();

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
        if (focusNodeId != null && sourceModel.nodes().stream().noneMatch(node -> node.id().equals(focusNodeId))) {
            focusNodeId = null;
        }
        if (selectedSourceNodeId != null
                && sourceModel.nodes().stream().noneMatch(node -> node.id().equals(selectedSourceNodeId))) {
            selectedSourceNodeId = null;
        }
        updateControlAvailability();
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
        if (focusNodeId != null && DiagramModeProjector.project(sourceModel, mode).nodes().stream()
                .noneMatch(node -> node.id().equals(focusNodeId))) {
            focusNodeId = null;
        }
        publishCurrentMode();
    }

    private void publishCurrentMode() {
        DiagramModel modeDiagram = DiagramModeProjector.project(sourceModel, mode);
        DiagramModel visibleDiagram = DiagramNavigationProjector.project(
                modeDiagram, hiddenLayers, Optional.ofNullable(focusNodeId));
        model = visibleDiagram;
        updateControlAvailability();
        canvas.setModel(visibleDiagram);
        applyCurrentHighlight();
        long request = generation.incrementAndGet();
        String presentation = focusNodeId == null ? mode.toString() : "Focus";
        state.setText(visibleDiagram.nodes().isEmpty()
                ? "No diagram data"
                : presentation + ": laying out " + visibleDiagram.nodes().size() + " node(s)...");
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
                    if (fitAfterLayout) {
                        fitAfterLayout = false;
                        canvas.fitToScreen();
                    }
                    state.setText(visibleDiagram.nodes().isEmpty()
                            ? "No diagram data"
                            : presentation + ": " + visibleDiagram.nodes().size() + " node(s), "
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
        selectedSourceNodeId = null;
        focusNodeId = null;
        hiddenLayers.clear();
        highlightedIssueRuleId = null;
        setControlSelections();
        updateControlAvailability();
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
                .filter(node -> node.type() == DiagramNodeType.ISSUE)
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
        selectionListener = Objects.requireNonNull(listener, "listener");
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

    JButton exportSvgForTest() {
        return exportSvg;
    }

    Path exportSvgForTest(Path output, boolean overwrite) throws IOException {
        return exportSvg(output, overwrite);
    }

    JButton focusAgentForTest() {
        return focusAgent;
    }

    JButton focusGoalPlanForTest() {
        return focusGoalPlan;
    }

    JToggleButton showIssuesForTest() {
        return showIssues;
    }

    JToggleButton showUmlOclForTest() {
        return showUmlOcl;
    }

    JToggleButton showOrganizationForTest() {
        return showOrganization;
    }

    JToggleButton showEnvironmentForTest() {
        return showEnvironment;
    }

    private void applyCurrentHighlight() {
        if (highlightedIssueRuleId == null) {
            canvas.setHighlights(Set.of(), Set.of());
            return;
        }
        DiagramHighlightPath.Highlight highlight = DiagramHighlightPath.forIssue(model, highlightedIssueRuleId);
        canvas.setHighlights(highlight.nodeIds(), highlight.edgeIds());
    }

    private void handleSelection(DiagramNode node) {
        selectedSourceNodeId = node.id();
        updateControlAvailability();
        selectionListener.accept(node);
    }

    private void focusSelected(Set<DiagramNodeType> allowedTypes) {
        if (selectedSourceNodeId == null) {
            return;
        }
        Optional<DiagramNode> selected = sourceModel.nodes().stream()
                .filter(node -> node.id().equals(selectedSourceNodeId))
                .filter(node -> allowedTypes.contains(node.type()))
                .findFirst();
        if (selected.isEmpty()) {
            return;
        }
        focusNodeId = selectedSourceNodeId;
        fitAfterLayout = true;
        publishCurrentMode();
    }

    private void setLayerVisible(DiagramLayer layer, boolean visible) {
        if (updatingControls) {
            return;
        }
        if (visible) {
            hiddenLayers.remove(layer);
        } else {
            hiddenLayers.add(layer);
        }
        publishCurrentMode();
    }

    private void resetPresentation() {
        mode = DiagramViewMode.ALL;
        focusNodeId = null;
        hiddenLayers.clear();
        highlightedIssueRuleId = null;
        fitAfterLayout = false;
        setControlSelections();
        publishCurrentMode();
        canvas.resetView();
    }

    private void setControlSelections() {
        updatingControls = true;
        try {
            modeSelector.setSelectedItem(mode);
            showIssues.setSelected(!hiddenLayers.contains(DiagramLayer.ISSUES));
            showUmlOcl.setSelected(!hiddenLayers.contains(DiagramLayer.UML_OCL));
            showOrganization.setSelected(!hiddenLayers.contains(DiagramLayer.ORGANIZATION));
            showEnvironment.setSelected(!hiddenLayers.contains(DiagramLayer.ENVIRONMENT));
        } finally {
            updatingControls = false;
        }
    }

    private void updateControlAvailability() {
        DiagramNode selected = selectedSourceNodeId == null ? null : sourceModel.nodes().stream()
                .filter(node -> node.id().equals(selectedSourceNodeId))
                .findFirst()
                .orElse(null);
        focusAgent.setEnabled(selected != null
                && selected.type() == DiagramNodeType.AGENT);
        focusGoalPlan.setEnabled(selected != null
                && (selected.type() == DiagramNodeType.GOAL || selected.type() == DiagramNodeType.PLAN));
        showIssues.setEnabled(hasLayer(DiagramLayer.ISSUES));
        showUmlOcl.setEnabled(hasLayer(DiagramLayer.UML_OCL));
        showOrganization.setEnabled(hasLayer(DiagramLayer.ORGANIZATION));
        showEnvironment.setEnabled(hasLayer(DiagramLayer.ENVIRONMENT));
        exportSvg.setEnabled(!model.nodes().isEmpty());
    }

    private void chooseExportSvg() {
        JFileChooser chooser = BdiFileChooserSupport.create();
        chooser.setDialogTitle("Export Current Diagram as SVG");
        chooser.setFileFilter(new FileNameExtensionFilter("Scalable Vector Graphics (*.svg)", "svg"));
        chooser.setSelectedFile(new java.io.File("bdi-diagram.svg"));
        if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }
        Path output = chooser.getSelectedFile().toPath();
        if (!output.getFileName().toString().toLowerCase(java.util.Locale.ROOT).endsWith(".svg")) {
            output = output.resolveSibling(output.getFileName() + ".svg");
        }
        boolean overwrite = false;
        if (Files.exists(output)) {
            int choice = JOptionPane.showConfirmDialog(
                    this,
                    "Replace existing diagram?\n" + output.toAbsolutePath().normalize(),
                    "Confirm diagram overwrite",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE);
            if (choice != JOptionPane.YES_OPTION) {
                return;
            }
            overwrite = true;
        }
        try {
            Path exported = exportSvg(output, overwrite);
            state.setText("Exported current diagram: " + exported.getFileName());
        } catch (IOException error) {
            state.setText("Diagram export failed: " + error.getMessage());
            JOptionPane.showMessageDialog(
                    this, error.getMessage(), "Diagram export failed", JOptionPane.ERROR_MESSAGE);
        }
    }

    private Path exportSvg(Path output, boolean overwrite) throws IOException {
        return new DiagramSvgExporter().export(
                model,
                canvas.highlightedNodeIds(),
                canvas.highlightedEdgeIds(),
                canvas.selectedNodeId(),
                output,
                overwrite);
    }

    private boolean hasLayer(DiagramLayer layer) {
        return sourceModel.nodes().stream().anyMatch(layer::contains);
    }

    private static JButton button(String label, String tooltip) {
        JButton button = new JButton(label);
        button.setToolTipText(tooltip);
        return button;
    }

    private static JToggleButton toggle(String label, String tooltip) {
        JToggleButton button = new JToggleButton(label, true);
        button.setToolTipText(tooltip);
        return button;
    }
}

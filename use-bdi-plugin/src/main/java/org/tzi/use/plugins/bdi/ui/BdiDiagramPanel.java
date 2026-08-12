package org.tzi.use.plugins.bdi.ui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;

import javax.swing.BorderFactory;
import javax.swing.JButton;
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
    private final AtomicLong generation = new AtomicLong();
    private DiagramModel model = DiagramModel.empty();
    private SwingWorker<BdiDiagramLayout.Layout, Void> layoutWorker;

    public BdiDiagramPanel() {
        super(new BorderLayout(6, 6));
        canvas = new BdiDiagramCanvas();
        state = new JLabel("No diagram data");
        zoomIn = button("+", "Zoom in");
        zoomOut = button("-", "Zoom out");
        fit = button("Fit", "Fit diagram to the viewport");
        reset = button("Reset", "Reset zoom and pan");
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
        model = diagram;
        canvas.setModel(diagram);
        long request = generation.incrementAndGet();
        state.setText(diagram.nodes().isEmpty()
                ? "No diagram data"
                : "Laying out " + diagram.nodes().size() + " node(s)...");
        if (layoutWorker != null && !layoutWorker.isDone()) {
            layoutWorker.cancel(true);
        }
        layoutWorker = new SwingWorker<>() {
            @Override
            protected BdiDiagramLayout.Layout doInBackground() {
                return BdiDiagramLayout.compute(diagram);
            }

            @Override
            protected void done() {
                if (request != generation.get() || isCancelled()) {
                    return;
                }
                try {
                    canvas.setLayoutSnapshot(get());
                    state.setText(diagram.nodes().isEmpty()
                            ? "No diagram data"
                            : diagram.nodes().size() + " node(s), " + diagram.edges().size() + " edge(s)");
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
        model = DiagramModel.empty();
        canvas.setModel(model);
        state.setText("Diagram unavailable: " + message);
    }

    public void setSelectionListener(Consumer<DiagramNode> listener) {
        canvas.setSelectionListener(Objects.requireNonNull(listener, "listener"));
    }

    DiagramModel modelForTest() {
        return model;
    }

    BdiDiagramCanvas canvasForTest() {
        return canvas;
    }

    JLabel stateForTest() {
        return state;
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

    private static JButton button(String label, String tooltip) {
        JButton button = new JButton(label);
        button.setToolTipText(tooltip);
        return button;
    }
}

package org.tzi.use.plugins.bdi.ui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.geom.Path2D;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;

import javax.swing.JComponent;
import javax.swing.JViewport;
import javax.swing.SwingUtilities;

import org.tzi.use.plugins.bdi.diagram.DiagramEdge;
import org.tzi.use.plugins.bdi.diagram.DiagramModel;
import org.tzi.use.plugins.bdi.diagram.DiagramNode;
import org.tzi.use.plugins.bdi.diagram.DiagramNodeType;

/** Lightweight Java2D canvas; it never edits the supplied semantic model. */
@SuppressWarnings("serial")
final class BdiDiagramCanvas extends JComponent {
    static final double MIN_ZOOM = 0.25;
    static final double MAX_ZOOM = 3.0;

    private DiagramModel model = DiagramModel.empty();
    private BdiDiagramLayout.Layout layout = BdiDiagramLayout.compute(model);
    private Consumer<DiagramNode> selectionListener = ignored -> {
    };
    private double zoom = 1.0;
    private double offsetX = 0.0;
    private double offsetY = 0.0;
    private String selectedNodeId;
    private Point lastPanPoint;
    private boolean panning;
    private boolean panMoved;

    BdiDiagramCanvas() {
        setOpaque(true);
        setBackground(Color.WHITE);
        setToolTipText("");
        addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mousePressed(java.awt.event.MouseEvent event) {
                if (event.getButton() == java.awt.event.MouseEvent.BUTTON2
                        || (event.getButton() == java.awt.event.MouseEvent.BUTTON1
                                && event.isShiftDown())) {
                    panning = true;
                    panMoved = false;
                    lastPanPoint = event.getPoint();
                    setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.MOVE_CURSOR));
                }
            }

            @Override
            public void mouseReleased(java.awt.event.MouseEvent event) {
                panning = false;
                lastPanPoint = null;
                setCursor(java.awt.Cursor.getDefaultCursor());
            }

            @Override
            public void mouseClicked(java.awt.event.MouseEvent event) {
                if (event.getButton() == java.awt.event.MouseEvent.BUTTON1
                        && !event.isShiftDown() && !panMoved) {
                    selectAt(event.getPoint());
                }
                panMoved = false;
            }
        });
        addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
            @Override
            public void mouseDragged(java.awt.event.MouseEvent event) {
                if (!panning || lastPanPoint == null) {
                    return;
                }
                offsetX += event.getX() - lastPanPoint.x;
                offsetY += event.getY() - lastPanPoint.y;
                lastPanPoint = event.getPoint();
                panMoved = true;
                repaint();
            }
        });
        addMouseWheelListener(event -> {
            if (event.isControlDown()) {
                setZoom(zoom * (event.getWheelRotation() < 0 ? 1.1 : 0.9));
                event.consume();
            }
        });
    }

    void setModel(DiagramModel model) {
        requireEdt();
        this.model = Objects.requireNonNull(model, "model");
        this.layout = BdiDiagramLayout.compute(DiagramModel.empty());
        selectedNodeId = null;
        zoom = 1.0;
        offsetX = 0.0;
        offsetY = 0.0;
        updatePreferredSize();
        repaint();
    }

    void setLayoutSnapshot(BdiDiagramLayout.Layout layout) {
        requireEdt();
        this.layout = Objects.requireNonNull(layout, "layout");
        updatePreferredSize();
        repaint();
    }

    void setSelectionListener(Consumer<DiagramNode> listener) {
        selectionListener = Objects.requireNonNull(listener, "listener");
    }

    void zoomIn() {
        setZoom(zoom * 1.25);
    }

    void zoomOut() {
        setZoom(zoom * 0.8);
    }

    void fitToScreen() {
        requireEdt();
        if (model.nodes().isEmpty()) {
            zoom = 1.0;
            offsetX = 0.0;
            offsetY = 0.0;
            updatePreferredSize();
            repaint();
            return;
        }
        JViewport viewport = (JViewport) SwingUtilities.getAncestorOfClass(JViewport.class, this);
        Dimension available = viewport == null ? getSize() : viewport.getExtentSize();
        double availableWidth = Math.max(160.0, available.width - 48.0);
        double availableHeight = Math.max(140.0, available.height - 48.0);
        zoom = clamp(Math.min(availableWidth / layout.width(), availableHeight / layout.height()));
        offsetX = Math.max(24.0, (available.width - layout.width() * zoom) / 2.0);
        offsetY = Math.max(24.0, (available.height - layout.height() * zoom) / 2.0);
        updatePreferredSize();
        repaint();
    }

    void resetView() {
        requireEdt();
        zoom = 1.0;
        offsetX = 0.0;
        offsetY = 0.0;
        updatePreferredSize();
        repaint();
    }

    DiagramModel modelForTest() {
        return model;
    }

    boolean layoutReadyForTest() {
        return layout.boxes().size() == model.nodes().size();
    }

    double zoomForTest() {
        return zoom;
    }

    Optional<DiagramNode> selectedNodeForTest() {
        return model.nodes().stream().filter(node -> node.id().equals(selectedNodeId)).findFirst();
    }

    void selectNodeForTest(String nodeId) {
        requireEdt();
        DiagramNode selected = model.nodes().stream()
                .filter(node -> node.id().equals(nodeId))
                .findFirst()
                .orElseThrow();
        selectedNodeId = selected.id();
        selectionListener.accept(selected);
        repaint();
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(
                Math.max(640, (int) Math.ceil(layout.width() * zoom + 80)),
                Math.max(420, (int) Math.ceil(layout.height() * zoom + 80)));
    }

    @Override
    public String getToolTipText(java.awt.event.MouseEvent event) {
        return nodeAt(event.getPoint()).map(node -> node.type() + ": " + node.label()).orElse(null);
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);
        Graphics2D canvas = (Graphics2D) graphics.create();
        try {
            canvas.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            if (model.nodes().isEmpty()) {
                paintEmptyState(canvas);
                return;
            }
            canvas.translate(offsetX, offsetY);
            canvas.scale(zoom, zoom);
            paintEdges(canvas);
            for (DiagramNode node : model.nodes()) {
                paintNode(canvas, node, layout.boxes().get(node.id()));
            }
        } finally {
            canvas.dispose();
        }
    }

    private void paintEmptyState(Graphics2D canvas) {
        canvas.setColor(new Color(92, 101, 112));
        canvas.setFont(getFont().deriveFont(Font.BOLD, 15f));
        String title = "No diagram data";
        FontMetrics metrics = canvas.getFontMetrics();
        canvas.drawString(title, Math.max(20, (getWidth() - metrics.stringWidth(title)) / 2), getHeight() / 2 - 8);
        canvas.setFont(getFont().deriveFont(Font.PLAIN, 12f));
        String hint = "Import an AgentSpeak or JaCaMo project to build a read-only view.";
        metrics = canvas.getFontMetrics();
        canvas.drawString(hint, Math.max(20, (getWidth() - metrics.stringWidth(hint)) / 2), getHeight() / 2 + 18);
    }

    private void paintEdges(Graphics2D canvas) {
        canvas.setStroke(new BasicStroke(1.3f));
        for (DiagramEdge edge : model.edges()) {
            BdiDiagramLayout.NodeBox source = layout.boxes().get(edge.sourceNodeId());
            BdiDiagramLayout.NodeBox target = layout.boxes().get(edge.targetNodeId());
            if (source == null || target == null) {
                continue;
            }
            double x1 = source.centerX();
            double y1 = source.centerY();
            double x2 = target.centerX();
            double y2 = target.centerY();
            canvas.setColor(edge.type() == org.tzi.use.plugins.bdi.diagram.DiagramEdgeType.MISSING_MAPPING
                    ? new Color(202, 116, 38) : new Color(126, 139, 153));
            canvas.drawLine((int) x1, (int) y1, (int) x2, (int) y2);
            drawArrow(canvas, x1, y1, x2, y2);
            edge.label().ifPresent(label -> {
                canvas.setColor(new Color(74, 82, 92));
                canvas.setFont(getFont().deriveFont(Font.PLAIN, 10f));
                canvas.drawString(label, (int) ((x1 + x2) / 2), (int) ((y1 + y2) / 2) - 3);
            });
        }
    }

    private void paintNode(Graphics2D canvas, DiagramNode node, BdiDiagramLayout.NodeBox box) {
        if (box == null) {
            return;
        }
        Color fill = fillColor(node.type());
        canvas.setColor(fill);
        canvas.fillRoundRect((int) box.x(), (int) box.y(), (int) box.width(), (int) box.height(), 10, 10);
        Color border = node.id().equals(selectedNodeId) ? new Color(25, 87, 160)
                : node.type() == DiagramNodeType.ISSUE ? new Color(176, 45, 45)
                : node.type() == DiagramNodeType.GAP ? new Color(202, 116, 38)
                : new Color(96, 105, 115);
        canvas.setColor(border);
        canvas.setStroke(new BasicStroke(node.id().equals(selectedNodeId) ? 2.8f : 1.2f));
        canvas.drawRoundRect((int) box.x(), (int) box.y(), (int) box.width(), (int) box.height(), 10, 10);
        canvas.setColor(new Color(37, 43, 51));
        canvas.setFont(getFont().deriveFont(Font.PLAIN, 11f));
        drawLabel(canvas, node.label(), box);
    }

    private static void drawLabel(Graphics2D canvas, String label, BdiDiagramLayout.NodeBox box) {
        FontMetrics metrics = canvas.getFontMetrics();
        String text = label.length() > 28 ? label.substring(0, 25) + "..." : label;
        int x = (int) (box.x() + (box.width() - metrics.stringWidth(text)) / 2);
        int y = (int) (box.y() + box.height() / 2 + metrics.getAscent() / 2 - 2);
        canvas.drawString(text, Math.max((int) box.x() + 5, x), y);
    }

    private static void drawArrow(Graphics2D canvas, double x1, double y1, double x2, double y2) {
        double angle = Math.atan2(y2 - y1, x2 - x1);
        double size = 7.0;
        Path2D arrow = new Path2D.Double();
        arrow.moveTo(x2, y2);
        arrow.lineTo(x2 - size * Math.cos(angle - Math.PI / 6), y2 - size * Math.sin(angle - Math.PI / 6));
        arrow.lineTo(x2 - size * Math.cos(angle + Math.PI / 6), y2 - size * Math.sin(angle + Math.PI / 6));
        arrow.closePath();
        canvas.fill(arrow);
    }

    private static Color fillColor(DiagramNodeType type) {
        return switch (type) {
            case ISSUE -> new Color(255, 225, 225);
            case GAP -> new Color(255, 239, 208);
            case UML_CLASS, UML_OBJECT, UML_ATTRIBUTE, UML_OPERATION, OCL_CONSTRAINT, TRACE_TARGET ->
                    new Color(222, 235, 248);
            case AGENT, MAS_PROJECT, ORGANIZATION, ROLE, MISSION -> new Color(224, 241, 229);
            default -> new Color(239, 242, 245);
        };
    }

    private void selectAt(Point point) {
        Optional<DiagramNode> selected = nodeAt(point);
        selectedNodeId = selected.map(DiagramNode::id).orElse(null);
        selected.ifPresent(selectionListener);
        repaint();
    }

    private Optional<DiagramNode> nodeAt(Point point) {
        double modelX = (point.x - offsetX) / zoom;
        double modelY = (point.y - offsetY) / zoom;
        return model.nodes().stream()
                .filter(node -> {
                    BdiDiagramLayout.NodeBox box = layout.boxes().get(node.id());
                    return box != null && box.contains(modelX, modelY);
                })
                .findFirst();
    }

    private void setZoom(double requested) {
        requireEdt();
        zoom = clamp(requested);
        updatePreferredSize();
        repaint();
    }

    private static double clamp(double value) {
        return Math.max(MIN_ZOOM, Math.min(MAX_ZOOM, value));
    }

    private void updatePreferredSize() {
        setPreferredSize(getPreferredSize());
        revalidate();
    }

    private static void requireEdt() {
        if (!SwingUtilities.isEventDispatchThread()) {
            throw new IllegalStateException("Diagram canvas must be updated on the Swing EDT");
        }
    }
}

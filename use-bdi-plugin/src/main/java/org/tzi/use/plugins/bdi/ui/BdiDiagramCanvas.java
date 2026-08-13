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
import java.util.Set;
import java.util.function.Consumer;

import javax.swing.JComponent;
import javax.swing.JViewport;
import javax.swing.SwingUtilities;

import org.tzi.use.plugins.bdi.diagram.DiagramEdge;
import org.tzi.use.plugins.bdi.diagram.DiagramModel;
import org.tzi.use.plugins.bdi.diagram.DiagramNode;

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
    private Set<String> highlightedNodeIds = Set.of();
    private Set<String> highlightedEdgeIds = Set.of();
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
        String previousSelection = selectedNodeId;
        this.model = Objects.requireNonNull(model, "model");
        this.layout = BdiDiagramLayout.compute(DiagramModel.empty());
        selectedNodeId = this.model.nodes().stream()
                .anyMatch(node -> node.id().equals(previousSelection)) ? previousSelection : null;
        Set<String> nodeIds = this.model.nodes().stream().map(DiagramNode::id).collect(java.util.stream.Collectors.toSet());
        Set<String> edgeIds = this.model.edges().stream().map(DiagramEdge::id).collect(java.util.stream.Collectors.toSet());
        highlightedNodeIds = highlightedNodeIds.stream().filter(nodeIds::contains).collect(java.util.stream.Collectors.toUnmodifiableSet());
        highlightedEdgeIds = highlightedEdgeIds.stream().filter(edgeIds::contains).collect(java.util.stream.Collectors.toUnmodifiableSet());
        zoom = 1.0;
        offsetX = 0.0;
        offsetY = 0.0;
        updatePreferredSize();
        resetViewportOrigin();
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

    void setHighlights(Set<String> nodeIds, Set<String> edgeIds) {
        requireEdt();
        highlightedNodeIds = Set.copyOf(Objects.requireNonNull(nodeIds, "nodeIds"));
        highlightedEdgeIds = Set.copyOf(Objects.requireNonNull(edgeIds, "edgeIds"));
        repaint();
    }

    void selectNode(String nodeId) {
        requireEdt();
        DiagramNode selected = model.nodes().stream()
                .filter(node -> node.id().equals(nodeId))
                .findFirst()
                .orElse(null);
        selectedNodeId = selected == null ? null : selected.id();
        if (selected != null) {
            selectionListener.accept(selected);
        }
        repaint();
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
        resetViewportOrigin();
        repaint();
    }

    void resetView() {
        requireEdt();
        zoom = 1.0;
        offsetX = 0.0;
        offsetY = 0.0;
        updatePreferredSize();
        resetViewportOrigin();
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
        if (model.nodes().stream().noneMatch(node -> node.id().equals(nodeId))) {
            throw new IllegalArgumentException("Unknown diagram node: " + nodeId);
        }
        selectNode(nodeId);
    }

    Set<String> highlightedNodeIdsForTest() {
        return highlightedNodeIds();
    }

    Set<String> highlightedEdgeIdsForTest() {
        return highlightedEdgeIds();
    }

    Set<String> highlightedNodeIds() {
        return highlightedNodeIds;
    }

    Set<String> highlightedEdgeIds() {
        return highlightedEdgeIds;
    }

    Optional<String> selectedNodeId() {
        return Optional.ofNullable(selectedNodeId);
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(
                Math.max(640, (int) Math.ceil(layout.width() * zoom + 80)),
                Math.max(420, (int) Math.ceil(layout.height() * zoom + 80)));
    }

    @Override
    public String getToolTipText(java.awt.event.MouseEvent event) {
        return nodeAt(event.getPoint()).map(node -> {
            DiagramVisualState state = DiagramVisualStateResolver.resolve(node);
            String layer = node.attributes().get("layer");
            return node.type() + ": " + node.label() + " [" + state.badge() + "]"
                    + (layer == null ? "" : " layer=" + layer);
        }).orElse(null);
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);
        Graphics2D canvas = (Graphics2D) graphics.create();
        try {
            // A bare JComponent has no UI delegate to clear its background.
            canvas.setColor(getBackground());
            canvas.fillRect(0, 0, getWidth(), getHeight());
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
            double[] start = boundaryPoint(source, target.centerX(), target.centerY());
            double[] end = boundaryPoint(target, source.centerX(), source.centerY());
            double x1 = start[0];
            double y1 = start[1];
            double x2 = end[0];
            double y2 = end[1];
            canvas.setColor(highlightedEdgeIds.contains(edge.id()) ? new Color(24, 91, 147)
                    : edge.type() == org.tzi.use.plugins.bdi.diagram.DiagramEdgeType.MISSING_MAPPING
                    ? new Color(202, 116, 38) : new Color(126, 139, 153));
            canvas.setStroke(new BasicStroke(highlightedEdgeIds.contains(edge.id()) ? 3.2f : 1.3f));
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
        DiagramVisualState state = DiagramVisualStateResolver.resolve(node);
        boolean selected = node.id().equals(selectedNodeId);
        boolean highlighted = highlightedNodeIds.contains(node.id());
        Color fill = state == DiagramVisualState.CLEAN ? DiagramPalette.fill(node) : state.fill();
        canvas.setColor(fill);
        canvas.fillRoundRect((int) box.x(), (int) box.y(), (int) box.width(), (int) box.height(), 10, 10);
        Color border = selected ? new Color(25, 87, 160)
                : highlighted ? new Color(24, 91, 147)
                : state == DiagramVisualState.CLEAN ? DiagramPalette.cleanBorder(node.type()) : state.border();
        canvas.setColor(border);
        canvas.setStroke(selected || highlighted
                ? new BasicStroke(selected ? 2.8f : 2.4f)
                : state.dashed()
                        ? new BasicStroke(1.6f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND,
                                10.0f, new float[] { 6.0f, 4.0f }, 0.0f)
                        : new BasicStroke(1.2f));
        canvas.drawRoundRect((int) box.x(), (int) box.y(), (int) box.width(), (int) box.height(), 10, 10);
        String badge = state == DiagramVisualState.CLEAN && selected ? "SELECTED" : state.badge();
        if (state != DiagramVisualState.CLEAN || selected) {
            canvas.setColor(border);
            canvas.setFont(getFont().deriveFont(Font.BOLD, 8.5f));
            canvas.drawString(badge, (int) box.x() + 6, (int) box.y() + 13);
        }
        canvas.setColor(new Color(37, 43, 51));
        canvas.setFont(getFont().deriveFont(Font.PLAIN, 11f));
        drawLabel(canvas, node.label(), box, state != DiagramVisualState.CLEAN || selected);
    }

    private static void drawLabel(
            Graphics2D canvas, String label, BdiDiagramLayout.NodeBox box, boolean hasBadge) {
        FontMetrics metrics = canvas.getFontMetrics();
        String text = label.length() > 28 ? label.substring(0, 25) + "..." : label;
        int x = (int) (box.x() + (box.width() - metrics.stringWidth(text)) / 2);
        int y = (int) (box.y() + (hasBadge ? box.height() * 0.68 : box.height() / 2)
                + metrics.getAscent() / 2 - 2);
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

    private static double[] boundaryPoint(
            BdiDiagramLayout.NodeBox box, double targetX, double targetY) {
        double dx = targetX - box.centerX();
        double dy = targetY - box.centerY();
        if (dx == 0.0 && dy == 0.0) {
            return new double[] { box.centerX(), box.centerY() };
        }
        double horizontalScale = dx == 0.0 ? Double.POSITIVE_INFINITY : box.width() / (2.0 * Math.abs(dx));
        double verticalScale = dy == 0.0 ? Double.POSITIVE_INFINITY : box.height() / (2.0 * Math.abs(dy));
        double scale = Math.min(horizontalScale, verticalScale);
        return new double[] { box.centerX() + dx * scale, box.centerY() + dy * scale };
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

    private void resetViewportOrigin() {
        JViewport viewport = (JViewport) SwingUtilities.getAncestorOfClass(JViewport.class, this);
        if (viewport != null) {
            viewport.setViewPosition(new Point(0, 0));
        }
    }

    private static void requireEdt() {
        if (!SwingUtilities.isEventDispatchThread()) {
            throw new IllegalStateException("Diagram canvas must be updated on the Swing EDT");
        }
    }
}

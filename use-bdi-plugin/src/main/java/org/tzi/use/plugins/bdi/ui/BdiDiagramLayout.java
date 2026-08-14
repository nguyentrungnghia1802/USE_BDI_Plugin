package org.tzi.use.plugins.bdi.ui;

import java.awt.Dimension;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.tzi.use.plugins.bdi.diagram.DiagramModel;
import org.tzi.use.plugins.bdi.diagram.DiagramNode;
import org.tzi.use.plugins.bdi.diagram.DiagramNodeType;

/** Deterministic, renderer-owned layout snapshot for the Swing canvas. */
final class BdiDiagramLayout {
    private static final int NODE_WIDTH = 190;
    private static final int NODE_HEIGHT = 58;
    private static final int COLUMN_GAP = 40;
    private static final int ROW_GAP = 28;
    private static final int MARGIN = 36;

    private BdiDiagramLayout() {
    }

    static Layout compute(DiagramModel model) {
        Objects.requireNonNull(model, "model");
        if (model.nodes().isEmpty()) {
            return new Layout(Map.of(), 640, 420);
        }

        List<DiagramNodeType> columns = model.nodes().stream()
                .map(DiagramNode::type)
                .distinct()
                .sorted(Comparator.comparingInt(DiagramNodeType::ordinal))
                .toList();
        Map<String, NodeBox> boxes = new LinkedHashMap<>();
        int width = 0;
        int height = 0;
        for (int column = 0; column < columns.size(); column++) {
            DiagramNodeType type = columns.get(column);
            List<DiagramNode> nodes = model.nodes().stream()
                    .filter(node -> node.type() == type)
                    .sorted(Comparator.comparing(DiagramNode::id))
                    .toList();
            double x = MARGIN + column * (NODE_WIDTH + COLUMN_GAP);
            for (int row = 0; row < nodes.size(); row++) {
                double y = MARGIN + row * (NODE_HEIGHT + ROW_GAP);
                boxes.put(nodes.get(row).id(), new NodeBox(x, y, NODE_WIDTH, NODE_HEIGHT));
                width = Math.max(width, (int) Math.ceil(x + NODE_WIDTH + MARGIN));
                height = Math.max(height, (int) Math.ceil(y + NODE_HEIGHT + MARGIN));
            }
        }
        return new Layout(boxes, width, height);
    }

    record Layout(Map<String, NodeBox> boxes, int width, int height) {
        Layout {
            boxes = Map.copyOf(Objects.requireNonNull(boxes, "boxes"));
            if (width <= 0 || height <= 0) {
                throw new IllegalArgumentException("Layout dimensions must be positive");
            }
        }

        Dimension preferredSize() {
            return new Dimension(width, height);
        }
    }

    record NodeBox(double x, double y, double width, double height) {
        double centerX() {
            return x + width / 2.0;
        }

        double centerY() {
            return y + height / 2.0;
        }

        boolean contains(double pointX, double pointY) {
            return pointX >= x && pointX <= x + width
                    && pointY >= y && pointY <= y + height;
        }
    }
}

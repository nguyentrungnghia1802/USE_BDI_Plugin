package org.tzi.use.plugins.bdi.ui;

import java.awt.Color;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;

import org.tzi.use.plugins.bdi.diagram.DiagramEdge;
import org.tzi.use.plugins.bdi.diagram.DiagramEdgeType;
import org.tzi.use.plugins.bdi.diagram.DiagramModel;
import org.tzi.use.plugins.bdi.diagram.DiagramNode;

/** Writes the current derived presentation as dependency-free deterministic SVG. */
final class DiagramSvgExporter {
    private static final Pattern ABSOLUTE_SOURCE = Pattern.compile(
            "(?i)(?:[a-z]:[\\\\/]|\\\\\\\\|\\b(?:file|https?)://|^/)");

    Path export(
            DiagramModel model,
            Set<String> highlightedNodeIds,
            Set<String> highlightedEdgeIds,
            Optional<String> selectedNodeId,
            Path output,
            boolean overwrite) throws IOException {
        Objects.requireNonNull(model, "model");
        highlightedNodeIds = Set.copyOf(Objects.requireNonNull(highlightedNodeIds, "highlightedNodeIds"));
        highlightedEdgeIds = Set.copyOf(Objects.requireNonNull(highlightedEdgeIds, "highlightedEdgeIds"));
        selectedNodeId = Objects.requireNonNull(selectedNodeId, "selectedNodeId");
        Path target = Objects.requireNonNull(output, "output").toAbsolutePath().normalize();
        if (model.nodes().isEmpty()) {
            throw new IOException("Cannot export an empty diagram");
        }
        if (Files.exists(target) && !overwrite) {
            throw new IOException("Diagram already exists and overwrite was not confirmed: " + target);
        }
        Path parent = target.getParent();
        if (parent == null) {
            throw new IOException("Diagram output has no parent directory: " + target);
        }
        Files.createDirectories(parent);
        String svg = render(model, highlightedNodeIds, highlightedEdgeIds, selectedNodeId);
        Path temporary = Files.createTempFile(parent, ".use-bdi-diagram-", ".svg");
        try {
            Files.writeString(temporary, svg, StandardCharsets.UTF_8);
            move(temporary, target, overwrite);
            return target;
        } finally {
            Files.deleteIfExists(temporary);
        }
    }

    String render(
            DiagramModel model,
            Set<String> highlightedNodeIds,
            Set<String> highlightedEdgeIds,
            Optional<String> selectedNodeId) {
        BdiDiagramLayout.Layout layout = BdiDiagramLayout.compute(model);
        StringBuilder svg = new StringBuilder(4096);
        svg.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n")
                .append("<svg xmlns=\"http://www.w3.org/2000/svg\" width=\"").append(layout.width())
                .append("\" height=\"").append(layout.height()).append("\" viewBox=\"0 0 ")
                .append(layout.width()).append(' ').append(layout.height()).append("\">\n")
                .append("  <title>USE BDI derived diagram</title>\n")
                .append("  <desc>Read-only current presentation: ").append(model.nodes().size())
                .append(" nodes, ").append(model.edges().size()).append(" edges</desc>\n")
                .append("  <defs><marker id=\"arrow\" markerWidth=\"8\" markerHeight=\"8\" refX=\"7\" refY=\"4\" orient=\"auto\"><path d=\"M0,0 L8,4 L0,8 z\" fill=\"#7e8b99\"/></marker></defs>\n")
                .append("  <g id=\"edges\">\n");
        for (DiagramEdge edge : model.edges()) {
            BdiDiagramLayout.NodeBox source = layout.boxes().get(edge.sourceNodeId());
            BdiDiagramLayout.NodeBox target = layout.boxes().get(edge.targetNodeId());
            if (source == null || target == null) {
                continue;
            }
            boolean highlighted = highlightedEdgeIds.contains(edge.id());
            String stroke = highlighted ? "#185b93"
                    : edge.type() == DiagramEdgeType.MISSING_MAPPING ? "#ca7426" : "#7e8b99";
            svg.append("    <line x1=\"").append(number(source.centerX())).append("\" y1=\"")
                    .append(number(source.centerY())).append("\" x2=\"").append(number(target.centerX()))
                    .append("\" y2=\"").append(number(target.centerY())).append("\" stroke=\"")
                    .append(stroke).append("\" stroke-width=\"").append(highlighted ? "3.2" : "1.3")
                    .append("\" marker-end=\"url(#arrow)\"/>").append('\n');
            edge.label().ifPresent(label -> svg.append("    <text x=\"")
                    .append(number((source.centerX() + target.centerX()) / 2.0)).append("\" y=\"")
                    .append(number((source.centerY() + target.centerY()) / 2.0 - 3.0))
                    .append("\" font-family=\"sans-serif\" font-size=\"10\" fill=\"#4a525c\">")
                    .append(xml(portable(label))).append("</text>\n"));
        }
        svg.append("  </g>\n  <g id=\"nodes\">\n");
        for (DiagramNode node : model.nodes()) {
            BdiDiagramLayout.NodeBox box = layout.boxes().get(node.id());
            if (box == null) {
                continue;
            }
            DiagramVisualState state = DiagramVisualStateResolver.resolve(node);
            boolean selected = selectedNodeId.filter(node.id()::equals).isPresent();
            boolean highlighted = highlightedNodeIds.contains(node.id());
            Color fill = state == DiagramVisualState.CLEAN ? DiagramPalette.fill(node) : state.fill();
            Color border = selected ? new Color(25, 87, 160)
                    : highlighted ? new Color(24, 91, 147)
                    : state == DiagramVisualState.CLEAN ? DiagramPalette.cleanBorder(node.type()) : state.border();
            String dash = state.dashed() && !selected && !highlighted ? " stroke-dasharray=\"6 4\"" : "";
            svg.append("    <g data-node-id=\"").append(xml(node.id())).append("\">\n")
                    .append("      <rect x=\"").append(number(box.x())).append("\" y=\"")
                    .append(number(box.y())).append("\" width=\"").append(number(box.width()))
                    .append("\" height=\"").append(number(box.height()))
                    .append("\" rx=\"10\" fill=\"").append(hex(fill)).append("\" stroke=\"")
                    .append(hex(border)).append("\" stroke-width=\"")
                    .append(selected ? "2.8" : highlighted ? "2.4" : "1.2").append('"')
                    .append(dash).append("/>\n");
            String badge = selected ? "SELECTED" : state == DiagramVisualState.CLEAN ? "" : state.badge();
            if (!badge.isEmpty()) {
                svg.append("      <text x=\"").append(number(box.x() + 6)).append("\" y=\"")
                        .append(number(box.y() + 13)).append("\" font-family=\"sans-serif\" font-size=\"8.5\" font-weight=\"bold\" fill=\"")
                        .append(hex(border)).append("\">").append(xml(badge)).append("</text>\n");
            }
            svg.append("      <text x=\"").append(number(box.centerX())).append("\" y=\"")
                    .append(number(box.y() + (badge.isEmpty() ? 31 : 38)))
                    .append("\" text-anchor=\"middle\" font-family=\"sans-serif\" font-size=\"11\" fill=\"#252b33\">")
                    .append(xml(portable(node.label()))).append("</text>\n    </g>\n");
        }
        return svg.append("  </g>\n</svg>\n").toString();
    }

    private static void move(Path source, Path target, boolean overwrite) throws IOException {
        StandardCopyOption[] options = overwrite
                ? new StandardCopyOption[] { StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING }
                : new StandardCopyOption[] { StandardCopyOption.ATOMIC_MOVE };
        try {
            Files.move(source, target, options);
        } catch (AtomicMoveNotSupportedException error) {
            if (overwrite) {
                Files.move(source, target, StandardCopyOption.REPLACE_EXISTING);
            } else {
                Files.move(source, target);
            }
        }
    }

    private static String portable(String label) {
        return ABSOLUTE_SOURCE.matcher(label).find() ? "[non-portable source omitted]" : label;
    }

    private static String xml(String value) {
        return value.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
                .replace("\"", "&quot;").replace("'", "&apos;");
    }

    private static String hex(Color color) {
        return String.format("#%02x%02x%02x", color.getRed(), color.getGreen(), color.getBlue());
    }

    private static String number(double value) {
        return value == Math.rint(value) ? Long.toString(Math.round(value))
                : String.format(java.util.Locale.ROOT, "%.2f", value);
    }
}

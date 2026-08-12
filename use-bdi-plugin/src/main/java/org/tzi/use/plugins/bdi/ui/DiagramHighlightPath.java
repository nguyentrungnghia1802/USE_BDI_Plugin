package org.tzi.use.plugins.bdi.ui;

import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

import org.tzi.use.plugins.bdi.diagram.DiagramEdge;
import org.tzi.use.plugins.bdi.diagram.DiagramEdgeType;
import org.tzi.use.plugins.bdi.diagram.DiagramModel;
import org.tzi.use.plugins.bdi.diagram.DiagramNode;
import org.tzi.use.plugins.bdi.diagram.DiagramNodeType;

/** Finds the evidence path for an issue without re-running analysis. */
final class DiagramHighlightPath {
    private DiagramHighlightPath() {
    }

    static Highlight forIssue(DiagramModel model, String ruleId) {
        Objects.requireNonNull(model, "model");
        if (ruleId == null || ruleId.isBlank()) {
            return Highlight.empty();
        }

        Set<String> nodeIds = new LinkedHashSet<>();
        for (DiagramNode node : model.nodes()) {
            if (node.type() == DiagramNodeType.ISSUE
                    && node.issueMarker().map(marker -> marker.ruleId().equals(ruleId)).orElse(false)) {
                nodeIds.add(node.id());
            }
        }
        if (nodeIds.isEmpty()) {
            return Highlight.empty();
        }

        Set<String> edgeIds = new LinkedHashSet<>();
        boolean changed;
        do {
            changed = false;
            for (DiagramEdge edge : model.edges()) {
                if (!isEvidenceEdge(edge.type())
                        || (!nodeIds.contains(edge.sourceNodeId()) && !nodeIds.contains(edge.targetNodeId()))) {
                    continue;
                }
                if (edgeIds.add(edge.id())) {
                    changed = true;
                }
                if (nodeIds.add(edge.sourceNodeId())) {
                    changed = true;
                }
                if (nodeIds.add(edge.targetNodeId())) {
                    changed = true;
                }
            }
        } while (changed);
        return new Highlight(nodeIds, edgeIds);
    }

    private static boolean isEvidenceEdge(DiagramEdgeType type) {
        return switch (type) {
            case HAS_ISSUE, MAPS_TO, MISSING_MAPPING, CONSTRAINED_BY, OWNS -> true;
            default -> false;
        };
    }

    record Highlight(Set<String> nodeIds, Set<String> edgeIds) {
        Highlight {
            nodeIds = Set.copyOf(Objects.requireNonNull(nodeIds, "nodeIds"));
            edgeIds = Set.copyOf(Objects.requireNonNull(edgeIds, "edgeIds"));
        }

        static Highlight empty() {
            return new Highlight(Set.of(), Set.of());
        }

        boolean isEmpty() {
            return nodeIds.isEmpty();
        }
    }
}

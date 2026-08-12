package org.tzi.use.plugins.bdi.ui;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.tzi.use.plugins.bdi.diagram.DiagramEdge;
import org.tzi.use.plugins.bdi.diagram.DiagramEdgeType;
import org.tzi.use.plugins.bdi.diagram.DiagramGroup;
import org.tzi.use.plugins.bdi.diagram.DiagramModel;
import org.tzi.use.plugins.bdi.diagram.DiagramNode;
import org.tzi.use.plugins.bdi.diagram.DiagramNodeType;

/** Applies layer and bounded-focus controls to an immutable diagram projection. */
final class DiagramNavigationProjector {
    private static final int MAX_ISSUE_PATH_DEPTH = 8;
    private static final Set<DiagramEdgeType> ISSUE_PATH_EDGES = EnumSet.of(
            DiagramEdgeType.OWNS,
            DiagramEdgeType.MAPS_TO,
            DiagramEdgeType.MISSING_MAPPING,
            DiagramEdgeType.CONSTRAINED_BY,
            DiagramEdgeType.HAS_ISSUE);

    private DiagramNavigationProjector() {
    }

    static DiagramModel project(DiagramModel source, Set<DiagramLayer> hiddenLayers, Optional<String> focusNodeId) {
        Objects.requireNonNull(source, "source");
        Set<DiagramLayer> hidden = Set.copyOf(Objects.requireNonNull(hiddenLayers, "hiddenLayers"));
        Optional<String> focus = Objects.requireNonNull(focusNodeId, "focusNodeId");

        Set<String> visibleNodeIds = source.nodes().stream()
                .filter(node -> hidden.stream().noneMatch(layer -> layer.contains(node)))
                .map(DiagramNode::id)
                .collect(Collectors.toCollection(LinkedHashSet::new));
        DiagramModel layered = rebuild(source, visibleNodeIds);
        if (focus.isEmpty() || !visibleNodeIds.contains(focus.orElseThrow())) {
            return layered;
        }
        return focus(layered, focus.orElseThrow());
    }

    private static DiagramModel focus(DiagramModel source, String selectedNodeId) {
        Set<String> nodeIds = new LinkedHashSet<>();
        nodeIds.add(selectedNodeId);
        for (DiagramEdge edge : source.edges()) {
            if (edge.sourceNodeId().equals(selectedNodeId) || edge.targetNodeId().equals(selectedNodeId)) {
                nodeIds.add(edge.sourceNodeId());
                nodeIds.add(edge.targetNodeId());
            }
        }
        addIssuePaths(source, nodeIds);
        return rebuild(source, nodeIds);
    }

    private static void addIssuePaths(DiagramModel source, Set<String> neighborhood) {
        Map<String, List<DiagramEdge>> adjacency = new HashMap<>();
        for (DiagramEdge edge : source.edges()) {
            if (!ISSUE_PATH_EDGES.contains(edge.type())) {
                continue;
            }
            adjacency.computeIfAbsent(edge.sourceNodeId(), ignored -> new ArrayList<>()).add(edge);
            adjacency.computeIfAbsent(edge.targetNodeId(), ignored -> new ArrayList<>()).add(edge);
        }

        Set<String> seeds = Set.copyOf(neighborhood);
        ArrayDeque<PathStep> queue = new ArrayDeque<>();
        Map<String, PathStep> visited = new HashMap<>();
        for (String seed : seeds) {
            PathStep step = new PathStep(seed, null, 0);
            queue.add(step);
            visited.put(seed, step);
        }
        while (!queue.isEmpty()) {
            PathStep current = queue.removeFirst();
            if (isIssue(source, current.nodeId())) {
                addPath(current, neighborhood);
                continue;
            }
            if (current.depth() >= MAX_ISSUE_PATH_DEPTH) {
                continue;
            }
            for (DiagramEdge edge : adjacency.getOrDefault(current.nodeId(), List.of())) {
                String next = edge.sourceNodeId().equals(current.nodeId())
                        ? edge.targetNodeId() : edge.sourceNodeId();
                if (!visited.containsKey(next)) {
                    PathStep step = new PathStep(next, current, current.depth() + 1);
                    visited.put(next, step);
                    queue.addLast(step);
                }
            }
        }
    }

    private static boolean isIssue(DiagramModel source, String nodeId) {
        return source.nodes().stream()
                .anyMatch(node -> node.id().equals(nodeId) && node.type() == DiagramNodeType.ISSUE);
    }

    private static void addPath(PathStep step, Set<String> nodeIds) {
        PathStep current = step;
        while (current != null) {
            nodeIds.add(current.nodeId());
            current = current.parent();
        }
    }

    private static DiagramModel rebuild(DiagramModel source, Set<String> nodeIds) {
        List<DiagramNode> nodes = source.nodes().stream()
                .filter(node -> nodeIds.contains(node.id()))
                .toList();
        List<DiagramEdge> edges = source.edges().stream()
                .filter(edge -> nodeIds.contains(edge.sourceNodeId()) && nodeIds.contains(edge.targetNodeId()))
                .toList();
        List<DiagramGroup> groups = source.groups().stream()
                .map(group -> {
                    List<String> members = group.nodeIds().stream().filter(nodeIds::contains).toList();
                    return members.isEmpty() ? null
                            : new DiagramGroup(group.selection(), group.label(), members, group.attributes());
                })
                .filter(Objects::nonNull)
                .toList();
        return new DiagramModel(nodes, edges, groups);
    }

    private record PathStep(String nodeId, PathStep parent, int depth) {
    }
}

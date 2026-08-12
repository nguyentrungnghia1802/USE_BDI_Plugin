package org.tzi.use.plugins.bdi.diagram;

import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/** Immutable renderer-neutral diagram with validated references and stable ordering. */
public record DiagramModel(
        List<DiagramNode> nodes,
        List<DiagramEdge> edges,
        List<DiagramGroup> groups) {
    public DiagramModel {
        nodes = List.copyOf(Objects.requireNonNull(nodes, "nodes"));
        edges = List.copyOf(Objects.requireNonNull(edges, "edges"));
        groups = List.copyOf(Objects.requireNonNull(groups, "groups"));

        Set<String> nodeIds = collectUniqueNodeIds(nodes);
        rejectDuplicateEdgeIds(edges);
        rejectDuplicateGroupIds(groups);
        validateReferences(nodeIds, edges, groups);

        nodes = nodes.stream().sorted(Comparator.comparing(DiagramNode::id)).toList();
        edges = edges.stream().sorted(Comparator.comparing(DiagramEdge::id)).toList();
        groups = groups.stream().sorted(Comparator.comparing(DiagramGroup::id)).toList();
    }

    public static DiagramModel empty() {
        return new DiagramModel(List.of(), List.of(), List.of());
    }

    private static Set<String> collectUniqueNodeIds(List<DiagramNode> nodes) {
        Set<String> nodeIds = new HashSet<>();
        for (DiagramNode node : nodes) {
            Objects.requireNonNull(node, "node");
            if (!nodeIds.add(node.id())) {
                throw new IllegalArgumentException("Diagram contains duplicate node ID: " + node.id());
            }
        }
        return nodeIds;
    }

    private static void rejectDuplicateEdgeIds(List<DiagramEdge> edges) {
        Set<String> edgeIds = new HashSet<>();
        for (DiagramEdge edge : edges) {
            Objects.requireNonNull(edge, "edge");
            if (!edgeIds.add(edge.id())) {
                throw new IllegalArgumentException("Diagram contains duplicate edge ID: " + edge.id());
            }
        }
    }

    private static void rejectDuplicateGroupIds(List<DiagramGroup> groups) {
        Set<String> groupIds = new HashSet<>();
        for (DiagramGroup group : groups) {
            Objects.requireNonNull(group, "group");
            if (!groupIds.add(group.id())) {
                throw new IllegalArgumentException("Diagram contains duplicate group ID: " + group.id());
            }
        }
    }

    private static void validateReferences(
            Set<String> nodeIds,
            List<DiagramEdge> edges,
            List<DiagramGroup> groups) {
        for (DiagramEdge edge : edges) {
            if (!nodeIds.contains(edge.sourceNodeId()) || !nodeIds.contains(edge.targetNodeId())) {
                throw new IllegalArgumentException("Diagram edge references an unknown node: " + edge.id());
            }
        }
        for (DiagramGroup group : groups) {
            for (String nodeId : group.nodeIds()) {
                if (!nodeIds.contains(nodeId)) {
                    throw new IllegalArgumentException(
                            "Diagram group references an unknown node: " + group.id() + " -> " + nodeId);
                }
            }
        }
    }
}

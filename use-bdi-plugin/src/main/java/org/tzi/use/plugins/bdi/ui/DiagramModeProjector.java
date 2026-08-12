package org.tzi.use.plugins.bdi.ui;

import java.util.EnumSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.tzi.use.plugins.bdi.diagram.DiagramEdge;
import org.tzi.use.plugins.bdi.diagram.DiagramEdgeType;
import org.tzi.use.plugins.bdi.diagram.DiagramGroup;
import org.tzi.use.plugins.bdi.diagram.DiagramModel;
import org.tzi.use.plugins.bdi.diagram.DiagramNode;
import org.tzi.use.plugins.bdi.diagram.DiagramNodeType;

/** Filters an existing diagram without parsing or revalidating its source. */
final class DiagramModeProjector {
    private DiagramModeProjector() {
    }

    static DiagramModel project(DiagramModel source, DiagramViewMode mode) {
        Objects.requireNonNull(source, "source");
        return switch (Objects.requireNonNull(mode, "mode")) {
            case ALL -> source;
            case BDI_PLAN -> filterByNodeTypes(source, EnumSet.of(
                    DiagramNodeType.AGENT,
                    DiagramNodeType.MAS_PROJECT,
                    DiagramNodeType.BELIEF,
                    DiagramNodeType.GOAL,
                    DiagramNodeType.PLAN,
                    DiagramNodeType.TRIGGER,
                    DiagramNodeType.CONTEXT,
                    DiagramNodeType.ACTION,
                    DiagramNodeType.MESSAGE,
                    DiagramNodeType.GAP,
                    DiagramNodeType.ISSUE));
            case AGENT_OVERVIEW -> filterByNodeTypes(source, EnumSet.of(
                    DiagramNodeType.AGENT,
                    DiagramNodeType.MAS_PROJECT,
                    DiagramNodeType.BELIEF,
                    DiagramNodeType.GOAL,
                    DiagramNodeType.PLAN,
                    DiagramNodeType.ACTION,
                    DiagramNodeType.MESSAGE,
                    DiagramNodeType.GAP,
                    DiagramNodeType.ISSUE));
            case MAPPING -> mappingView(source);
        };
    }

    private static DiagramModel filterByNodeTypes(
            DiagramModel source,
            Set<DiagramNodeType> allowedTypes) {
        Set<String> nodeIds = source.nodes().stream()
                .filter(node -> allowedTypes.contains(node.type()))
                .map(DiagramNode::id)
                .collect(Collectors.toSet());
        return rebuild(source, nodeIds, source.edges().stream()
                .filter(edge -> nodeIds.contains(edge.sourceNodeId()) && nodeIds.contains(edge.targetNodeId()))
                .toList());
    }

    private static DiagramModel mappingView(DiagramModel source) {
        Set<DiagramEdgeType> mappingEdges = EnumSet.of(
                DiagramEdgeType.MAPS_TO,
                DiagramEdgeType.MISSING_MAPPING);
        List<DiagramEdge> selectedEdges = source.edges().stream()
                .filter(edge -> mappingEdges.contains(edge.type()))
                .toList();
        Set<String> nodeIds = selectedEdges.stream()
                .flatMap(edge -> java.util.stream.Stream.of(edge.sourceNodeId(), edge.targetNodeId()))
                .collect(Collectors.toSet());
        boolean changed;
        do {
            Set<String> issueTargets = source.edges().stream()
                    .filter(edge -> edge.type() == DiagramEdgeType.HAS_ISSUE)
                    .filter(edge -> nodeIds.contains(edge.sourceNodeId()))
                    .map(DiagramEdge::targetNodeId)
                    .collect(Collectors.toSet());
            changed = nodeIds.addAll(issueTargets);
        } while (changed);
        List<DiagramEdge> edges = source.edges().stream()
                .filter(edge -> nodeIds.contains(edge.sourceNodeId()) && nodeIds.contains(edge.targetNodeId()))
                .filter(edge -> mappingEdges.contains(edge.type()) || edge.type() == DiagramEdgeType.HAS_ISSUE)
                .toList();
        return rebuild(source, nodeIds, edges);
    }

    private static DiagramModel rebuild(
            DiagramModel source,
            Set<String> nodeIds,
            List<DiagramEdge> edges) {
        List<DiagramNode> nodes = source.nodes().stream()
                .filter(node -> nodeIds.contains(node.id()))
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
}

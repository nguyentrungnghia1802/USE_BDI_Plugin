package org.tzi.use.plugins.bdi.trace;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/** Immutable derived graph with deterministic node/edge ordering. */
public record TraceabilityGraph(List<TraceNode> nodes, List<TraceEdge> edges) {
    public TraceabilityGraph {
        nodes = List.copyOf(Objects.requireNonNull(nodes, "nodes")).stream()
                .distinct().sorted(java.util.Comparator.comparing(TraceNode::id)).toList();
        edges = List.copyOf(Objects.requireNonNull(edges, "edges")).stream()
                .distinct().sorted(java.util.Comparator.comparing(TraceEdge::id)).toList();
        Set<String> nodeIds = nodes.stream().map(TraceNode::id).collect(Collectors.toSet());
        if (nodeIds.size() != nodes.size()) {
            throw new IllegalArgumentException("Trace graph contains duplicate node IDs");
        }
        if (edges.stream().map(TraceEdge::id).distinct().count() != edges.size()) {
            throw new IllegalArgumentException("Trace graph contains duplicate edge IDs");
        }
        for (TraceEdge edge : edges) {
            if (!nodeIds.contains(edge.from()) || !nodeIds.contains(edge.to())) {
                throw new IllegalArgumentException("Trace edge references an unknown node: " + edge.id());
            }
        }
    }

    public TraceabilityGraph detailForIssue(String issueNodeId) {
        Objects.requireNonNull(issueNodeId, "issueNodeId");
        Map<String, TraceNode> byId = nodes.stream()
                .collect(Collectors.toMap(TraceNode::id, Function.identity()));
        TraceNode issue = byId.get(issueNodeId);
        if (issue == null || issue.kind() != TraceNodeKind.ISSUE) {
            throw new IllegalArgumentException("Unknown issue node: " + issueNodeId);
        }
        Set<String> included = new HashSet<>();
        ArrayDeque<String> pending = new ArrayDeque<>();
        included.add(issueNodeId);
        pending.add(issueNodeId);
        while (!pending.isEmpty()) {
            String target = pending.removeFirst();
            edges.stream().filter(edge -> edge.to().equals(target)).forEach(edge -> {
                if (included.add(edge.from())) {
                    pending.addLast(edge.from());
                }
            });
        }
        List<TraceNode> detailNodes = included.stream().map(byId::get).toList();
        List<TraceEdge> detailEdges = edges.stream()
                .filter(edge -> included.contains(edge.from()) && included.contains(edge.to()))
                .toList();
        return new TraceabilityGraph(new ArrayList<>(detailNodes), detailEdges);
    }
}

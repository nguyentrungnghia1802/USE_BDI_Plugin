package org.tzi.use.plugins.bdi.diagram;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Pattern;

import org.tzi.use.plugins.bdi.trace.TraceEdge;
import org.tzi.use.plugins.bdi.trace.TraceNode;
import org.tzi.use.plugins.bdi.trace.TraceNodeKind;
import org.tzi.use.plugins.bdi.trace.TraceRelationKind;
import org.tzi.use.plugins.bdi.trace.TraceabilityGraph;
import org.tzi.use.plugins.bdi.validation.IssueSeverity;

/** Projects existing trace evidence into the renderer-neutral diagram model. */
public final class TraceabilityDiagramContributor {
    private static final Pattern ABSOLUTE_SOURCE = Pattern.compile(
            "(?i)(?:[a-z]:[\\\\/]|\\\\\\\\|\\b(?:file|https?)://|^/)");

    public DiagramModel build(TraceabilityGraph graph) {
        Objects.requireNonNull(graph, "graph");
        Map<String, DiagramNode> nodes = new LinkedHashMap<>();
        for (TraceNode traceNode : graph.nodes()) {
            DiagramNode node = node(traceNode);
            nodes.put(traceNode.id(), node);
        }

        Map<String, DiagramEdge> edges = new LinkedHashMap<>();
        for (TraceEdge traceEdge : graph.edges()) {
            DiagramNode source = requireNode(nodes, traceEdge.from());
            DiagramNode target = requireNode(nodes, traceEdge.to());
            DiagramEdgeType type = edgeType(traceEdge.relation());
            String visualKey = type + "\n" + source.id() + "\n" + target.id();
            edges.putIfAbsent(visualKey, new DiagramEdge(
                    type,
                    source.id(),
                    target.id(),
                    DiagramSelectionRef.of("trace-edge", traceEdge.id()),
                    Optional.of(traceEdge.relation().name()),
                    Map.of(
                            "traceRelation", traceEdge.relation().name(),
                            "certainty", traceEdge.certainty().name())));
        }
        return new DiagramModel(new ArrayList<>(nodes.values()), new ArrayList<>(edges.values()), List.of());
    }

    private static DiagramNode node(TraceNode traceNode) {
        Map<String, String> attributes = new LinkedHashMap<>();
        attributes.put("traceId", traceNode.id());
        attributes.put("traceKind", traceNode.kind().name());
        traceNode.status().ifPresent(value -> attributes.put("status", value.name()));
        traceNode.certainty().ifPresent(value -> attributes.put("certainty", value.name()));
        traceNode.ruleId().ifPresent(value -> attributes.put("ruleId", value));
        traceNode.severity().ifPresent(value -> attributes.put("severity", value.name()));

        Optional<DiagramIssueMarker> marker = traceNode.kind() == TraceNodeKind.ISSUE
                ? Optional.of(issueMarker(traceNode)) : Optional.empty();
        return new DiagramNode(
                nodeType(traceNode.kind()),
                DiagramSelectionRef.of("trace", traceNode.id()),
                portableLabel(traceNode.label()),
                traceNode.source(),
                marker,
                attributes);
    }

    private static DiagramIssueMarker issueMarker(TraceNode node) {
        String ruleId = node.ruleId().orElseThrow(() ->
                new IllegalArgumentException("Issue trace node is missing ruleId: " + node.id()));
        IssueSeverity severity = node.severity().orElseThrow(() ->
                new IllegalArgumentException("Issue trace node is missing severity: " + node.id()));
        return new DiagramIssueMarker(
                ruleId,
                severity,
                node.status().orElseThrow(() ->
                        new IllegalArgumentException("Issue trace node is missing status: " + node.id())),
                node.certainty().orElseThrow(() ->
                        new IllegalArgumentException("Issue trace node is missing certainty: " + node.id())),
                node.evidence());
    }

    private static DiagramNode requireNode(Map<String, DiagramNode> nodes, String id) {
        DiagramNode node = nodes.get(id);
        if (node == null) {
            throw new IllegalArgumentException("Trace edge references an unknown diagram node: " + id);
        }
        return node;
    }

    private static DiagramNodeType nodeType(TraceNodeKind kind) {
        return switch (kind) {
            case SOURCE -> DiagramNodeType.TRACE_SOURCE;
            case BDI_ELEMENT, OBSERVABLE_PROPERTY, ORGANIZATION_CARDINALITY -> DiagramNodeType.TRACE_ELEMENT;
            case MAPPING -> DiagramNodeType.TRACE_MAPPING;
            case UML_ELEMENT -> DiagramNodeType.TRACE_TARGET;
            case OCL_CONSTRAINT -> DiagramNodeType.OCL_CONSTRAINT;
            case ENVIRONMENT_ARTIFACT -> DiagramNodeType.ARTIFACT;
            case ARTIFACT_OPERATION -> DiagramNodeType.ARTIFACT_OPERATION;
            case ORGANIZATION -> DiagramNodeType.ORGANIZATION;
            case ORGANIZATION_ROLE -> DiagramNodeType.ROLE;
            case ORGANIZATION_MISSION -> DiagramNodeType.MISSION;
            case GAP -> DiagramNodeType.GAP;
            case ISSUE -> DiagramNodeType.ISSUE;
        };
    }

    private static DiagramEdgeType edgeType(TraceRelationKind relation) {
        return switch (relation) {
            case DECLARES -> DiagramEdgeType.OWNS;
            case MAPPED_BY, TARGETS, ENVIRONMENT_TARGET, ORGANIZATION_TARGET -> DiagramEdgeType.MAPS_TO;
            case EVALUATED_BY -> DiagramEdgeType.CONSTRAINED_BY;
            case MISSING_TARGET, MISSING_MAPPING -> DiagramEdgeType.MISSING_MAPPING;
            case PRODUCES -> DiagramEdgeType.HAS_ISSUE;
        };
    }

    private static String portableLabel(String label) {
        return ABSOLUTE_SOURCE.matcher(label).find()
                ? "[non-portable source omitted]" : label;
    }
}

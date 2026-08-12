package org.tzi.use.plugins.bdi.diagram;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.tzi.use.plugins.bdi.trace.TraceEdge;
import org.tzi.use.plugins.bdi.trace.TraceNode;
import org.tzi.use.plugins.bdi.trace.TraceNodeKind;
import org.tzi.use.plugins.bdi.trace.TraceRelationKind;
import org.tzi.use.plugins.bdi.trace.TraceabilityGraph;
import org.tzi.use.plugins.bdi.validation.IssueCertainty;
import org.tzi.use.plugins.bdi.validation.IssueSeverity;
import org.tzi.use.plugins.bdi.validation.IssueStatus;

class TraceabilityDiagramContributorTest {
    @Test
    void projectsOclIssueChainAndPreservesIssueMetadata() {
        TraceabilityGraph graph = completeGraph();

        DiagramModel diagram = new TraceabilityDiagramContributor().build(graph);

        assertEquals(6, diagram.nodes().size());
        assertEquals(5, diagram.edges().size());
        DiagramNode issue = diagram.nodes().stream()
                .filter(node -> node.type() == DiagramNodeType.ISSUE)
                .findFirst().orElseThrow();
        DiagramIssueMarker marker = issue.issueMarker().orElseThrow();
        assertEquals("OCL-004", marker.ruleId());
        assertEquals(IssueSeverity.ERROR, marker.severity());
        assertEquals(IssueStatus.OPEN, marker.status());
        assertEquals(IssueCertainty.UNKNOWN, marker.certainty());
        assertTrue(diagram.edges().stream().anyMatch(edge -> edge.type() == DiagramEdgeType.CONSTRAINED_BY));
        assertTrue(diagram.edges().stream().anyMatch(edge -> edge.type() == DiagramEdgeType.HAS_ISSUE));
    }

    @Test
    void keepsAnExplicitMissingMappingGap() {
        TraceNode bdi = node("bdi", TraceNodeKind.BDI_ELEMENT, "plan p");
        TraceNode gap = node("gap", TraceNodeKind.GAP, "Missing confirmed mapping");
        TraceNode issue = issue("issue", "MAP-002", "mapping missing");
        TraceabilityGraph graph = new TraceabilityGraph(
                List.of(bdi, gap, issue),
                List.of(edge("missing", "bdi", "gap", TraceRelationKind.MISSING_MAPPING),
                        edge("produces", "gap", "issue", TraceRelationKind.PRODUCES)));

        DiagramModel diagram = new TraceabilityDiagramContributor().build(graph);

        assertTrue(diagram.nodes().stream().anyMatch(node -> node.type() == DiagramNodeType.GAP));
        assertTrue(diagram.edges().stream().anyMatch(edge -> edge.type() == DiagramEdgeType.MISSING_MAPPING));
        assertTrue(diagram.edges().stream().anyMatch(edge -> edge.type() == DiagramEdgeType.HAS_ISSUE));
    }

    @Test
    void deduplicatesEquivalentVisualEdgesAndHidesAbsoluteLabels() {
        TraceNode source = node("source", TraceNodeKind.SOURCE, "C:\\checkout\\auction.asl:1");
        TraceNode target = node("target", TraceNodeKind.UML_ELEMENT, "Auctioneer");
        TraceabilityGraph graph = new TraceabilityGraph(
                List.of(source, target),
                List.of(edge("e1", "source", "target", TraceRelationKind.TARGETS),
                        edge("e2", "source", "target", TraceRelationKind.TARGETS)));

        DiagramModel diagram = new TraceabilityDiagramContributor().build(graph);

        assertEquals(1, diagram.edges().size());
        assertEquals("[non-portable source omitted]", diagram.nodes().stream()
                .filter(node -> node.type() == DiagramNodeType.TRACE_SOURCE)
                .findFirst().orElseThrow().label());
        assertFalse(diagram.nodes().stream().anyMatch(node -> node.label().contains("checkout")));
    }

    @Test
    void equivalentPortableGraphsProduceStableDiagram() {
        TraceabilityGraph first = completeGraph();
        TraceabilityGraph second = new TraceabilityGraph(
                List.copyOf(first.nodes()), List.copyOf(first.edges()));

        assertEquals(new TraceabilityDiagramContributor().build(first),
                new TraceabilityDiagramContributor().build(second));
    }

    private static TraceabilityGraph completeGraph() {
        List<TraceNode> nodes = List.of(
                node("source", TraceNodeKind.SOURCE, "auctioneer.asl"),
                node("bdi", TraceNodeKind.BDI_ELEMENT, "plan sell"),
                node("mapping", TraceNodeKind.MAPPING, "ACTION -> Auctioneer.sell"),
                node("uml", TraceNodeKind.UML_ELEMENT, "Auctioneer.sell"),
                new TraceNode("ocl", TraceNodeKind.OCL_CONSTRAINT, "OCL-004 on Auctioneer.sell",
                        Optional.empty(), Optional.of(IssueStatus.OPEN), Optional.of(IssueCertainty.UNKNOWN),
                        Optional.of("OCL-004"), Optional.of(IssueSeverity.ERROR), List.of("ocl evidence")),
                issue("issue", "OCL-004", "precondition unknown"));
        List<TraceEdge> edges = List.of(
                edge("declares", "source", "bdi", TraceRelationKind.DECLARES),
                edge("mapped", "bdi", "mapping", TraceRelationKind.MAPPED_BY),
                edge("targets", "mapping", "uml", TraceRelationKind.TARGETS),
                edge("evaluated", "uml", "ocl", TraceRelationKind.EVALUATED_BY),
                edge("produces", "ocl", "issue", TraceRelationKind.PRODUCES));
        return new TraceabilityGraph(nodes, edges);
    }

    private static TraceNode node(String id, TraceNodeKind kind, String label) {
        return new TraceNode(id, kind, label, Optional.empty(), Optional.empty(), Optional.empty(), List.of());
    }

    private static TraceNode issue(String id, String ruleId, String label) {
        return new TraceNode(id, TraceNodeKind.ISSUE, ruleId + ": " + label,
                Optional.empty(), Optional.of(IssueStatus.OPEN), Optional.of(IssueCertainty.UNKNOWN),
                Optional.of(ruleId), Optional.of(IssueSeverity.ERROR), List.of("evidence"));
    }

    private static TraceEdge edge(String id, String from, String to, TraceRelationKind relation) {
        return new TraceEdge(id, from, to, relation, IssueCertainty.UNKNOWN, List.of("evidence"));
    }
}

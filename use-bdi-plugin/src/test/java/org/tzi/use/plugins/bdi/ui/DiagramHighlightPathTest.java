package org.tzi.use.plugins.bdi.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.tzi.use.plugins.bdi.diagram.DiagramEdge;
import org.tzi.use.plugins.bdi.diagram.DiagramEdgeType;
import org.tzi.use.plugins.bdi.diagram.DiagramIssueMarker;
import org.tzi.use.plugins.bdi.diagram.DiagramModel;
import org.tzi.use.plugins.bdi.diagram.DiagramNode;
import org.tzi.use.plugins.bdi.diagram.DiagramNodeType;
import org.tzi.use.plugins.bdi.diagram.DiagramSelectionRef;
import org.tzi.use.plugins.bdi.validation.IssueCertainty;
import org.tzi.use.plugins.bdi.validation.IssueSeverity;
import org.tzi.use.plugins.bdi.validation.IssueStatus;

class DiagramHighlightPathTest {
    @Test
    void highlightsMappedIssueEvidencePathAndExcludesUnrelatedNodes() {
        DiagramNode source = node(DiagramNodeType.TRACE_SOURCE, "source", "auction.asl");
        DiagramNode element = node(DiagramNodeType.TRACE_ELEMENT, "element", "action");
        DiagramNode mapping = node(DiagramNodeType.TRACE_MAPPING, "mapping", "action-operation");
        DiagramNode target = node(DiagramNodeType.TRACE_TARGET, "target", "Auction.sell");
        DiagramNode issue = issue("MAP-003");
        DiagramNode unrelated = node(DiagramNodeType.ACTION, "other", "unrelated");
        List<DiagramNode> nodes = List.of(source, element, mapping, target, issue, unrelated);
        List<DiagramEdge> edges = List.of(
                edge(DiagramEdgeType.OWNS, source, element),
                edge(DiagramEdgeType.MAPS_TO, element, mapping),
                edge(DiagramEdgeType.MAPS_TO, mapping, target),
                edge(DiagramEdgeType.HAS_ISSUE, target, issue));

        DiagramHighlightPath.Highlight highlight = DiagramHighlightPath.forIssue(
                new DiagramModel(nodes, edges, List.of()), "MAP-003");

        assertEquals(5, highlight.nodeIds().size());
        assertEquals(4, highlight.edgeIds().size());
        assertTrue(highlight.nodeIds().contains(issue.id()));
        assertTrue(highlight.nodeIds().contains(target.id()));
        assertTrue(!highlight.nodeIds().contains(unrelated.id()));
    }

    @Test
    void highlightsExplicitMissingMappingEvidence() {
        DiagramNode element = node(DiagramNodeType.TRACE_ELEMENT, "element", "action");
        DiagramNode gap = node(DiagramNodeType.GAP, "gap", "missing mapping");
        DiagramNode issue = issue("SIG-001");
        DiagramModel model = new DiagramModel(
                List.of(element, gap, issue),
                List.of(edge(DiagramEdgeType.MISSING_MAPPING, element, gap),
                        edge(DiagramEdgeType.HAS_ISSUE, gap, issue)),
                List.of());

        DiagramHighlightPath.Highlight highlight = DiagramHighlightPath.forIssue(model, "SIG-001");

        assertEquals(3, highlight.nodeIds().size());
        assertEquals(2, highlight.edgeIds().size());
    }

    private static DiagramNode issue(String ruleId) {
        return new DiagramNode(
                DiagramNodeType.ISSUE,
                DiagramSelectionRef.of("issue", ruleId),
                ruleId + " issue",
                Optional.empty(),
                Optional.of(new DiagramIssueMarker(
                        ruleId, IssueSeverity.ERROR, IssueStatus.OPEN, IssueCertainty.CONFIRMED,
                        List.of("trace evidence"))),
                Map.of());
    }

    private static DiagramNode node(DiagramNodeType type, String namespace, String reference) {
        return new DiagramNode(type, DiagramSelectionRef.of(namespace, reference), reference,
                Optional.empty(), Optional.empty(), Map.of());
    }

    private static DiagramEdge edge(DiagramEdgeType type, DiagramNode source, DiagramNode target) {
        return new DiagramEdge(type, source.id(), target.id(),
                DiagramSelectionRef.of("edge", source.id() + "->" + target.id()),
                Optional.empty(), Map.of());
    }
}

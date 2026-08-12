package org.tzi.use.plugins.bdi.diagram;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.tzi.use.plugins.bdi.model.source.ProjectSourceId;
import org.tzi.use.plugins.bdi.validation.IssueCertainty;
import org.tzi.use.plugins.bdi.validation.IssueSeverity;
import org.tzi.use.plugins.bdi.validation.IssueStatus;

class DiagramModelTest {
    @Test
    void ordersEquivalentContentDeterministically() {
        DiagramNode agent = node(DiagramNodeType.AGENT, "agent", "auctioneer", "Auctioneer");
        DiagramNode goal = node(DiagramNodeType.GOAL, "goal", "sell/1", "sell(Item)");
        DiagramEdge edge = edge(DiagramEdgeType.PURSUES_GOAL, agent, goal, "pursues:sell/1");
        DiagramGroup group = new DiagramGroup(
                DiagramSelectionRef.of("agent", "auctioneer"),
                "Auctioneer",
                List.of(goal.id(), agent.id()),
                Map.of());

        DiagramModel first = new DiagramModel(List.of(goal, agent), List.of(edge), List.of(group));
        DiagramModel second = new DiagramModel(List.of(agent, goal), List.of(edge), List.of(group));

        assertEquals(first, second);
        assertEquals(first.nodes().stream().map(DiagramNode::id).sorted().toList(),
                first.nodes().stream().map(DiagramNode::id).toList());
        assertEquals(List.of(agent.id(), goal.id()).stream().sorted().toList(), group.nodeIds());
    }

    @Test
    void rejectsDuplicateNodeAndEdgeIdentities() {
        DiagramNode first = node(DiagramNodeType.AGENT, "agent", "bidder", "Bidder");
        DiagramNode duplicate = node(DiagramNodeType.AGENT, "agent", "bidder", "Renamed bidder");

        IllegalArgumentException nodeError = assertThrows(IllegalArgumentException.class,
                () -> new DiagramModel(List.of(first, duplicate), List.of(), List.of()));
        assertTrue(nodeError.getMessage().contains("duplicate node ID"));

        DiagramNode goal = node(DiagramNodeType.GOAL, "goal", "bid/2", "bid(Item, Price)");
        DiagramEdge edge = edge(DiagramEdgeType.PURSUES_GOAL, first, goal, "pursues:bid/2");
        IllegalArgumentException edgeError = assertThrows(IllegalArgumentException.class,
                () -> new DiagramModel(List.of(first, goal), List.of(edge, edge), List.of()));
        assertTrue(edgeError.getMessage().contains("duplicate edge ID"));
    }

    @Test
    void rejectsEdgesAndGroupsWithMissingEndpoints() {
        DiagramNode agent = node(DiagramNodeType.AGENT, "agent", "bidder", "Bidder");
        DiagramNode missing = node(DiagramNodeType.GOAL, "goal", "win/1", "win(Item)");
        DiagramEdge edge = edge(DiagramEdgeType.PURSUES_GOAL, agent, missing, "pursues:win/1");

        IllegalArgumentException edgeError = assertThrows(IllegalArgumentException.class,
                () -> new DiagramModel(List.of(agent), List.of(edge), List.of()));
        assertTrue(edgeError.getMessage().contains("unknown node"));

        DiagramGroup group = new DiagramGroup(
                DiagramSelectionRef.of("agent", "bidder"),
                "Bidder",
                List.of(agent.id(), missing.id()),
                Map.of());
        IllegalArgumentException groupError = assertThrows(IllegalArgumentException.class,
                () -> new DiagramModel(List.of(agent), List.of(), List.of(group)));
        assertTrue(groupError.getMessage().contains("unknown node"));
    }

    @Test
    void copiesCollectionsAndPreservesIssueEvidence() {
        List<String> evidence = new ArrayList<>(List.of("Auction::open"));
        Map<String, String> attributes = new HashMap<>(Map.of("qualifiedRef", "Auction::open"));
        DiagramIssueMarker marker = new DiagramIssueMarker(
                "OCL-004",
                IssueSeverity.WARNING,
                IssueStatus.OPEN,
                IssueCertainty.UNKNOWN,
                evidence);
        DiagramNode issue = new DiagramNode(
                DiagramNodeType.ISSUE,
                DiagramSelectionRef.of("trace-node", "issue:OCL-004:auction-open"),
                "OCL result is unknown",
                Optional.empty(),
                Optional.of(marker),
                attributes);
        List<DiagramNode> nodes = new ArrayList<>(List.of(issue));
        DiagramModel model = new DiagramModel(nodes, List.of(), List.of());

        evidence.add("late mutation");
        attributes.put("late", "mutation");
        nodes.clear();

        assertEquals(List.of("Auction::open"), issue.issueMarker().orElseThrow().evidence());
        assertEquals(Map.of("qualifiedRef", "Auction::open"), issue.attributes());
        assertEquals(List.of(issue), model.nodes());
        assertEquals(IssueStatus.OPEN, issue.issueMarker().orElseThrow().status());
        assertEquals(IssueCertainty.UNKNOWN, issue.issueMarker().orElseThrow().certainty());
        assertThrows(UnsupportedOperationException.class, () -> model.nodes().add(issue));
        assertThrows(UnsupportedOperationException.class, () -> issue.attributes().put("x", "y"));
    }

    @Test
    void keepsNodeIdentityStableWhenCheckoutRootChanges(@TempDir Path tempDir) {
        Path firstRoot = tempDir.resolve("checkout-one");
        Path secondRoot = tempDir.resolve("checkout-two");
        ProjectSourceId firstSource = ProjectSourceId.fromPath(firstRoot, firstRoot.resolve("agents/bidder.asl"));
        ProjectSourceId secondSource = ProjectSourceId.fromPath(secondRoot, secondRoot.resolve("agents/bidder.asl"));
        DiagramNode first = new DiagramNode(
                DiagramNodeType.AGENT,
                DiagramSelectionRef.source(firstSource),
                "Bidder",
                Optional.of(firstSource),
                Optional.empty(),
                Map.of());
        DiagramNode second = new DiagramNode(
                DiagramNodeType.AGENT,
                DiagramSelectionRef.source(secondSource),
                "Bidder",
                Optional.of(secondSource),
                Optional.empty(),
                Map.of());

        assertEquals(firstSource, secondSource);
        assertEquals(first.id(), second.id());
        assertEquals(first, second);
        assertFalse(first.id().contains(firstRoot.toAbsolutePath().toString()));
    }

    @Test
    void rejectsAbsoluteSelectionReferences() {
        assertThrows(IllegalArgumentException.class,
                () -> DiagramSelectionRef.of("source", "D:\\checkout\\agents\\bidder.asl"));
        assertThrows(IllegalArgumentException.class,
                () -> DiagramSelectionRef.of("source", "/checkout/agents/bidder.asl"));
        assertThrows(IllegalArgumentException.class,
                () -> DiagramSelectionRef.of("source", "file:/checkout/agents/bidder.asl"));
    }

    private static DiagramNode node(
            DiagramNodeType type,
            String namespace,
            String reference,
            String label) {
        return new DiagramNode(
                type,
                DiagramSelectionRef.of(namespace, reference),
                label,
                Optional.empty(),
                Optional.empty(),
                Map.of());
    }

    private static DiagramEdge edge(
            DiagramEdgeType type,
            DiagramNode source,
            DiagramNode target,
            String reference) {
        return new DiagramEdge(
                type,
                source.id(),
                target.id(),
                DiagramSelectionRef.of("relation", reference),
                Optional.empty(),
                Map.of());
    }
}

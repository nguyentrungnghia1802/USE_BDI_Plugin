package org.tzi.use.plugins.bdi.casestudy;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.URISyntaxException;
import java.nio.file.Path;
import java.time.Instant;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.tzi.use.plugins.bdi.application.BdiImportService;
import org.tzi.use.plugins.bdi.application.BdiImportSnapshot;
import org.tzi.use.plugins.bdi.application.CurrentAnalysisSnapshot;
import org.tzi.use.plugins.bdi.application.CurrentAnalysisSnapshotService;
import org.tzi.use.plugins.bdi.diagram.BdiDiagramBuilder;
import org.tzi.use.plugins.bdi.diagram.DiagramEdge;
import org.tzi.use.plugins.bdi.diagram.DiagramEdgeType;
import org.tzi.use.plugins.bdi.diagram.DiagramModel;
import org.tzi.use.plugins.bdi.diagram.DiagramNode;
import org.tzi.use.plugins.bdi.diagram.DiagramNodeType;
import org.tzi.use.plugins.bdi.index.BdiIndexBuilder;
import org.tzi.use.plugins.bdi.model.ir.AgentModel;
import org.tzi.use.plugins.bdi.model.ir.BeliefModel;
import org.tzi.use.plugins.bdi.model.ir.LiteralTermModel;
import org.tzi.use.plugins.bdi.model.ir.SourceSpan;
import org.tzi.use.plugins.bdi.model.mapping.MappingDocument;
import org.tzi.use.plugins.bdi.model.mapping.MappingSuggestionService;
import org.tzi.use.plugins.bdi.use.UseModelSnapshot;
import org.tzi.use.plugins.bdi.use.UseUmlModelFacade;
import org.tzi.use.plugins.bdi.validation.ValidationOrchestrator;
import org.tzi.use.uml.sys.MSystem;

class BdiDiagramBuilderTest {
    private static final Instant FIXED_TIME = Instant.parse("2026-08-13T00:00:00Z");

    @Test
    void projectsMinimalSourceAndIndexSupportingPlanWithoutReparsing() throws Exception {
        Path source = fixture("fixtures/asl/valid/minimal.asl");
        BdiImportSnapshot imported = new BdiImportService().importFiles(List.of(source));
        CurrentAnalysisSnapshot snapshot = snapshot(
                imported,
                Optional.empty(),
                MappingDocument.empty("no-use-model"));

        BdiDiagramBuilder builder = new BdiDiagramBuilder();
        DiagramModel first = builder.build(snapshot, source.getParent());
        DiagramModel second = builder.build(snapshot, source.getParent());

        assertEquals(first, second);
        assertEquals(1, first.groups().size());
        assertTrue(nodeTypes(first).containsAll(EnumSet.of(
                DiagramNodeType.AGENT,
                DiagramNodeType.BELIEF,
                DiagramNodeType.GOAL,
                DiagramNodeType.PLAN,
                DiagramNodeType.TRIGGER,
                DiagramNodeType.CONTEXT,
                DiagramNodeType.ACTION,
                DiagramNodeType.GAP)));
        assertEquals(1, edges(first, DiagramEdgeType.SUPPORTED_BY).size());
        assertEquals(1, edges(first, DiagramEdgeType.PURSUES_GOAL).size());
        assertEquals(1, edges(first, DiagramEdgeType.HAS_BELIEF).size());
        assertFalse(first.nodes().stream().anyMatch(node -> node.type().name().startsWith("UML_")));
    }

    @Test
    void keepsSmartQueueStepOrderAndMessageReceiverGapsVisible() throws Exception {
        Path source = fixture("fixtures/smartqueue/Smart_manager_agent.asl");
        BdiImportSnapshot imported = new BdiImportService().importFiles(List.of(source));
        DiagramModel diagram = new BdiDiagramBuilder().build(
                snapshot(imported, Optional.empty(), MappingDocument.empty("no-use-model")),
                source.getParent());

        List<DiagramNode> messages = diagram.nodes().stream()
                .filter(node -> node.type() == DiagramNodeType.MESSAGE)
                .toList();
        assertEquals(2, messages.size());
        assertEquals(2, edges(diagram, DiagramEdgeType.SENDS_MESSAGE).size());
        assertTrue(messages.stream().allMatch(message -> edges(diagram, DiagramEdgeType.MISSING_MAPPING).stream()
                .anyMatch(edge -> edge.sourceNodeId().equals(message.id()))));

        List<Integer> stepOrders = edges(diagram, DiagramEdgeType.EXECUTES).stream()
                .map(edge -> Integer.parseInt(edge.attributes().get("order")))
                .toList();
        assertTrue(stepOrders.stream().allMatch(order -> order >= 1));
        for (DiagramNode plan : diagram.nodes().stream()
                .filter(node -> node.type() == DiagramNodeType.PLAN).toList()) {
            List<Integer> planOrders = edges(diagram, DiagramEdgeType.EXECUTES).stream()
                    .filter(edge -> edge.sourceNodeId().equals(plan.id()))
                    .map(edge -> Integer.parseInt(edge.attributes().get("order")))
                    .sorted()
                    .toList();
            assertEquals(java.util.stream.IntStream.rangeClosed(1, planOrders.size()).boxed().toList(), planOrders);
        }
        assertFalse(diagram.toString().contains(source.getParent().toAbsolutePath().toString()));
    }

    @Test
    void projectsConfirmedAuctionMappingsAcrossSeparateAgentGroupsWithoutStateMutation() throws Exception {
        BdiImportSnapshot imported = auctionImport();
        MSystem system = AuctionMappingFixtureTest.loadAuctionSystem();
        UseUmlModelFacade facade = new UseUmlModelFacade();
        UseModelSnapshot uml = facade.snapshot(system);
        MappingDocument mapping = AuctionMappingFixtureTest.confirmedMapping(imported, uml);
        CurrentAnalysisSnapshot snapshot = snapshot(imported, Optional.of(uml), mapping);
        String before = facade.snapshot(system).fingerprint();

        DiagramModel diagram = new BdiDiagramBuilder().build(snapshot, imported.models().get(0).source().getParent());

        assertEquals(before, facade.snapshot(system).fingerprint());
        assertEquals(2, diagram.groups().size());
        assertTrue(diagram.nodes().stream().anyMatch(node ->
                node.type() == DiagramNodeType.UML_OPERATION
                        && node.label().startsWith("Auction::open(")));
        assertTrue(diagram.nodes().stream().anyMatch(node ->
                node.type() == DiagramNodeType.UML_ATTRIBUTE
                        && node.label().equals("Auction::status")));
        assertTrue(edges(diagram, DiagramEdgeType.MAPS_TO).stream()
                .allMatch(edge -> edge.attributes().get("mappingStatus").equals("CONFIRMED")));
        assertTrue(edges(diagram, DiagramEdgeType.MAPS_TO).stream()
                .allMatch(edge -> Set.of("CURRENT", "STALE", "UNKNOWN")
                        .contains(edge.attributes().get("targetState"))));
        assertTrue(diagram.nodes().stream()
                .filter(node -> node.type().name().startsWith("UML_"))
                .allMatch(node -> node.attributes().get("targetState").equals("CURRENT")));
    }

    @Test
    void doesNotPromoteAuctionCandidatesWhenNoBindingWasConfirmed() throws Exception {
        BdiImportSnapshot imported = auctionImport();
        UseModelSnapshot uml = new UseUmlModelFacade().snapshot(AuctionMappingFixtureTest.loadAuctionSystem());
        assertFalse(new MappingSuggestionService().suggest(imported.models(), imported.index(), uml).isEmpty());
        MappingDocument empty = MappingDocument.empty(uml.fingerprint());
        DiagramModel diagram = new BdiDiagramBuilder().build(
                snapshot(imported, Optional.of(uml), empty),
                imported.models().get(0).source().getParent());

        assertTrue(edges(diagram, DiagramEdgeType.MAPS_TO).isEmpty());
        assertFalse(edges(diagram, DiagramEdgeType.MISSING_MAPPING).isEmpty());
        assertTrue(diagram.nodes().stream()
                .filter(node -> node.type() == DiagramNodeType.GAP)
                .allMatch(node -> node.attributes().get("mappingStatus").equals("MISSING")));
    }

    @Test
    void keepsElementsDistinctWhenSourceCoordinatesAreUnknown(@TempDir Path projectRoot) {
        Path source = projectRoot.resolve("agents/unknown-lines.asl");
        SourceSpan unknown = SourceSpan.unknown(source);
        BeliefModel ready = new BeliefModel(
                new LiteralTermModel("ready", List.of(), false, List.of(), unknown), unknown);
        BeliefModel waiting = new BeliefModel(
                new LiteralTermModel("waiting", List.of(), false, List.of(), unknown), unknown);
        AgentModel model = new AgentModel(
                source,
                "test-parser",
                2,
                0,
                0,
                List.of(ready, waiting),
                List.of(),
                List.of(),
                List.of());
        BdiImportSnapshot imported = new BdiImportSnapshot(
                List.of(model), List.of(), new BdiIndexBuilder().build(model));

        DiagramModel diagram = new BdiDiagramBuilder().build(
                snapshot(imported, Optional.empty(), MappingDocument.empty("no-use-model")),
                projectRoot);

        List<DiagramNode> beliefs = diagram.nodes().stream()
                .filter(node -> node.type() == DiagramNodeType.BELIEF)
                .toList();
        assertEquals(2, beliefs.size());
        assertEquals(2, beliefs.stream().map(DiagramNode::id).distinct().count());
    }

    private static CurrentAnalysisSnapshot snapshot(
            BdiImportSnapshot imported,
            Optional<UseModelSnapshot> uml,
            MappingDocument mapping) {
        return new CurrentAnalysisSnapshotService(
                new ValidationOrchestrator(),
                "test configuration",
                "0.1.0",
                "USE-7.1.1")
                .create(FIXED_TIME, imported, uml, Optional.empty(), mapping);
    }

    private static BdiImportSnapshot auctionImport() throws Exception {
        return new BdiImportService().importFiles(List.of(
                AuctionMappingFixtureTest.fixture("fixtures/casestudy/auction/auctioneer.asl"),
                AuctionMappingFixtureTest.fixture("fixtures/casestudy/auction/bidder.asl")));
    }

    private static Path fixture(String resource) throws URISyntaxException {
        return Path.of(Objects.requireNonNull(
                BdiDiagramBuilderTest.class.getClassLoader().getResource(resource), resource).toURI());
    }

    private static EnumSet<DiagramNodeType> nodeTypes(DiagramModel diagram) {
        return diagram.nodes().stream().map(DiagramNode::type)
                .collect(java.util.stream.Collectors.toCollection(() -> EnumSet.noneOf(DiagramNodeType.class)));
    }

    private static List<DiagramEdge> edges(DiagramModel diagram, DiagramEdgeType type) {
        return diagram.edges().stream().filter(edge -> edge.type() == type).toList();
    }
}

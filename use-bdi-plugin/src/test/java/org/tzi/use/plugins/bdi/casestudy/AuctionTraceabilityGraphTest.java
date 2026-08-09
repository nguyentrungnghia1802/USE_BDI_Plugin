package org.tzi.use.plugins.bdi.casestudy;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;
import org.tzi.use.plugins.bdi.application.BdiImportService;
import org.tzi.use.plugins.bdi.application.BdiImportSnapshot;
import org.tzi.use.plugins.bdi.application.CurrentAnalysisSnapshot;
import org.tzi.use.plugins.bdi.application.CurrentAnalysisSnapshotService;
import org.tzi.use.plugins.bdi.model.mapping.MappingDocument;
import org.tzi.use.plugins.bdi.trace.TraceNode;
import org.tzi.use.plugins.bdi.trace.TraceNodeKind;
import org.tzi.use.plugins.bdi.trace.TraceRelationKind;
import org.tzi.use.plugins.bdi.trace.TraceabilityGraph;
import org.tzi.use.plugins.bdi.trace.TraceabilityGraphBuilder;
import org.tzi.use.plugins.bdi.trace.TraceabilityGraphJsonSerializer;
import org.tzi.use.plugins.bdi.use.UseModelSnapshot;
import org.tzi.use.plugins.bdi.use.UseUmlModelFacade;
import org.tzi.use.plugins.bdi.validation.ConsistencyIssue;
import org.tzi.use.plugins.bdi.validation.IssueCertainty;
import org.tzi.use.plugins.bdi.validation.IssueStatus;
import org.tzi.use.plugins.bdi.validation.ValidationOrchestrator;

class AuctionTraceabilityGraphTest {
    private static final Instant FIXED_TIME = Instant.parse("2026-08-10T00:00:00Z");

    @Test
    void explainsUnknownAuctionOclIssueWithPortableStableChain() throws Exception {
        Fixture fixture = fixture(true);
        TraceabilityGraph graph = new TraceabilityGraphBuilder().build(fixture.snapshot(), fixture.projectRoot());
        TraceNode issue = graph.nodes().stream()
                .filter(node -> node.kind() == TraceNodeKind.ISSUE)
                .filter(node -> node.label().startsWith("OCL-004:"))
                .findFirst()
                .orElseThrow();

        TraceabilityGraph detail = graph.detailForIssue(issue.id());
        assertEquals(EnumSet.of(
                TraceNodeKind.SOURCE,
                TraceNodeKind.BDI_ELEMENT,
                TraceNodeKind.MAPPING,
                TraceNodeKind.UML_ELEMENT,
                TraceNodeKind.OCL_CONSTRAINT,
                TraceNodeKind.ISSUE), kinds(detail));
        assertEquals(EnumSet.of(
                TraceRelationKind.DECLARES,
                TraceRelationKind.MAPPED_BY,
                TraceRelationKind.TARGETS,
                TraceRelationKind.EVALUATED_BY,
                TraceRelationKind.PRODUCES), relations(detail));
        assertEquals(Optional.of(IssueStatus.OPEN), issue.status());
        assertEquals(Optional.of(IssueCertainty.UNKNOWN), issue.certainty());
        assertTrue(detail.edges().stream().allMatch(edge -> edge.certainty() == IssueCertainty.UNKNOWN));

        TraceabilityGraphJsonSerializer serializer = new TraceabilityGraphJsonSerializer();
        String first = serializer.serialize(graph);
        String second = serializer.serialize(new TraceabilityGraphBuilder()
                .build(fixture.snapshot(), fixture.projectRoot()));
        assertEquals(first, second);
        assertFalse(first.contains(fixture.projectRoot().toAbsolutePath().toString()));
        assertFalse(first.matches("(?s).*\\b[A-Za-z]:\\\\.*"), first);
    }

    @Test
    void representsMissingActionMappingAsExplicitGap() throws Exception {
        Fixture fixture = fixture(false);
        TraceabilityGraph graph = new TraceabilityGraphBuilder().build(fixture.snapshot(), fixture.projectRoot());
        TraceNode issue = graph.nodes().stream()
                .filter(node -> node.kind() == TraceNodeKind.ISSUE)
                .filter(node -> node.label().startsWith("MAP-002:"))
                .findFirst()
                .orElseThrow();

        TraceabilityGraph detail = graph.detailForIssue(issue.id());
        assertEquals(EnumSet.of(TraceNodeKind.SOURCE, TraceNodeKind.BDI_ELEMENT,
                TraceNodeKind.GAP, TraceNodeKind.ISSUE), kinds(detail));
        assertEquals(EnumSet.of(TraceRelationKind.DECLARES, TraceRelationKind.MISSING_MAPPING,
                TraceRelationKind.PRODUCES), relations(detail));
        assertFalse(detail.nodes().stream().anyMatch(node -> node.kind() == TraceNodeKind.MAPPING));
    }

    @Test
    void duplicateSemanticIssueDoesNotDuplicateGraphContent() throws Exception {
        Fixture fixture = fixture(true);
        CurrentAnalysisSnapshot original = fixture.snapshot();
        List<ConsistencyIssue> duplicatedIssues = new ArrayList<>(original.issues());
        duplicatedIssues.add(original.issues().get(0));
        CurrentAnalysisSnapshot duplicated = new CurrentAnalysisSnapshot(
                original.timestamp(), original.bdiImport(), original.useModel(), original.mapping(),
                original.configurationOrigin(), original.suppressions(), duplicatedIssues,
                original.importedFileCount(), original.mappingCount(), duplicatedIssues.size(),
                original.modelHash(), original.mappingHash(), original.versions());

        TraceabilityGraphBuilder builder = new TraceabilityGraphBuilder();
        TraceabilityGraph expected = builder.build(original, fixture.projectRoot());
        TraceabilityGraph actual = builder.build(duplicated, fixture.projectRoot());
        assertEquals(expected, actual);
        assertEquals(actual.nodes().size(), actual.nodes().stream().map(TraceNode::id).distinct().count());
        assertEquals(actual.edges().size(), actual.edges().stream().map(edge -> edge.id()).distinct().count());
    }

    private static EnumSet<TraceNodeKind> kinds(TraceabilityGraph graph) {
        return graph.nodes().stream().map(TraceNode::kind)
                .collect(Collectors.toCollection(() -> EnumSet.noneOf(TraceNodeKind.class)));
    }

    private static EnumSet<TraceRelationKind> relations(TraceabilityGraph graph) {
        return graph.edges().stream().map(edge -> edge.relation())
                .collect(Collectors.toCollection(() -> EnumSet.noneOf(TraceRelationKind.class)));
    }

    private static Fixture fixture(boolean confirmedMapping) throws Exception {
        BdiImportSnapshot imported = new BdiImportService().importFiles(List.of(
                AuctionMappingFixtureTest.fixture("fixtures/casestudy/auction/auctioneer.asl"),
                AuctionMappingFixtureTest.fixture("fixtures/casestudy/auction/bidder.asl")));
        UseModelSnapshot uml = new UseUmlModelFacade().snapshot(AuctionMappingFixtureTest.loadAuctionSystem());
        MappingDocument mapping = confirmedMapping
                ? AuctionMappingFixtureTest.confirmedMapping(imported, uml)
                : MappingDocument.empty(uml.fingerprint());
        CurrentAnalysisSnapshot snapshot = new CurrentAnalysisSnapshotService(
                new ValidationOrchestrator(), "test configuration", "0.1.0", "USE-7.1.1")
                .create(FIXED_TIME, imported, Optional.of(uml), Optional.empty(), mapping);
        return new Fixture(snapshot, imported.models().get(0).source().getParent());
    }

    private record Fixture(CurrentAnalysisSnapshot snapshot, java.nio.file.Path projectRoot) {
    }
}

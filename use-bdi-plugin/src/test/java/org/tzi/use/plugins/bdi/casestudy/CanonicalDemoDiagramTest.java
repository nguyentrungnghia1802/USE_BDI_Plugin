package org.tzi.use.plugins.bdi.casestudy;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.tzi.use.parser.use.USECompiler;
import org.tzi.use.plugins.bdi.application.BdiImportService;
import org.tzi.use.plugins.bdi.application.BdiImportSnapshot;
import org.tzi.use.plugins.bdi.application.BdiProjectConfiguration;
import org.tzi.use.plugins.bdi.application.CurrentAnalysisSnapshot;
import org.tzi.use.plugins.bdi.application.CurrentAnalysisSnapshotService;
import org.tzi.use.plugins.bdi.application.MasProjectAnalysisRequest;
import org.tzi.use.plugins.bdi.application.MasProjectAnalysisResult;
import org.tzi.use.plugins.bdi.application.MasProjectAnalysisService;
import org.tzi.use.plugins.bdi.diagram.BdiDiagramBuilder;
import org.tzi.use.plugins.bdi.diagram.DiagramEdgeType;
import org.tzi.use.plugins.bdi.diagram.DiagramModel;
import org.tzi.use.plugins.bdi.diagram.DiagramNodeType;
import org.tzi.use.plugins.bdi.diagram.MasOverviewDiagramBuilder;
import org.tzi.use.plugins.bdi.diagram.TraceabilityDiagramContributor;
import org.tzi.use.plugins.bdi.model.mapping.MappingDocument;
import org.tzi.use.plugins.bdi.persistence.MappingFileRepository;
import org.tzi.use.plugins.bdi.trace.TraceabilityGraphBuilder;
import org.tzi.use.plugins.bdi.use.UseModelSnapshot;
import org.tzi.use.plugins.bdi.use.UseUmlModelFacade;
import org.tzi.use.plugins.bdi.validation.ValidationOrchestrator;
import org.tzi.use.uml.mm.MModel;
import org.tzi.use.uml.mm.ModelFactory;
import org.tzi.use.uml.sys.MSystem;

/** Protects the presentation paths in the user-facing canonical demo bundles. */
class CanonicalDemoDiagramTest {
    private static final Instant FIXED_TIME = Instant.parse("2026-08-13T00:00:00Z");

    @Test
    void familyPersonRendersCompactGoalPlanActionToUmlPath() throws Exception {
        Path demo = demo("family-person");
        CurrentAnalysisSnapshot snapshot = directAnalysis(
                demo.resolve("person.asl"), demo.resolve("FamilyPerson.bdimap.json"));

        DiagramModel diagram = new BdiDiagramBuilder().build(snapshot, demo);

        assertTrue(hasLabel(diagram, DiagramNodeType.GOAL, "introduce_family"));
        assertTrue(hasLabel(diagram, DiagramNodeType.PLAN, "introduce_family"));
        assertTrue(hasLabel(diagram, DiagramNodeType.ACTION, "greet"));
        assertTrue(hasLabel(diagram, DiagramNodeType.UML_OPERATION, "Person::greet()"));
        assertTrue(hasEdge(diagram, DiagramEdgeType.SUPPORTED_BY));
        assertTrue(hasEdge(diagram, DiagramEdgeType.MAPS_TO));
        assertTrue(diagram.nodes().size() <= 12, () -> "Family diagram is no longer compact: " + diagram.nodes().size());
    }

    @Test
    void smartQueueRendersDecisionContextAndConfirmedAssignmentTarget() throws Exception {
        Path demo = demo("smart-queue");
        CurrentAnalysisSnapshot snapshot = directAnalysis(
                demo.resolve("smart_queue_manager.asl"), demo.resolve("SmartQueue.bdimap.json"));

        DiagramModel diagram = new BdiDiagramBuilder().build(snapshot, demo);

        assertTrue(hasLabel(diagram, DiagramNodeType.GOAL, "reduce_waiting_time"));
        assertTrue(hasLabel(diagram, DiagramNodeType.PLAN, "assign_customer"));
        assertTrue(hasLabel(diagram, DiagramNodeType.CONTEXT, "queue_length"));
        assertTrue(hasLabel(diagram, DiagramNodeType.ACTION, "assignCustomer"));
        assertTrue(hasLabel(diagram, DiagramNodeType.UML_OPERATION, "Manager::assignCustomer("));
        assertTrue(hasEdge(diagram, DiagramEdgeType.REQUIRES_CONTEXT));
        assertTrue(hasEdge(diagram, DiagramEdgeType.SUPPORTED_BY));
        assertTrue(hasEdge(diagram, DiagramEdgeType.MAPS_TO));
    }

    @Test
    void smartHomeSeparatesResidentDecisionFromStaticEnvironment() throws Exception {
        Path demo = demo("smart-home");
        MasProjectAnalysisResult analysis = projectAnalysis(
                demo.resolve("smart-home.jcm"), demo.resolve("SmartHome.bdimap.json"), Optional.empty());

        DiagramModel bdi = new BdiDiagramBuilder().build(analysis.snapshot(), demo);
        DiagramModel overview = new MasOverviewDiagramBuilder().build(
                analysis.project().orElseThrow(), analysis.snapshot(), demo);

        assertTrue(hasLabel(bdi, DiagramNodeType.GOAL, "prepare_evening"));
        assertTrue(hasLabel(bdi, DiagramNodeType.ACTION, "turn_on_lights"));
        assertTrue(hasLabel(bdi, DiagramNodeType.UML_OPERATION, "Resident::turn_on_lights()"));
        assertTrue(overview.nodes().stream().anyMatch(node -> node.type() == DiagramNodeType.AGENT
                && node.label().contains("resident") && node.attributes().get("layer").equals("BDI")));
        assertTrue(overview.nodes().stream().anyMatch(node -> node.type() == DiagramNodeType.ARTIFACT
                && node.label().contains("smart_home_environment")
                && node.attributes().get("layer").equals("ENVIRONMENT")));
        assertTrue(overview.groups().stream().anyMatch(group -> "true".equals(group.attributes().get("staticOnly"))));
    }

    @Test
    void auctionRendersReadableMasBdiUmlOclAndIssueOverview() throws Exception {
        Path demo = demo("auction");
        UseModelSnapshot uml = useSnapshot(demo.resolve("Auction.use"));
        MasProjectAnalysisResult analysis = projectAnalysis(
                demo.resolve("auction.jcm"), demo.resolve("Auction.bdimap.json"), Optional.of(uml));

        DiagramModel bdi = new BdiDiagramBuilder().build(analysis.snapshot(), demo);
        DiagramModel overview = new MasOverviewDiagramBuilder().build(
                analysis.project().orElseThrow(), analysis.snapshot(), demo);
        DiagramModel evidence = new TraceabilityDiagramContributor().build(
                new TraceabilityGraphBuilder().build(analysis.snapshot(), demo));

        assertEquals(3, overview.nodes().stream().filter(node -> node.type() == DiagramNodeType.AGENT).count());
        assertTrue(overview.nodes().stream().anyMatch(node -> node.type() == DiagramNodeType.ORGANIZATION));
        assertTrue(overview.nodes().stream().anyMatch(node -> node.type() == DiagramNodeType.ARTIFACT));
        assertTrue(bdi.nodes().stream().anyMatch(node -> node.type() == DiagramNodeType.ACTION));
        assertTrue(bdi.nodes().stream().anyMatch(node -> node.type() == DiagramNodeType.MESSAGE));
        assertTrue(bdi.nodes().stream().anyMatch(node -> node.type().name().startsWith("UML_")));
        assertTrue(evidence.nodes().stream().anyMatch(node -> node.type() == DiagramNodeType.OCL_CONSTRAINT));
        assertTrue(evidence.nodes().stream().anyMatch(node -> node.type() == DiagramNodeType.ISSUE));
        assertTrue(overview.nodes().size() < 40, () -> "MAS Overview is no longer bounded: " + overview.nodes().size());
    }

    private static CurrentAnalysisSnapshot directAnalysis(Path source, Path mappingFile) throws Exception {
        BdiImportSnapshot imported = new BdiImportService().importFiles(List.of(source));
        assertTrue(imported.diagnostics().isEmpty(), () -> "Unexpected import diagnostics: " + imported.diagnostics());
        MappingDocument mapping = new MappingFileRepository().load(mappingFile, source.getParent());
        return new CurrentAnalysisSnapshotService(
                new ValidationOrchestrator(), "canonical demo test", "0.1.0", "USE-7.1.1")
                .create(FIXED_TIME, imported, Optional.empty(), Optional.empty(), mapping);
    }

    private static MasProjectAnalysisResult projectAnalysis(
            Path projectFile,
            Path mappingFile,
            Optional<UseModelSnapshot> useModel) throws Exception {
        Path root = projectFile.getParent();
        MappingDocument mapping = new MappingFileRepository().load(mappingFile, root);
        MasProjectAnalysisResult result = new MasProjectAnalysisService().analyze(MasProjectAnalysisRequest.of(
                projectFile,
                FIXED_TIME,
                useModel,
                Optional.empty(),
                mapping,
                BdiProjectConfiguration.defaults()));
        assertTrue(result.project().isPresent(), () -> "Project did not normalize: " + result.projectDiagnostics());
        assertTrue(result.snapshot().bdiImport().models().size() > 0);
        return result;
    }

    private static UseModelSnapshot useSnapshot(Path specification) throws Exception {
        StringWriter errors = new StringWriter();
        MModel model;
        try (var input = Files.newInputStream(specification)) {
            model = USECompiler.compileSpecification(
                    input, specification.toString(), new PrintWriter(errors), new ModelFactory());
        }
        assertNotNull(model, errors::toString);
        model.setFilename(specification.toString());
        return new UseUmlModelFacade().snapshot(new MSystem(model));
    }

    private static boolean hasLabel(DiagramModel diagram, DiagramNodeType type, String fragment) {
        return diagram.nodes().stream().anyMatch(node -> node.type() == type && node.label().contains(fragment));
    }

    private static boolean hasEdge(DiagramModel diagram, DiagramEdgeType type) {
        return diagram.edges().stream().anyMatch(edge -> edge.type() == type);
    }

    private static Path demo(String name) {
        return repositoryRoot().resolve("use-bdi-plugin/demo").resolve(name);
    }

    private static Path repositoryRoot() {
        Path current = Path.of("").toAbsolutePath().normalize();
        while (current != null) {
            if (Files.isDirectory(current.resolve("use-bdi-plugin/demo"))) {
                return current;
            }
            current = current.getParent();
        }
        throw new IllegalStateException("Could not locate repository root from the test working directory");
    }
}

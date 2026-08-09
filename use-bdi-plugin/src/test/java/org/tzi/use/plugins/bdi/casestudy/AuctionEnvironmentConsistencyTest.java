package org.tzi.use.plugins.bdi.casestudy;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.RecordComponent;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.tzi.use.plugins.bdi.importer.CArtAgOArtifactAdapter;
import org.tzi.use.plugins.bdi.model.environment.ArtifactModel;
import org.tzi.use.plugins.bdi.model.environment.EnvironmentMapping;
import org.tzi.use.plugins.bdi.model.environment.EnvironmentModel;
import org.tzi.use.plugins.bdi.model.environment.EnvironmentOperationMapping;
import org.tzi.use.plugins.bdi.model.environment.EnvironmentPropertyMapping;
import org.tzi.use.plugins.bdi.model.environment.ObservablePropertyModel;
import org.tzi.use.plugins.bdi.trace.EnvironmentTraceabilityGraphBuilder;
import org.tzi.use.plugins.bdi.trace.TraceNodeKind;
import org.tzi.use.plugins.bdi.trace.TraceRelationKind;
import org.tzi.use.plugins.bdi.trace.TraceabilityGraph;
import org.tzi.use.plugins.bdi.use.UseModelSnapshot;
import org.tzi.use.plugins.bdi.use.UseUmlModelFacade;
import org.tzi.use.plugins.bdi.validation.IssueCertainty;
import org.tzi.use.plugins.bdi.validation.IssueSeverity;
import org.tzi.use.plugins.bdi.validation.environment.EnvironmentConsistencyValidator;
import org.tzi.use.plugins.bdi.validation.environment.EnvironmentFinding;
import org.tzi.use.plugins.bdi.validation.environment.EnvironmentValidationContext;

import cartago.Artifact;
import cartago.OPERATION;

class AuctionEnvironmentConsistencyTest {
    @Test
    void validStaticMappingHasNoConfirmedErrorAndPreservesDynamicUnknownTrace() throws Exception {
        EnvironmentValidationContext context = context(validMappings());
        List<EnvironmentFinding> findings = new EnvironmentConsistencyValidator().evaluate(context);

        assertFalse(findings.stream().anyMatch(finding ->
                finding.issue().severity() == IssueSeverity.ERROR
                        && finding.issue().certainty() == IssueCertainty.CONFIRMED));
        assertEquals(List.of("ENV-003"), findings.stream().map(finding -> finding.issue().ruleId()).toList());
        assertEquals(IssueCertainty.UNKNOWN, findings.get(0).issue().certainty());

        TraceabilityGraph graph = new EnvironmentTraceabilityGraphBuilder().build(context, findings);
        assertTrue(graph.nodes().stream().anyMatch(node -> node.kind() == TraceNodeKind.ENVIRONMENT_ARTIFACT));
        assertTrue(graph.nodes().stream().anyMatch(node -> node.kind() == TraceNodeKind.OBSERVABLE_PROPERTY));
        assertTrue(graph.edges().stream().anyMatch(edge -> edge.relation() == TraceRelationKind.ENVIRONMENT_TARGET));
        assertTrue(graph.edges().stream().allMatch(edge -> edge.certainty() == IssueCertainty.UNKNOWN));
    }

    @Test
    void detectsMissingOperationWrongArityAndWrongPropertyMutants() throws Exception {
        EnvironmentOperationMapping missing = new EnvironmentOperationMapping(
                "withdraw", 0, "main", "auction", "withdraw", "Auction::close()");
        EnvironmentOperationMapping wrongArity = new EnvironmentOperationMapping(
                "open", 1, "main", "auction", "open", "Auction::open()");
        EnvironmentPropertyMapping wrongProperty = new EnvironmentPropertyMapping(
                "auction_phase/1", "main", "auction", "phase", 1, "Auction::status");

        List<EnvironmentFinding> findings = new EnvironmentConsistencyValidator().evaluate(
                context(List.of(missing, wrongArity, wrongProperty)));

        assertEquals(List.of("ENV-001", "ENV-002", "ENV-003"), findings.stream()
                .map(finding -> finding.issue().ruleId()).toList());
        assertTrue(findings.stream().allMatch(finding -> finding.issue().certainty() == IssueCertainty.CONFIRMED));
        assertTrue(findings.stream().allMatch(finding -> !finding.issue().evidence().isEmpty()));
        TraceabilityGraph graph = new EnvironmentTraceabilityGraphBuilder().build(
                context(List.of(missing)), findings.stream().filter(value -> value.mapping().equals(missing)).toList());
        assertTrue(graph.nodes().stream().anyMatch(node -> node.kind() == TraceNodeKind.GAP));
        assertTrue(graph.edges().stream().anyMatch(edge -> edge.relation() == TraceRelationKind.MISSING_TARGET));
    }

    @Test
    void domainAndRulesExposeNoCartagoConcreteTypes() throws Exception {
        List<Class<?>> boundaryTypes = List.of(
                ArtifactModel.class,
                EnvironmentModel.class,
                EnvironmentValidationContext.class,
                EnvironmentConsistencyValidator.class);
        for (Class<?> type : boundaryTypes) {
            for (RecordComponent component : type.isRecord() ? type.getRecordComponents() : new RecordComponent[0]) {
                assertFalse(component.getGenericType().getTypeName().startsWith("cartago."));
            }
        }
        Path sourceRoot = repositoryRoot().resolve("use-bdi-plugin/src/main/java");
        try (var sources = Files.walk(sourceRoot)) {
            List<Path> cartagoImports = sources.filter(path -> path.toString().endsWith(".java"))
                    .filter(path -> {
                        try {
                            return Files.readString(path).contains("import cartago.");
                        } catch (java.io.IOException error) {
                            throw new java.io.UncheckedIOException(error);
                        }
                    })
                    .toList();
            assertEquals(List.of(sourceRoot.resolve(
                    "org/tzi/use/plugins/bdi/importer/CArtAgOArtifactAdapter.java")), cartagoImports);
        }
    }

    private static List<EnvironmentMapping> validMappings() {
        return List.of(
                new EnvironmentOperationMapping("open", 0, "main", "auction", "open", "Auction::open()"),
                new EnvironmentPropertyMapping(
                        "auction_status/1", "main", "auction", "status", 1, "Auction::status"));
    }

    private static EnvironmentValidationContext context(List<? extends EnvironmentMapping> mappings) throws Exception {
        UseModelSnapshot uml = new UseUmlModelFacade().snapshot(AuctionMappingFixtureTest.loadAuctionSystem());
        ArtifactModel artifact = new CArtAgOArtifactAdapter().normalize(
                "main", "auction", AuctionArtifact.class,
                List.of(new ObservablePropertyModel("status", 1, java.util.Optional.empty(),
                        List.of("Static descriptor declaration"))));
        return new EnvironmentValidationContext(new EnvironmentModel(List.of(artifact)), uml, List.copyOf(mappings));
    }

    private static Path repositoryRoot() {
        Path current = Path.of("").toAbsolutePath().normalize();
        while (current != null) {
            if (Files.isRegularFile(current.resolve("docs/project/16_PROJECT_COMPLETION_CHECKLIST.md"))) {
                return current;
            }
            current = current.getParent();
        }
        throw new IllegalStateException("Could not locate repository root");
    }

    static final class AuctionArtifact extends Artifact {
        @OPERATION(guard = "")
        public void open() {
        }

        @OPERATION(guard = "")
        public void close() {
        }
    }
}

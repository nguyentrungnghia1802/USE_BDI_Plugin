package org.tzi.use.plugins.bdi.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Instant;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.tzi.use.plugins.bdi.cli.HeadlessAnalysisRequest;
import org.tzi.use.plugins.bdi.cli.HeadlessAnalysisResult;
import org.tzi.use.plugins.bdi.cli.HeadlessAnalysisService;
import org.tzi.use.plugins.bdi.cli.HeadlessStateFixture;
import org.tzi.use.plugins.bdi.diagram.DiagramModel;
import org.tzi.use.plugins.bdi.diagram.DiagramNodeType;
import org.tzi.use.plugins.bdi.diagram.TraceabilityDiagramContributor;
import org.tzi.use.plugins.bdi.evaluation.EvaluationManifest;
import org.tzi.use.plugins.bdi.evaluation.EvaluationManifestCodec;
import org.tzi.use.plugins.bdi.trace.TraceabilityGraphBuilder;

/** Verifies visual evidence against the same reviewed Auction mutant corpus used by evaluation. */
class AuctionMutantDiagramTest {
    private static final Instant FIXED_TIME = Instant.parse("2026-08-11T00:00:00Z");
    private static final Set<String> VISUAL_RULES = Set.of("MAP-003", "SIG-001", "REF-001", "OCL-001");

    @TempDir
    Path tempDir;

    @Test
    void reviewedBaselineHasNoScopedMutantHighlight() throws Exception {
        EvaluationManifest manifest = manifest();
        EvaluationManifest.EvaluationCase baseline = caseById(manifest, "baseline");

        DiagramModel diagram = diagramFor(baseline);

        for (String ruleId : VISUAL_RULES) {
            String token = baseline.evidenceTokens().get(ruleId);
            assertTrue(diagram.nodes().stream()
                    .filter(node -> node.type() == DiagramNodeType.ISSUE)
                    .filter(node -> node.issueMarker().orElseThrow().ruleId().equals(ruleId))
                    .noneMatch(node -> token == null || matchesToken(node.label(),
                            node.issueMarker().orElseThrow().evidence(), token)),
                    () -> "Baseline violates the reviewed scope for " + ruleId + " / " + token);
        }
    }

    @Test
    void reviewedMutantsHighlightOnlyTheirRealSnapshotEvidencePaths() throws Exception {
        EvaluationManifest manifest = manifest();
        Map<String, EnumSet<DiagramNodeType>> expectedTypes = Map.of(
                "MAP-003", EnumSet.of(DiagramNodeType.TRACE_SOURCE, DiagramNodeType.TRACE_ELEMENT,
                        DiagramNodeType.TRACE_MAPPING, DiagramNodeType.TRACE_TARGET, DiagramNodeType.ISSUE),
                "SIG-001", EnumSet.of(DiagramNodeType.TRACE_SOURCE, DiagramNodeType.TRACE_ELEMENT,
                        DiagramNodeType.TRACE_MAPPING, DiagramNodeType.TRACE_TARGET, DiagramNodeType.ISSUE),
                "REF-001", EnumSet.of(DiagramNodeType.TRACE_SOURCE, DiagramNodeType.TRACE_ELEMENT,
                        DiagramNodeType.GAP, DiagramNodeType.ISSUE),
                "OCL-001", EnumSet.of(DiagramNodeType.TRACE_SOURCE, DiagramNodeType.TRACE_ELEMENT,
                        DiagramNodeType.TRACE_MAPPING, DiagramNodeType.TRACE_TARGET,
                        DiagramNodeType.OCL_CONSTRAINT, DiagramNodeType.ISSUE));

        for (EvaluationManifest.EvaluationCase evaluationCase : manifest.cases()) {
            if (evaluationCase.requiredRuleIds().isEmpty()) {
                continue;
            }
            String ruleId = evaluationCase.requiredRuleIds().get(0);
            DiagramModel diagram = diagramFor(evaluationCase);
            DiagramHighlightPath.Highlight highlight = DiagramHighlightPath.forIssue(diagram, ruleId);

            assertFalse(highlight.isEmpty(), evaluationCase.id());
            EnumSet<DiagramNodeType> highlightedTypes = diagram.nodes().stream()
                    .filter(node -> highlight.nodeIds().contains(node.id()))
                    .map(node -> node.type())
                    .collect(Collectors.toCollection(() -> EnumSet.noneOf(DiagramNodeType.class)));
            assertTrue(highlightedTypes.containsAll(expectedTypes.get(ruleId)),
                    () -> evaluationCase.id() + " highlighted " + highlightedTypes);
            assertTrue(diagram.edges().stream()
                    .filter(edge -> highlight.edgeIds().contains(edge.id()))
                    .allMatch(edge -> highlight.nodeIds().contains(edge.sourceNodeId())
                            && highlight.nodeIds().contains(edge.targetNodeId())));

            List<String> highlightedRules = diagram.nodes().stream()
                    .filter(node -> highlight.nodeIds().contains(node.id()))
                    .filter(node -> node.type() == DiagramNodeType.ISSUE)
                    .map(node -> node.issueMarker().orElseThrow().ruleId())
                    .distinct()
                    .toList();
            assertEquals(List.of(ruleId), highlightedRules,
                    () -> evaluationCase.id() + " fabricated unrelated issue paths");
            assertTrue(diagram.nodes().stream()
                    .filter(node -> node.type() == DiagramNodeType.ISSUE)
                    .filter(node -> node.issueMarker().orElseThrow().ruleId().equals(ruleId))
                    .allMatch(node -> node.issueMarker().orElseThrow().certainty()
                            == evaluationCase.expectedCertainties().get(ruleId)));
            Optional.ofNullable(evaluationCase.evidenceTokens().get(ruleId)).ifPresent(token -> assertTrue(
                    diagram.nodes().stream()
                            .filter(node -> highlight.nodeIds().contains(node.id()))
                            .anyMatch(node -> matchesToken(node.label(), node.issueMarker()
                                    .map(marker -> marker.evidence()).orElse(List.of()), token)),
                    () -> evaluationCase.id() + " highlight misses reviewed token " + token));
            assertFalse(diagram.nodes().stream().anyMatch(node -> node.label().contains(tempDir.toString())));
        }
    }

    private DiagramModel diagramFor(EvaluationManifest.EvaluationCase evaluationCase) throws Exception {
        Path root = repositoryRoot();
        Path workspace = tempDir.resolve(evaluationCase.id());
        Files.createDirectories(workspace);
        Map<String, Path> copied = new LinkedHashMap<>();
        Path useFile = copy(root, workspace, evaluationCase.useFile(), copied);
        List<Path> aslFiles = evaluationCase.aslFiles().stream()
                .map(path -> copyUnchecked(root, workspace, path, copied))
                .toList();
        Optional<Path> projectFile = evaluationCase.jcmFile()
                .map(path -> copyUnchecked(root, workspace, path, copied));
        Optional<Path> mappingFile = evaluationCase.mappingFile()
                .map(path -> copyUnchecked(root, workspace, path, copied));
        Optional<HeadlessStateFixture> stateFixture = evaluationCase.stateFixture().map(HeadlessStateFixture::new);
        HeadlessAnalysisResult analysis = new HeadlessAnalysisService().analyze(new HeadlessAnalysisRequest(
                useFile,
                aslFiles,
                projectFile,
                mappingFile,
                Optional.empty(),
                Optional.empty(),
                FIXED_TIME,
                Optional.of(evaluationCase.id()),
                stateFixture));
        return new TraceabilityDiagramContributor().build(
                new TraceabilityGraphBuilder().build(analysis.snapshot(), workspace));
    }

    private static Path copy(Path root, Path workspace, String relative, Map<String, Path> copied) throws Exception {
        Path source = root.resolve(relative).normalize();
        Path target = workspace.resolve(source.getFileName().toString());
        Path previous = copied.putIfAbsent(target.getFileName().toString(), source);
        if (previous != null && !previous.equals(source)) {
            throw new IllegalArgumentException("Fixture filename collision: " + previous + " and " + source);
        }
        Files.copy(source, target, StandardCopyOption.REPLACE_EXISTING);
        return target;
    }

    private static Path copyUnchecked(Path root, Path workspace, String relative, Map<String, Path> copied) {
        try {
            return copy(root, workspace, relative, copied);
        } catch (Exception error) {
            throw new IllegalStateException(error);
        }
    }

    private static EvaluationManifest manifest() throws Exception {
        return EvaluationManifestCodec.load(
                repositoryRoot().resolve("docs/project/evidence/auction-evaluation-manifest.json"));
    }

    private static EvaluationManifest.EvaluationCase caseById(EvaluationManifest manifest, String id) {
        return manifest.cases().stream().filter(item -> item.id().equals(id)).findFirst().orElseThrow();
    }

    private static boolean matchesToken(String label, List<String> evidence, String token) {
        return label.contains(token) || evidence.stream().anyMatch(item -> item.contains(token));
    }

    private static Path repositoryRoot() {
        Path current = Path.of("").toAbsolutePath().normalize();
        while (current != null) {
            if (Files.isRegularFile(current.resolve("docs/project/evidence/auction-evaluation-manifest.json"))) {
                return current;
            }
            current = current.getParent();
        }
        throw new IllegalStateException("Could not locate repository root");
    }
}

package org.tzi.use.plugins.bdi.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Files;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import javax.swing.SwingUtilities;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeModel;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.tzi.use.plugins.bdi.application.BdiImportService;
import org.tzi.use.plugins.bdi.application.BdiProjectConfiguration;
import org.tzi.use.plugins.bdi.application.BdiSourceTracker;
import org.tzi.use.plugins.bdi.model.mapping.MappingKind;
import org.tzi.use.plugins.bdi.report.ReportFormat;
import org.tzi.use.plugins.bdi.use.UmlClassRef;
import org.tzi.use.plugins.bdi.use.UmlObjectRef;
import org.tzi.use.plugins.bdi.use.UseModelSnapshot;
import org.tzi.use.plugins.bdi.use.UseSnapshotContext;
import org.tzi.use.plugins.bdi.use.UseSnapshotProvider;
import org.tzi.use.plugins.bdi.validation.BoundedEffectResult;
import org.tzi.use.plugins.bdi.validation.BoundedEffectStatus;
import org.tzi.use.plugins.bdi.validation.OclSnapshotResult;
import org.tzi.use.plugins.bdi.validation.OclSnapshotStatus;
import org.tzi.use.plugins.bdi.validation.RuleConfiguration;
import org.tzi.use.plugins.bdi.validation.SnapshotOclEvaluator;

class BdiExplorerViewTest {
    @Test
    void exportsTheExactCurrentAnalysisAndExposesTheGuiAction(@TempDir Path tempDir) throws Exception {
        BdiExplorerView view = new BdiExplorerView(new BdiImportService());
        assertEquals("Export Current Analysis...", view.exportButtonForTest().getText());
        assertFalse(view.exportButtonForTest().isEnabled());
        view.importFiles(List.of(fixture("fixtures/asl/valid/minimal.asl")));
        waitForImport(view);
        var current = view.currentAnalysisForTest().orElseThrow();
        Path output = tempDir.resolve("current.json");

        view.exportCurrentAnalysisForTest(output, ReportFormat.JSON, false);

        assertTrue(view.exportButtonForTest().isEnabled());
        assertSame(current, view.currentAnalysisForTest().orElseThrow(), "export must not rerun analysis");
        String report = Files.readString(output);
        assertTrue(report.contains("\"issuesCount\":" + current.issueCount()));
        assertTrue(report.contains(current.mappingHash()));
        assertTrue(report.contains("Configuration:"));
        assertEquals(current.issues().size(), current.issueCount());
    }

    @Test
    void buildsBdiTreeAndSourceDetailAfterBackgroundImport() throws Exception {
        BdiExplorerView view = new BdiExplorerView(new BdiImportService());
        view.importFiles(List.of(fixture("fixtures/asl/valid/minimal.asl")));
        waitForImport(view);

        TreeModel tree = view.treeForTest().getModel();
        DefaultMutableTreeNode root = (DefaultMutableTreeNode) tree.getRoot();
        assertEquals("AgentSpeak BDI model", root.toString());
        assertEquals(1, root.getChildCount());

        DefaultMutableTreeNode file = (DefaultMutableTreeNode) root.getChildAt(0);
        assertTrue(file.toString().contains("minimal.asl"));
        assertEquals(3, file.getChildCount());
        DefaultMutableTreeNode plans = (DefaultMutableTreeNode) file.getChildAt(2);
        DefaultMutableTreeNode plan = (DefaultMutableTreeNode) plans.getChildAt(0);
        DefaultMutableTreeNode step = (DefaultMutableTreeNode) plan.getChildAt(0);
        view.treeForTest().setSelectionPath(
                new javax.swing.tree.TreePath(step.getPath()));
        flushEdt();
        assertTrue(view.detailForTest().getText().contains("Source:"));
        assertTrue(view.detailForTest().getText().contains("Source excerpt:"));
        assertEquals(1, view.snapshotForTest().fileCount());
    }

    @Test
    void addsReadOnlyDiagramTabWithoutReplacingTheCurrentAnalysis() throws Exception {
        BdiExplorerView view = new BdiExplorerView(new BdiImportService());

        assertEquals(List.of("Explorer", "Diagram", "Problems", "Mapping"),
                java.util.stream.IntStream.range(0, view.tabsForTest().getTabCount())
                        .mapToObj(view.tabsForTest()::getTitleAt)
                        .toList());
        view.importFiles(List.of(fixture("fixtures/asl/valid/minimal.asl")));
        waitForImport(view);
        var current = view.currentAnalysisForTest().orElseThrow();
        long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(10);
        while (view.diagramForTest().modelForTest().nodes().isEmpty()
                && System.nanoTime() < deadline) {
            Thread.sleep(20);
        }
        flushEdt();

        assertTrue(!view.diagramForTest().modelForTest().nodes().isEmpty());
        SwingUtilities.invokeAndWait(() -> {
            view.diagramForTest().fitForTest().doClick();
            view.diagramForTest().resetForTest().doClick();
        });
        assertSame(current, view.currentAnalysisForTest().orElseThrow());
        assertEquals(1, view.snapshotForTest().fileCount());
    }

    @Test
    void selectingAProblemHighlightsItsDiagramEvidencePath() throws Exception {
        UseModelSnapshot useModel = new UseModelSnapshot(
                "fixture",
                "fixture.use",
                List.of(new UmlClassRef("Minimal", false, List.of())),
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                "fingerprint");
        BdiExplorerView view = new BdiExplorerView(
                new BdiImportService(),
                new BdiSourceTracker(),
                useModel);
        view.importFiles(List.of(fixture("fixtures/asl/valid/minimal.asl")));
        waitForImport(view);

        assertTrue(view.hasProblemCodeForTest("MAP-001"));
        waitForDiagram(view);
        selectProblem(view, "MAP-001");

        assertEquals(view.diagramForTest(), view.tabsForTest().getSelectedComponent());
        assertTrue(!view.diagramForTest().highlightedNodeIdsForTest().isEmpty());
        assertTrue(view.detailForTest().getText().contains("MAP-001"));
        assertTrue(view.detailForTest().getText().contains("Evidence path: highlighted"));
    }

    @Test
    void reimportsAllSelectedSourcesWhenOneSourceChanges(@TempDir Path tempDir) throws Exception {
        Path source = tempDir.resolve("changed.asl");
        Files.copy(fixture("fixtures/asl/valid/minimal.asl"), source);
        BdiExplorerView view = new BdiExplorerView(new BdiImportService());
        view.importFiles(List.of(source));
        waitForImport(view);
        assertTrue(view.reimportButtonForTest().isEnabled());

        Files.writeString(source, Files.readString(source) + "\n+changed.\n");
        assertTrue(view.reimportChangedFiles());
        long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(10);
        while (view.statusForTest().getText().startsWith("Re-importing") && System.nanoTime() < deadline) {
            Thread.sleep(25);
        }
        flushEdt();
        assertTrue(view.statusForTest().getText().startsWith("1 file(s)"));
    }

    @Test
    void populatesMappingSuggestionsFromImportedBdiAndUseSnapshot() throws Exception {
        UseModelSnapshot useModel = new UseModelSnapshot(
                "fixture",
                "fixture.use",
                List.of(new UmlClassRef("Minimal", false, List.of())),
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                "fingerprint");
        BdiExplorerView view = new BdiExplorerView(
                new BdiImportService(),
                new BdiSourceTracker(),
                useModel);
        view.importFiles(List.of(fixture("fixtures/asl/valid/minimal.asl")));
        waitForImport(view);

        assertTrue(view.mappingForTest().suggestionsForTest().getModel().getSize() > 0);
        assertTrue(view.mappingForTest().suggestionsForTest().getModel().getElementAt(0).toString()
                .contains("Minimal"));
    }

    @Test
    void refreshesProblemsWhenTheUserAppliesAnAgentMappingSuggestion() throws Exception {
        UseModelSnapshot useModel = new UseModelSnapshot(
                "fixture",
                "fixture.use",
                List.of(new UmlClassRef("Minimal", false, List.of())),
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                "fingerprint");
        BdiExplorerView view = new BdiExplorerView(
                new BdiImportService(),
                new BdiSourceTracker(),
                useModel);
        view.importFiles(List.of(fixture("fixtures/asl/valid/minimal.asl")));
        waitForImport(view);

        assertTrue(view.hasProblemCodeForTest("MAP-001"));
        for (int index = 0; index < view.mappingForTest().suggestionsForTest().getModel().getSize(); index++) {
            if (view.mappingForTest().suggestionsForTest().getModel().getElementAt(index).kind()
                    == MappingKind.AGENT_CLASS) {
                view.mappingForTest().suggestionsForTest().setSelectedIndex(index);
                break;
            }
        }
        view.mappingForTest().applySelectedSuggestionForTest();

        assertTrue(view.mappingForTest().document().bindings().stream()
                .anyMatch(binding -> binding.kind() == MappingKind.AGENT_CLASS));
        assertTrue(!view.hasProblemCodeForTest("MAP-001"));
    }

    @Test
    void appliesProjectRuleConfigurationAndShowsItsSource() throws Exception {
        UseModelSnapshot useModel = new UseModelSnapshot(
                "fixture",
                "fixture.use",
                List.of(new UmlClassRef("Minimal", false, List.of())),
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                "fingerprint");
        BdiProjectConfiguration configuration = new BdiProjectConfiguration(
                java.util.Optional.of(Path.of(".")),
                RuleConfiguration.of(List.of("ASL-001")),
                List.of(),
                true,
                false);
        BdiExplorerView view = new BdiExplorerView(
                new BdiImportService(),
                new BdiSourceTracker(),
                useModel,
                configuration);

        view.importFiles(List.of(fixture("fixtures/asl/valid/minimal.asl")));
        waitForImport(view);

        assertTrue(!view.hasProblemCodeForTest("MAP-001"));
        assertTrue(view.statusForTest().getText().contains("1 rule(s) [project]"));
        assertTrue(view.statusForTest().getText().contains("0 suppression(s) [default]"));
    }

    @Test
    void importsJacamoProjectAndShowsResolvedAgentsAndProjectDiagnostics() throws Exception {
        BdiExplorerView view = new BdiExplorerView(new BdiImportService());

        view.importProject(fixture("fixtures/casestudy/auction/auction.jcm"));
        waitForProjectImport(view);

        assertEquals("auction", view.projectForTest().orElseThrow().name());
        assertEquals(3, view.projectForTest().orElseThrow().agents().size());
        assertEquals(1, view.projectForTest().orElseThrow().organizations().size());
        assertTrue(view.projectForTest().orElseThrow().resources().stream()
                .filter(resource -> resource.kind()
                        == org.tzi.use.plugins.bdi.model.mas.MasResourceKind.ORGANIZATION)
                .allMatch(resource -> resource.status()
                        == org.tzi.use.plugins.bdi.model.mas.MasResourceStatus.NORMALIZED));
        assertTrue(view.statusForTest().getText().contains("3 agent instance(s)"));
        assertTrue(view.statusForTest().getText().contains("2 project diagnostic(s)"));
        DefaultMutableTreeNode root = (DefaultMutableTreeNode) view.treeForTest().getModel().getRoot();
        assertTrue(java.util.stream.IntStream.range(0, root.getChildCount())
                .mapToObj(root::getChildAt)
                .map(Object::toString)
                .anyMatch(value -> value.contains("JaCaMo diagnostics")));
        assertTrue(view.exportButtonForTest().isEnabled());
    }

    @Test
    void refreshesUseSnapshotAndProblemsWithoutReimportingAsl() throws Exception {
        UseModelSnapshot before = useSnapshot("a".repeat(64), true);
        UseModelSnapshot after = useSnapshot("b".repeat(64));
        AtomicInteger captures = new AtomicInteger();
        UseSnapshotProvider provider = () -> new UseSnapshotContext(
                captures.incrementAndGet() == 1 ? before : after,
                unknownEvaluator());
        BdiExplorerView view = new BdiExplorerView(
                new BdiImportService(),
                new BdiSourceTracker(),
                provider,
                BdiProjectConfiguration.defaults());
        view.importFiles(List.of(fixture("fixtures/asl/valid/minimal.asl")));
        waitForImport(view);
        for (int index = 0; index < view.mappingForTest().suggestionsForTest().getModel().getSize(); index++) {
            if (view.mappingForTest().suggestionsForTest().getModel().getElementAt(index).kind()
                    == MappingKind.AGENT_OBJECT) {
                view.mappingForTest().suggestionsForTest().setSelectedIndex(index);
                break;
            }
        }
        view.mappingForTest().applySelectedSuggestionForTest();
        assertTrue(view.mappingForTest().document().bindings().stream()
                .anyMatch(binding -> binding.kind() == MappingKind.AGENT_OBJECT));
        var imported = view.snapshotForTest();
        assertTrue(!view.hasProblemCodeForTest("MAP-003"));

        view.refreshUseSnapshot();
        flushEdt();

        assertSame(imported, view.snapshotForTest(), "refresh must not reparse AgentSpeak");
        assertEquals(after.fingerprint(), view.useModelForTest().orElseThrow().fingerprint());
        assertTrue(view.hasProblemCodeForTest("MAP-003"));
        assertTrue(view.statusForTest().getText().contains("USE snapshot refreshed"));
        assertTrue(view.statusForTest().getText().contains("[default]"));
        assertEquals(view.problemsForTest().problemCount(),
                view.currentAnalysisForTest().orElseThrow().issueCount());
        assertEquals(view.currentAnalysisForTest().orElseThrow().issues().size(),
                view.currentAnalysisForTest().orElseThrow().issueCount());
        assertTrue(view.currentAnalysisForTest().orElseThrow().issues().stream()
                .allMatch(issue -> view.hasProblemCodeForTest(issue.ruleId())));
        assertTrue(view.currentAnalysisForTest().orElseThrow().suppressions().isEmpty());
        assertEquals(3, captures.get(), "initial, before-analysis, and after-analysis captures expected");
    }

    @Test
    void reportsRefreshFailureInsteadOfTreatingItAsSuccess() throws Exception {
        AtomicInteger captures = new AtomicInteger();
        UseSnapshotProvider provider = () -> {
            if (captures.incrementAndGet() > 1) {
                throw new IllegalStateException("fixture capture failed");
            }
            return new UseSnapshotContext(useSnapshot("c".repeat(64)), unknownEvaluator());
        };
        BdiExplorerView view = new BdiExplorerView(
                new BdiImportService(),
                new BdiSourceTracker(),
                provider,
                BdiProjectConfiguration.defaults());

        view.refreshUseSnapshot();
        flushEdt();

        assertTrue(view.statusForTest().getText().contains("refresh failed"));
        assertTrue(view.statusForTest().getText().contains("fixture capture failed"));
        assertTrue(!view.statusForTest().getText().contains("refreshed ["));
    }

    @Test
    void discardsQueuedStaleRefreshBeforeReadingUse() throws Exception {
        AtomicInteger captures = new AtomicInteger();
        UseSnapshotProvider provider = () -> {
            captures.incrementAndGet();
            return new UseSnapshotContext(useSnapshot("d".repeat(64)), unknownEvaluator());
        };
        BdiExplorerView view = new BdiExplorerView(
                new BdiImportService(),
                new BdiSourceTracker(),
                provider,
                BdiProjectConfiguration.defaults());
        CountDownLatch edtBlocked = new CountDownLatch(1);
        CountDownLatch releaseEdt = new CountDownLatch(1);
        SwingUtilities.invokeLater(() -> {
            edtBlocked.countDown();
            try {
                releaseEdt.await(10, TimeUnit.SECONDS);
            } catch (InterruptedException error) {
                Thread.currentThread().interrupt();
            }
        });
        assertTrue(edtBlocked.await(10, TimeUnit.SECONDS));

        view.refreshUseSnapshot();
        view.refreshUseSnapshot();
        releaseEdt.countDown();
        flushEdt();

        assertEquals(3, captures.get(), "stale request must not capture USE state");
    }

    private static UseModelSnapshot useSnapshot(String fingerprint) {
        return useSnapshot(fingerprint, false);
    }

    private static UseModelSnapshot useSnapshot(String fingerprint, boolean includeObject) {
        return new UseModelSnapshot(
                "fixture",
                "fixture.use",
                List.of(new UmlClassRef("Minimal", false, List.of())),
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                includeObject
                        ? List.of(new UmlObjectRef("minimal1", "Minimal", true, Map.of()))
                        : List.of(),
                List.of(),
                fingerprint);
    }

    private static SnapshotOclEvaluator unknownEvaluator() {
        return new SnapshotOclEvaluator() {
            @Override
            public List<OclSnapshotResult> evaluatePreconditions(
                    org.tzi.use.plugins.bdi.use.UmlOperationRef operation,
                    String receiverObject,
                    List<org.tzi.use.plugins.bdi.model.ir.TermModel> arguments) {
                return List.of(new OclSnapshotResult(
                        operation.reference(), OclSnapshotStatus.UNKNOWN, List.of("fixture unknown")));
            }

            @Override
            public OclSnapshotResult evaluateExpression(String expression, String subject) {
                return new OclSnapshotResult(subject, OclSnapshotStatus.UNKNOWN, List.of("fixture unknown"));
            }

            @Override
            public BoundedEffectResult simulateSoilEffect(String source) {
                return new BoundedEffectResult(BoundedEffectStatus.UNKNOWN, List.of("fixture unknown"));
            }
        };
    }

    private static void waitForImport(BdiExplorerView view) throws Exception {
        long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(10);
        while (view.snapshotForTest().fileCount() == 0 && System.nanoTime() < deadline) {
            Thread.sleep(25);
        }
        flushEdt();
        assertEquals(1, view.snapshotForTest().fileCount());
    }

    private static void waitForDiagram(BdiExplorerView view) throws Exception {
        long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(10);
        while (view.diagramForTest().modelForTest().nodes().isEmpty()
                && System.nanoTime() < deadline) {
            Thread.sleep(25);
        }
        flushEdt();
        assertTrue(!view.diagramForTest().modelForTest().nodes().isEmpty());
    }

    private static void selectProblem(BdiExplorerView view, String code) throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            assertTrue(view.problemsForTest().selectProblem(code),
                    "Problem row not found: " + code);
        });
        flushEdt();
    }

    private static void waitForProjectImport(BdiExplorerView view) throws Exception {
        long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(10);
        while (view.projectForTest().isEmpty() && System.nanoTime() < deadline) {
            Thread.sleep(25);
        }
        flushEdt();
        assertTrue(view.projectForTest().isPresent());
    }

    private static void flushEdt() throws Exception {
        CountDownLatch flushed = new CountDownLatch(1);
        SwingUtilities.invokeLater(flushed::countDown);
        if (!flushed.await(10, TimeUnit.SECONDS)) {
            throw new AssertionError("EDT did not flush in time");
        }
    }

    private static Path fixture(String name) throws URISyntaxException {
        URL resource = BdiExplorerViewTest.class.getClassLoader().getResource(name);
        if (resource == null) {
            throw new IllegalStateException("Missing test fixture: " + name);
        }
        return Path.of(resource.toURI());
    }
}

package org.tzi.use.plugins.bdi.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Files;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import javax.swing.SwingUtilities;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeModel;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.tzi.use.plugins.bdi.application.BdiImportService;
import org.tzi.use.plugins.bdi.application.BdiSourceTracker;
import org.tzi.use.plugins.bdi.model.mapping.MappingKind;
import org.tzi.use.plugins.bdi.use.UmlClassRef;
import org.tzi.use.plugins.bdi.use.UseModelSnapshot;

class BdiExplorerViewTest {
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

    private static void waitForImport(BdiExplorerView view) throws Exception {
        long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(10);
        while (view.snapshotForTest().fileCount() == 0 && System.nanoTime() < deadline) {
            Thread.sleep(25);
        }
        flushEdt();
        assertEquals(1, view.snapshotForTest().fileCount());
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

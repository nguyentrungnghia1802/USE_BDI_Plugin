package org.tzi.use.plugins.bdi.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import javax.swing.SwingUtilities;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeModel;

import org.junit.jupiter.api.Test;
import org.tzi.use.plugins.bdi.application.BdiImportService;

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

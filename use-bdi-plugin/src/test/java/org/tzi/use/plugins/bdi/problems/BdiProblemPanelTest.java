package org.tzi.use.plugins.bdi.problems;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.Component;
import java.awt.Container;
import java.awt.Rectangle;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import javax.swing.SwingUtilities;

import org.junit.jupiter.api.Test;

class BdiProblemPanelTest {
    @Test
    void publishesSelectedProblemRowsToTheOwningView() throws Exception {
        BdiProblem problem = new BdiProblem(
                "MAP-003", BdiProblemSeverity.ERROR, Path.of("auction.asl"), 8, 2,
                "Mapped operation mismatch", "Mapping validation");
        BdiProblemPanel panel = new BdiProblemPanel();
        AtomicReference<BdiProblem> selected = new AtomicReference<>();
        panel.setProblemSelectionListener(selected::set);
        panel.setProblems(List.of(problem));

        SwingUtilities.invokeAndWait(() -> panel.tableForTest().setRowSelectionInterval(0, 0));

        assertEquals(problem, selected.get());
    }

    @Test
    void reportsFilteredAndTotalProblemCountsWithoutClippingTheFilter() throws Exception {
        BdiProblem first = new BdiProblem(
                "MAP-001", BdiProblemSeverity.ERROR, Path.of("family.asl"), 2, 1,
                "Missing class mapping", "Mapping validation");
        BdiProblem second = new BdiProblem(
                "SIG-001", BdiProblemSeverity.WARNING, Path.of("family.asl"), 6, 1,
                "Operation signature differs", "Signature validation");
        BdiProblemPanel panel = new BdiProblemPanel();

        SwingUtilities.invokeAndWait(() -> {
            panel.setSize(640, 520);
            layoutRecursively(panel);
            panel.setProblems(List.of(first, second));
            panel.filterForTest().setText("not-present");
        });

        Rectangle filterBounds = SwingUtilities.convertRectangle(
                panel.filterForTest().getParent(), panel.filterForTest().getBounds(), panel);
        assertTrue(filterBounds.width >= 400);
        assertTrue(filterBounds.x + filterBounds.width <= panel.getWidth());
        assertEquals("0 of 2 problems shown", panel.summaryForTest().getText());
    }

    private static void layoutRecursively(Container container) {
        container.doLayout();
        for (Component component : container.getComponents()) {
            if (component instanceof Container child) {
                layoutRecursively(child);
            }
        }
    }
}

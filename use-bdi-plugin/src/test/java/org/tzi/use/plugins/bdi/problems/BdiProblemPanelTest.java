package org.tzi.use.plugins.bdi.problems;

import static org.junit.jupiter.api.Assertions.assertEquals;

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
}

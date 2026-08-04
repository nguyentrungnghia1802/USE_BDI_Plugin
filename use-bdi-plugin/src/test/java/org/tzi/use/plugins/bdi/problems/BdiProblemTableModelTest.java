package org.tzi.use.plugins.bdi.problems;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.Test;

class BdiProblemTableModelTest {
    private static final Path SOURCE = Path.of("problem.asl").toAbsolutePath();

    @Test
    void filtersByTextAndSeverityAndGroupsDeterministically() {
        BdiProblemTableModel model = new BdiProblemTableModel();
        model.setProblems(List.of(
                new BdiProblem("ASL-002", BdiProblemSeverity.WARNING, SOURCE, 4, 2,
                        "Unsupported feature", "Unsupported feature"),
                new BdiProblem("ASL-001", BdiProblemSeverity.ERROR, SOURCE, 2, 1,
                        "Syntax error", "Import"),
                new BdiProblem("BDI-INDEX-001", BdiProblemSeverity.WARNING, SOURCE, 7, 1,
                        "Duplicate plan label", "Index validation")));

        assertEquals(3, model.getRowCount());
        model.setFilterText("syntax");
        assertEquals(1, model.getRowCount());
        assertEquals("ASL-001", model.getValueAt(0, 1));

        model.setFilterText("");
        model.setSeverity(BdiProblemSeverity.WARNING);
        assertEquals(2, model.getRowCount());
        model.setGrouping(BdiProblemGrouping.CODE);
        assertEquals("ASL-002", model.getValueAt(0, 1));
    }
}

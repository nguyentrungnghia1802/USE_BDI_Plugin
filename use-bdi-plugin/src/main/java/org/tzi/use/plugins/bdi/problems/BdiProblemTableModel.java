package org.tzi.use.plugins.bdi.problems;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.function.Predicate;

import javax.swing.table.AbstractTableModel;

/** Table model with deterministic filtering and grouping order. */
@SuppressWarnings("serial")
public final class BdiProblemTableModel extends AbstractTableModel {
    private static final String[] COLUMNS = {
            "Severity", "Code", "Source", "Location", "Message", "Group"
    };

    private List<BdiProblem> allProblems = List.of();
    private List<BdiProblem> visibleProblems = List.of();
    private String filterText = "";
    private BdiProblemSeverity severity;
    private BdiProblemGrouping grouping = BdiProblemGrouping.NONE;

    public void setProblems(List<BdiProblem> problems) {
        allProblems = List.copyOf(Objects.requireNonNull(problems, "problems"));
        refresh();
    }

    public void setFilterText(String text) {
        filterText = text == null ? "" : text.trim().toLowerCase(Locale.ROOT);
        refresh();
    }

    public void setSeverity(BdiProblemSeverity severity) {
        this.severity = severity;
        refresh();
    }

    public void setGrouping(BdiProblemGrouping grouping) {
        this.grouping = Objects.requireNonNull(grouping, "grouping");
        refresh();
    }

    public List<BdiProblem> allProblems() {
        return allProblems;
    }

    public List<BdiProblem> visibleProblems() {
        return visibleProblems;
    }

    @Override
    public int getRowCount() {
        return visibleProblems.size();
    }

    @Override
    public int getColumnCount() {
        return COLUMNS.length;
    }

    @Override
    public String getColumnName(int column) {
        return COLUMNS[column];
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        BdiProblem problem = visibleProblems.get(rowIndex);
        return switch (columnIndex) {
            case 0 -> problem.severity();
            case 1 -> problem.code();
            case 2 -> problem.source().toString();
            case 3 -> problem.location();
            case 4 -> problem.message();
            case 5 -> problem.group();
            default -> throw new IndexOutOfBoundsException("column=" + columnIndex);
        };
    }

    private void refresh() {
        Predicate<BdiProblem> matches = problem -> {
            if (severity != null && problem.severity() != severity) {
                return false;
            }
            if (filterText.isEmpty()) {
                return true;
            }
            String searchable = String.join(" ",
                    problem.code(),
                    problem.source().toString(),
                    problem.message(),
                    problem.group()).toLowerCase(Locale.ROOT);
            return searchable.contains(filterText);
        };

        List<BdiProblem> next = new ArrayList<>();
        for (BdiProblem problem : allProblems) {
            if (matches.test(problem)) {
                next.add(problem);
            }
        }
        next.sort(groupComparator());
        visibleProblems = List.copyOf(next);
        fireTableDataChanged();
    }

    private Comparator<BdiProblem> groupComparator() {
        Comparator<BdiProblem> base = Comparator
                .comparing(BdiProblem::source)
                .thenComparingInt(problem -> problem.line() == 0 ? Integer.MAX_VALUE : problem.line())
                .thenComparing(BdiProblem::code)
                .thenComparing(BdiProblem::message);
        Comparator<BdiProblem> groupFirst = switch (grouping) {
            case GROUP -> Comparator.comparing(BdiProblem::group);
            case SOURCE -> Comparator.comparing(problem -> problem.source().toString());
            case CODE -> Comparator.comparing(BdiProblem::code);
            case NONE -> Comparator.comparingInt(problem -> 0);
        };
        return groupFirst.thenComparing(base);
    }
}

package org.tzi.use.plugins.bdi.problems;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

/** Swing presentation of retained diagnostics with filter and grouping controls. */
@SuppressWarnings("serial")
public final class BdiProblemPanel extends JPanel {
    private final BdiProblemTableModel tableModel = new BdiProblemTableModel();
    private final JTable table = new JTable(tableModel);
    private final JTextField filter = new JTextField(24);
    private final JComboBox<BdiProblemSeverity> severity = new JComboBox<>();
    private final JComboBox<BdiProblemGrouping> grouping = new JComboBox<>(BdiProblemGrouping.values());
    private Consumer<BdiProblem> selectionListener = ignored -> {
    };

    public BdiProblemPanel() {
        super(new BorderLayout(6, 6));
        setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));

        severity.addItem(null);
        severity.addItem(BdiProblemSeverity.ERROR);
        severity.addItem(BdiProblemSeverity.WARNING);
        severity.addItem(BdiProblemSeverity.INFO);

        JPanel controls = new JPanel(new FlowLayout(FlowLayout.LEADING, 6, 0));
        controls.add(new JLabel("Filter:"));
        controls.add(filter);
        controls.add(new JLabel("Severity:"));
        controls.add(severity);
        controls.add(new JLabel("Group by:"));
        controls.add(grouping);
        add(controls, BorderLayout.NORTH);

        table.setAutoCreateRowSorter(true);
        table.setFillsViewportHeight(true);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
        add(new JScrollPane(table), BorderLayout.CENTER);
        table.getSelectionModel().addListSelectionListener(event -> {
            if (event.getValueIsAdjusting()) {
                return;
            }
            int viewRow = table.getSelectedRow();
            if (viewRow < 0) {
                return;
            }
            int modelRow = table.convertRowIndexToModel(viewRow);
            if (modelRow < tableModel.visibleProblems().size()) {
                selectionListener.accept(tableModel.visibleProblems().get(modelRow));
            }
        });

        filter.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent event) {
                applyFilter();
            }

            @Override
            public void removeUpdate(DocumentEvent event) {
                applyFilter();
            }

            @Override
            public void changedUpdate(DocumentEvent event) {
                applyFilter();
            }
        });
        severity.addActionListener(event -> tableModel.setSeverity((BdiProblemSeverity) severity.getSelectedItem()));
        grouping.addActionListener(event -> tableModel.setGrouping((BdiProblemGrouping) grouping.getSelectedItem()));
    }

    public void setProblems(List<BdiProblem> problems) {
        tableModel.setProblems(problems);
    }

    public int problemCount() {
        return tableModel.allProblems().size();
    }

    public boolean hasProblemCode(String code) {
        return tableModel.allProblems().stream()
                .anyMatch(problem -> problem.code().equals(code));
    }

    /** Receives user row selections so the owning view can navigate to evidence. */
    public void setProblemSelectionListener(Consumer<BdiProblem> listener) {
        selectionListener = Objects.requireNonNull(listener, "listener");
    }

    /** Selects a visible problem by stable rule code for integrations and UI navigation. */
    public boolean selectProblem(String code) {
        Objects.requireNonNull(code, "code");
        if (!javax.swing.SwingUtilities.isEventDispatchThread()) {
            javax.swing.SwingUtilities.invokeLater(() -> selectProblem(code));
            return false;
        }
        for (int row = 0; row < table.getRowCount(); row++) {
            if (code.equals(table.getValueAt(row, 1))) {
                table.setRowSelectionInterval(row, row);
                return true;
            }
        }
        return false;
    }

    JTable tableForTest() {
        return table;
    }

    JTextField filterForTest() {
        return filter;
    }

    JComboBox<BdiProblemSeverity> severityForTest() {
        return severity;
    }

    JComboBox<BdiProblemGrouping> groupingForTest() {
        return grouping;
    }

    BdiProblemTableModel tableModelForTest() {
        return tableModel;
    }

    private void applyFilter() {
        tableModel.setFilterText(filter.getText());
    }
}

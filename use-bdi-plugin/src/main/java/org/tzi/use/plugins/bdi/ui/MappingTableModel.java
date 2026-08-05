package org.tzi.use.plugins.bdi.ui;

import java.util.List;
import java.util.Objects;

import javax.swing.table.AbstractTableModel;

import org.tzi.use.plugins.bdi.model.mapping.MappingBinding;

@SuppressWarnings("serial")
final class MappingTableModel extends AbstractTableModel {
    private static final String[] COLUMNS = {"Kind", "Source", "Target", "Expression", "Evidence"};
    private List<MappingBinding> bindings = List.of();

    void setBindings(List<MappingBinding> bindings) {
        this.bindings = List.copyOf(Objects.requireNonNull(bindings, "bindings"));
        fireTableDataChanged();
    }

    List<MappingBinding> bindings() {
        return bindings;
    }

    @Override
    public int getRowCount() {
        return bindings.size();
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
        MappingBinding binding = bindings.get(rowIndex);
        return switch (columnIndex) {
            case 0 -> binding.kind();
            case 1 -> binding.source();
            case 2 -> binding.target();
            case 3 -> binding.expression().orElse("");
            case 4 -> String.join("; ", binding.evidence());
            default -> throw new IndexOutOfBoundsException("column=" + columnIndex);
        };
    }
}

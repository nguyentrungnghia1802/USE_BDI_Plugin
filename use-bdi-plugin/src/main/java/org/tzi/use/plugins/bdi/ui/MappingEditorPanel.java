package org.tzi.use.plugins.bdi.ui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;

import org.tzi.use.plugins.bdi.model.mapping.MappingBinding;
import org.tzi.use.plugins.bdi.model.mapping.MappingDocument;
import org.tzi.use.plugins.bdi.model.mapping.MappingKind;
import org.tzi.use.plugins.bdi.model.mapping.MappingSuggestion;
import org.tzi.use.plugins.bdi.persistence.MappingFileRepository;

/** Small manual confirmation editor for mapping candidates and persisted bindings. */
@SuppressWarnings("serial")
public final class MappingEditorPanel extends JPanel {
    private final MappingFileRepository repository;
    private final Optional<Path> projectRoot;
    private final MappingTableModel tableModel = new MappingTableModel();
    private final JTable table = new JTable(tableModel);
    private final DefaultListModel<MappingSuggestion> suggestionModel = new DefaultListModel<>();
    private final JList<MappingSuggestion> suggestions = new JList<>(suggestionModel);
    private final JComboBox<MappingKind> kind = new JComboBox<>(MappingKind.values());
    private final JTextField source = new JTextField(24);
    private final JTextField target = new JTextField(24);
    private final JLabel status = new JLabel("No mapping document loaded");
    private final JSplitPane body;
    private MappingDocument document = MappingDocument.empty("unknown");
    private Consumer<MappingDocument> documentChangeListener = ignored -> {
    };

    public MappingEditorPanel() {
        this(new MappingFileRepository(), Optional.empty());
    }

    MappingEditorPanel(MappingFileRepository repository) {
        this(repository, Optional.empty());
    }

    MappingEditorPanel(MappingFileRepository repository, Optional<Path> projectRoot) {
        super(new BorderLayout(6, 6));
        this.repository = Objects.requireNonNull(repository, "repository");
        this.projectRoot = Objects.requireNonNull(projectRoot, "projectRoot")
                .map(path -> path.toAbsolutePath().normalize());
        setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));

        JPanel editControls = new JPanel();
        editControls.setLayout(new BoxLayout(editControls, BoxLayout.Y_AXIS));
        JPanel kindRow = row();
        kindRow.add(new JLabel("Kind:"));
        kindRow.add(kind);
        JButton upsert = new JButton("Add / update");
        upsert.addActionListener(event -> upsertFromFields());
        kindRow.add(upsert);
        editControls.add(kindRow);
        editControls.add(fieldRow("Source:", source));
        editControls.add(fieldRow("Target:", target));
        add(editControls, BorderLayout.NORTH);

        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setAutoCreateRowSorter(true);
        table.setFillsViewportHeight(true);
        table.setPreferredScrollableViewportSize(new Dimension(620, 220));
        table.getSelectionModel().addListSelectionListener(event -> populateFieldsFromSelection());

        suggestions.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        suggestions.setVisibleRowCount(12);
        suggestions.setToolTipText("Select a candidate, then confirm it explicitly");
        JPanel suggestionPanel = new JPanel(new BorderLayout(6, 6));
        suggestionPanel.setBorder(BorderFactory.createTitledBorder("Suggestions (candidates only)"));
        suggestionPanel.add(new JScrollPane(suggestions), BorderLayout.CENTER);
        JButton apply = new JButton("Apply selected suggestion");
        apply.addActionListener(event -> applySelectedSuggestion());
        suggestionPanel.add(apply, BorderLayout.SOUTH);

        body = new JSplitPane(
                JSplitPane.VERTICAL_SPLIT,
                new JScrollPane(table),
                suggestionPanel);
        body.setResizeWeight(0.58);
        body.setContinuousLayout(true);
        body.setOneTouchExpandable(true);
        add(body, BorderLayout.CENTER);

        JPanel footer = new JPanel();
        footer.setLayout(new BoxLayout(footer, BoxLayout.Y_AXIS));
        JPanel actions = row();
        JButton remove = new JButton("Remove selected");
        remove.addActionListener(event -> removeSelected());
        JButton save = new JButton("Save...");
        save.addActionListener(event -> chooseSave());
        JButton load = new JButton("Load...");
        load.addActionListener(event -> chooseLoad());
        actions.add(remove);
        actions.add(save);
        actions.add(load);
        footer.add(actions);
        status.setBorder(BorderFactory.createEmptyBorder(2, 6, 2, 4));
        status.setAlignmentX(LEFT_ALIGNMENT);
        footer.add(status);
        add(footer, BorderLayout.SOUTH);
    }

    public void setDocument(MappingDocument document) {
        setDocument(document, true);
    }

    void setDocumentWithoutNotification(MappingDocument document) {
        setDocument(document, false);
    }

    private void setDocument(MappingDocument document, boolean notify) {
        this.document = Objects.requireNonNull(document, "document");
        tableModel.setBindings(document.bindings());
        status.setText(document.bindings().size() + " binding(s)");
        if (notify) {
            documentChangeListener.accept(document);
        }
    }

    public void setDocumentChangeListener(Consumer<MappingDocument> listener) {
        documentChangeListener = Objects.requireNonNull(listener, "listener");
    }

    public MappingDocument document() {
        return document;
    }

    public void setSuggestions(List<MappingSuggestion> values) {
        suggestionModel.clear();
        Objects.requireNonNull(values, "values").forEach(suggestionModel::addElement);
    }

    public void save(Path file) throws IOException {
        repository.save(file, document, requiredProjectRoot(file));
        status.setText("Saved " + document.bindings().size() + " binding(s)");
    }

    public void load(Path file) throws IOException {
        setDocument(repository.load(file, requiredProjectRoot(file)));
        status.setText("Loaded " + document.bindings().size() + " binding(s)");
    }

    private Path requiredProjectRoot(Path file) throws IOException {
        return projectRoot.orElseThrow(() -> new IOException(
                "Mapping persistence requires an active file-backed USE project: " + file));
    }

    JTable tableForTest() {
        return table;
    }

    JList<MappingSuggestion> suggestionsForTest() {
        return suggestions;
    }

    JLabel statusForTest() {
        return status;
    }

    JTextField sourceForTest() {
        return source;
    }

    JTextField targetForTest() {
        return target;
    }

    JSplitPane bodyForTest() {
        return body;
    }

    void applySelectedSuggestionForTest() {
        applySelectedSuggestion();
    }

    private void upsertFromFields() {
        try {
            MappingBinding binding = new MappingBinding(
                    (MappingKind) kind.getSelectedItem(),
                    source.getText().trim(),
                    target.getText().trim());
            setDocument(document.upsert(binding));
        } catch (IllegalArgumentException error) {
            status.setText("Mapping error: " + error.getMessage());
        }
    }

    private void removeSelected() {
        int row = table.getSelectedRow();
        if (row < 0) {
            return;
        }
        int modelRow = table.convertRowIndexToModel(row);
        MappingBinding binding = tableModel.bindings().get(modelRow);
        setDocument(document.remove(binding.kind(), binding.source()));
    }

    private void applySelectedSuggestion() {
        MappingSuggestion suggestion = suggestions.getSelectedValue();
        if (suggestion == null) {
            status.setText("Select a suggestion first");
            return;
        }
        setDocument(document.upsert(suggestion.toBinding()));
        status.setText("Applied " + suggestion.kind());
    }

    private void populateFieldsFromSelection() {
        int row = table.getSelectedRow();
        if (row < 0) {
            return;
        }
        MappingBinding binding = tableModel.bindings().get(table.convertRowIndexToModel(row));
        kind.setSelectedItem(binding.kind());
        source.setText(binding.source());
        target.setText(binding.target());
    }

    private void chooseSave() {
        JFileChooser chooser = BdiFileChooserSupport.create();
        chooser.setDialogTitle("Save BDI mapping");
        chooser.setSelectedFile(new java.io.File("mapping.bdimap.json"));
        if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            try {
                save(chooser.getSelectedFile().toPath());
            } catch (IOException error) {
                status.setText("Save failed: " + error.getMessage());
            }
        }
    }

    private void chooseLoad() {
        JFileChooser chooser = BdiFileChooserSupport.create();
        chooser.setDialogTitle("Load BDI mapping");
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            try {
                load(chooser.getSelectedFile().toPath());
            } catch (IOException error) {
                status.setText("Load failed: " + error.getMessage());
            }
        }
    }

    private static JPanel row() {
        JPanel row = new JPanel(new FlowLayout(FlowLayout.LEADING, 6, 2));
        row.setAlignmentX(LEFT_ALIGNMENT);
        return row;
    }

    private static JPanel fieldRow(String label, JTextField field) {
        JPanel row = new JPanel(new BorderLayout(6, 0));
        JLabel fieldLabel = new JLabel(label);
        fieldLabel.setPreferredSize(new Dimension(56, fieldLabel.getPreferredSize().height));
        row.add(fieldLabel, BorderLayout.WEST);
        row.add(field, BorderLayout.CENTER);
        row.setBorder(BorderFactory.createEmptyBorder(1, 6, 1, 6));
        row.setAlignmentX(LEFT_ALIGNMENT);
        return row;
    }
}

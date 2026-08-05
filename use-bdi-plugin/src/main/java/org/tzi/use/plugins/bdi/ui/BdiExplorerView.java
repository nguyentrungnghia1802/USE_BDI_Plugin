package org.tzi.use.plugins.bdi.ui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JTabbedPane;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeSelectionModel;

import org.tzi.use.gui.views.View;
import org.tzi.use.plugins.bdi.ImportBdiAction;
import org.tzi.use.plugins.bdi.application.BdiImportService;
import org.tzi.use.plugins.bdi.application.BdiImportSnapshot;
import org.tzi.use.plugins.bdi.application.BdiSourceTracker;
import org.tzi.use.plugins.bdi.index.BdiIndex;
import org.tzi.use.plugins.bdi.importer.AslDiagnostic;
import org.tzi.use.plugins.bdi.model.ir.AchieveGoalStepModel;
import org.tzi.use.plugins.bdi.model.ir.ActionStepModel;
import org.tzi.use.plugins.bdi.model.ir.AgentModel;
import org.tzi.use.plugins.bdi.model.ir.BeliefModel;
import org.tzi.use.plugins.bdi.model.ir.BeliefUpdateStepModel;
import org.tzi.use.plugins.bdi.model.ir.ConstraintStepModel;
import org.tzi.use.plugins.bdi.model.ir.ContextExpr;
import org.tzi.use.plugins.bdi.model.ir.GoalModel;
import org.tzi.use.plugins.bdi.model.ir.InternalActionStepModel;
import org.tzi.use.plugins.bdi.model.ir.PlanModel;
import org.tzi.use.plugins.bdi.model.ir.PlanStepModel;
import org.tzi.use.plugins.bdi.model.ir.SourceSpan;
import org.tzi.use.plugins.bdi.model.ir.TestStepModel;
import org.tzi.use.plugins.bdi.model.mapping.MappingSuggestionService;
import org.tzi.use.plugins.bdi.model.mapping.MappingDocument;
import org.tzi.use.plugins.bdi.problems.BdiProblemCollector;
import org.tzi.use.plugins.bdi.problems.BdiProblemPanel;
import org.tzi.use.plugins.bdi.use.UseModelSnapshot;
import org.tzi.use.plugins.bdi.use.UseSnapshotOclEvaluator;
import org.tzi.use.plugins.bdi.use.UseUmlModelFacade;
import org.tzi.use.plugins.bdi.validation.SnapshotOclEvaluator;
import org.tzi.use.plugins.bdi.validation.ValidationContext;
import org.tzi.use.plugins.bdi.validation.ValidationOrchestrator;
import org.tzi.use.uml.sys.MSystem;

/** Minimal BDI tree and source detail view for the first explorer slice. */
@SuppressWarnings("serial")
public final class BdiExplorerView extends JPanel implements View {
    private final BdiImportService importService;
    private final JTree tree;
    private final JTextArea detail;
    private final JLabel status;
    private final BdiProblemPanel problems;
    private final MappingEditorPanel mapping;
    private final MappingSuggestionService mappingSuggestionService;
    private final ValidationOrchestrator validationOrchestrator;
    private final Optional<UseModelSnapshot> useModel;
    private final Optional<SnapshotOclEvaluator> oclEvaluator;
    private final JButton reimportButton;
    private final BdiSourceTracker sourceTracker;
    private BdiImportSnapshot snapshot;
    private BdiImportWorker worker;
    private long importGeneration;

    public BdiExplorerView() {
        this(new BdiImportService(), new BdiSourceTracker(), Optional.empty(), Optional.empty());
    }

    public BdiExplorerView(UseModelSnapshot useModel) {
        this(new BdiImportService(), new BdiSourceTracker(), Optional.of(Objects.requireNonNull(useModel, "useModel")), Optional.empty());
    }

    public BdiExplorerView(MSystem system) {
        this(
                new BdiImportService(),
                new BdiSourceTracker(),
                Optional.of(new UseUmlModelFacade().snapshot(Objects.requireNonNull(system, "system"))),
                Optional.of(new UseSnapshotOclEvaluator(system)));
    }

    BdiExplorerView(BdiImportService importService) {
        this(importService, new BdiSourceTracker(), Optional.empty(), Optional.empty());
    }

    BdiExplorerView(BdiImportService importService, BdiSourceTracker sourceTracker) {
        this(importService, sourceTracker, Optional.empty(), Optional.empty());
    }

    BdiExplorerView(
            BdiImportService importService,
            BdiSourceTracker sourceTracker,
            UseModelSnapshot useModel) {
        this(importService, sourceTracker, Optional.of(Objects.requireNonNull(useModel, "useModel")), Optional.empty());
    }

    private BdiExplorerView(
            BdiImportService importService,
            BdiSourceTracker sourceTracker,
            Optional<UseModelSnapshot> useModel,
            Optional<SnapshotOclEvaluator> oclEvaluator) {
        super(new BorderLayout(6, 6));
        this.importService = Objects.requireNonNull(importService, "importService");
        this.sourceTracker = Objects.requireNonNull(sourceTracker, "sourceTracker");
        this.useModel = Objects.requireNonNull(useModel, "useModel");
        this.oclEvaluator = Objects.requireNonNull(oclEvaluator, "oclEvaluator");
        this.mappingSuggestionService = new MappingSuggestionService();
        this.validationOrchestrator = new ValidationOrchestrator();
        this.snapshot = new BdiImportSnapshot(List.of(), List.of(), BdiIndex.empty());

        JButton importButton = new JButton("Import .asl...");
        importButton.setToolTipText("Choose AgentSpeak source files");
        importButton.addActionListener(event -> ImportBdiAction.chooseAndImport(this));
        reimportButton = new JButton("Re-import changed");
        reimportButton.setToolTipText("Re-import all selected files after a source change");
        reimportButton.setEnabled(false);
        reimportButton.addActionListener(event -> reimportChangedFiles());
        status = new JLabel("No AgentSpeak source imported");
        JPanel buttons = new JPanel();
        buttons.add(importButton);
        buttons.add(reimportButton);
        JPanel toolbar = new JPanel(new BorderLayout(6, 0));
        toolbar.add(buttons, BorderLayout.WEST);
        toolbar.add(status, BorderLayout.CENTER);
        add(toolbar, BorderLayout.NORTH);

        tree = new JTree(createTree(snapshot));
        tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        tree.addTreeSelectionListener(this::showSelection);
        tree.setRootVisible(true);

        detail = new JTextArea();
        detail.setEditable(false);
        detail.setLineWrap(false);
        detail.setFont(new java.awt.Font(java.awt.Font.MONOSPACED, java.awt.Font.PLAIN, 12));
        detail.setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));

        JSplitPane split = new JSplitPane(
                JSplitPane.HORIZONTAL_SPLIT,
                new JScrollPane(tree),
                new JScrollPane(detail));
        split.setResizeWeight(0.42);
        split.setPreferredSize(new Dimension(900, 520));
        problems = new BdiProblemPanel();
        mapping = new MappingEditorPanel();
        mapping.setDocumentChangeListener(ignored -> refreshProblems());
        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Explorer", split);
        tabs.addTab("Problems", problems);
        tabs.addTab("Mapping", mapping);
        add(tabs, BorderLayout.CENTER);
        detail.setText("Select a BDI node to inspect its details and source span.");
    }

    public void importFiles(List<Path> sources) {
        List<Path> selectedSources = List.copyOf(Objects.requireNonNull(sources, "sources"));
        sourceTracker.track(selectedSources);
        startImport(selectedSources, "Importing AgentSpeak...");
    }

    public boolean reimportChangedFiles() {
        List<Path> changed = sourceTracker.changedSources();
        if (changed.isEmpty()) {
            status.setText("No changed AgentSpeak source files");
            return false;
        }
        startImport(sourceTracker.sources(), "Re-importing " + changed.size() + " changed file(s)...");
        return true;
    }

    private void startImport(List<Path> sources, String message) {
        if (worker != null && !worker.isDone()) {
            worker.cancel(true);
        }
        long generation = ++importGeneration;
        status.setText(message);
        worker = new BdiImportWorker(
                importService,
                sources,
                imported -> {
                    if (generation == importGeneration) {
                        applySnapshot(imported);
                    }
                },
                failure -> {
                    if (generation == importGeneration) {
                        showFailure(failure);
                    }
                });
        worker.execute();
    }

    @Override
    public void detachModel() {
        importGeneration++;
        if (worker != null && !worker.isDone()) {
            worker.cancel(true);
        }
    }

    JTree treeForTest() {
        return tree;
    }

    JTextArea detailForTest() {
        return detail;
    }

    BdiImportSnapshot snapshotForTest() {
        return snapshot;
    }

    BdiProblemPanel problemsForTest() {
        return problems;
    }

    boolean hasProblemCodeForTest(String code) {
        return problems.hasProblemCode(code);
    }

    MappingEditorPanel mappingForTest() {
        return mapping;
    }

    JButton reimportButtonForTest() {
        return reimportButton;
    }

    JLabel statusForTest() {
        return status;
    }

    private void applySnapshot(BdiImportSnapshot imported) {
        Runnable update = () -> {
            snapshot = imported;
            sourceTracker.markImported();
            useModel.filter(model -> mapping.document().useFingerprint().equals("unknown"))
                    .ifPresent(model -> mapping.setDocument(MappingDocument.empty(model.fingerprint())));
            mapping.setSuggestions(useModel
                    .map(model -> mappingSuggestionService.suggest(imported.models(), imported.index(), model))
                    .orElse(List.of()));
            refreshProblems();
            reimportButton.setEnabled(!sourceTracker.sources().isEmpty());
            tree.setModel(new DefaultTreeModel(createTree(imported)));
            String message = imported.fileCount() + " file(s), "
                    + imported.index().allPredicateReferences().size() + " predicate reference(s)";
            int problemCount = problems.problemCount();
            if (problemCount > 0) {
                message += ", " + problemCount + " problem(s)";
            }
            status.setText(message);
            detail.setText("Select a BDI node to inspect its details and source span.");
        };
        if (SwingUtilities.isEventDispatchThread()) {
            update.run();
        } else {
            SwingUtilities.invokeLater(update);
        }
    }

    private void showFailure(Throwable failure) {
        status.setText("Import failed: " + failure.getMessage());
    }

    private void refreshProblems() {
        problems.setProblems(BdiProblemCollector.collectConsistencyIssues(
                validationOrchestrator.evaluate(ValidationContext.from(snapshot, mapping.document(), useModel, oclEvaluator))));
    }

    private DefaultMutableTreeNode createTree(BdiImportSnapshot imported) {
        DefaultMutableTreeNode root = node(new BdiTreeEntry(
                "AgentSpeak BDI model",
                "Metamodel version: " + imported.index().metamodelVersion(),
                Optional.empty()));
        for (AgentModel model : imported.models()) {
            root.add(createAgentNode(model));
        }
        if (!imported.diagnostics().isEmpty()) {
            DefaultMutableTreeNode diagnostics = node(new BdiTreeEntry(
                    "Diagnostics (" + imported.diagnostics().size() + ")",
                    "Import diagnostics retained for files that could not be materialized.",
                    Optional.empty()));
            imported.diagnostics().forEach(diagnostic -> diagnostics.add(node(diagnosticEntry(diagnostic))));
            root.add(diagnostics);
        }
        return root;
    }

    private DefaultMutableTreeNode createAgentNode(AgentModel model) {
        DefaultMutableTreeNode file = node(new BdiTreeEntry(
                model.source().getFileName().toString(),
                "Source: " + model.source() + "\nParser: " + model.parserVersion(),
                Optional.of(SourceSpan.unknown(model.source()))));
        DefaultMutableTreeNode beliefs = node(new BdiTreeEntry(
                "Beliefs (" + model.beliefs().size() + ")",
                "Initial beliefs",
                Optional.empty()));
        for (BeliefModel belief : model.beliefs()) {
            beliefs.add(node(new BdiTreeEntry(
                    "Belief: " + belief.literal().render(),
                    "Initial belief: " + belief.literal().render(),
                    Optional.of(belief.sourceSpan()))));
        }
        file.add(beliefs);

        DefaultMutableTreeNode goals = node(new BdiTreeEntry(
                "Goals (" + model.goals().size() + ")",
                "Initial goals",
                Optional.empty()));
        for (GoalModel goal : model.goals()) {
            goals.add(node(new BdiTreeEntry(
                    "Goal: " + goal.literal().render(),
                    "Initial goal: " + goal.literal().render(),
                    Optional.of(goal.sourceSpan()))));
        }
        file.add(goals);

        DefaultMutableTreeNode plans = node(new BdiTreeEntry(
                "Plans (" + model.plans().size() + ")",
                "Normalized plans and ordered body steps",
                Optional.empty()));
        for (PlanModel plan : model.plans()) {
            DefaultMutableTreeNode planNode = node(new BdiTreeEntry(
                    planLabel(plan),
                    "Trigger: " + plan.trigger().term().render()
                            + "\nContext: " + plan.context().map(ContextExpr::toString).orElse("<none>"),
                    Optional.of(plan.sourceSpan())));
            for (int index = 0; index < plan.steps().size(); index++) {
                planNode.add(node(stepEntry(index + 1, plan.steps().get(index))));
            }
            plans.add(planNode);
        }
        file.add(plans);
        return file;
    }

    private static String planLabel(PlanModel plan) {
        String label = plan.label().isBlank() ? "<unlabeled>" : plan.label();
        return "Plan: " + label + " [" + plan.trigger().term().render() + "]";
    }

    private static BdiTreeEntry stepEntry(int index, PlanStepModel step) {
        String description;
        if (step instanceof ActionStepModel action) {
            description = "External action: " + action.action().render();
        } else if (step instanceof InternalActionStepModel action) {
            description = "Internal action: " + action.action().render();
        } else if (step instanceof AchieveGoalStepModel goal) {
            description = "Achieve goal: " + goal.goal().render();
        } else if (step instanceof TestStepModel) {
            description = "Test condition";
        } else if (step instanceof ConstraintStepModel) {
            description = "Constraint condition";
        } else if (step instanceof BeliefUpdateStepModel update) {
            description = "Belief update: " + update.operator() + " " + update.belief().render();
        } else {
            description = "Unsupported step";
        }
        return new BdiTreeEntry(index + ". " + description, description, Optional.of(step.sourceSpan()));
    }

    private static BdiTreeEntry diagnosticEntry(AslDiagnostic diagnostic) {
        SourceSpan span = diagnostic.hasSourcePosition()
                ? new SourceSpan(
                        diagnostic.source(),
                        diagnostic.line(),
                        SourceSpan.UNKNOWN_POSITION,
                        diagnostic.line(),
                        SourceSpan.UNKNOWN_POSITION)
                : SourceSpan.unknown(diagnostic.source());
        return new BdiTreeEntry(
                diagnostic.code() + ": " + diagnostic.source().getFileName(),
                diagnostic.message(),
                Optional.of(span));
    }

    private void showSelection(TreeSelectionEvent event) {
        if (event.getPath() == null) {
            return;
        }
        Object selected = event.getPath().getLastPathComponent();
        if (!(selected instanceof DefaultMutableTreeNode treeNode)
                || !(treeNode.getUserObject() instanceof BdiTreeEntry entry)) {
            return;
        }
        detail.setText(renderDetail(entry));
        detail.setCaretPosition(0);
    }

    private static String renderDetail(BdiTreeEntry entry) {
        StringBuilder text = new StringBuilder(entry.details());
        entry.sourceSpan().ifPresent(span -> {
            text.append("\n\nSource: ").append(span.source());
            text.append("\nSpan: ").append(formatSpan(span));
            appendSourceExcerpt(text, span);
        });
        return text.toString();
    }

    private static String formatSpan(SourceSpan span) {
        if (!span.hasLinePosition()) {
            return "unknown";
        }
        return span.beginLine() + "-" + span.endLine();
    }

    private static void appendSourceExcerpt(StringBuilder text, SourceSpan span) {
        if (!span.hasLinePosition()) {
            return;
        }
        try {
            List<String> lines = Files.readAllLines(span.source(), StandardCharsets.UTF_8);
            int first = Math.max(1, span.beginLine() - 1);
            int last = Math.min(lines.size(), Math.max(span.endLine(), span.beginLine()) + 1);
            text.append("\n\nSource excerpt:\n");
            for (int line = first; line <= last; line++) {
                text.append(String.format("%4d | %s%n", line, lines.get(line - 1)));
            }
        } catch (Exception error) {
            text.append("\n\nSource excerpt unavailable: ").append(error.getMessage());
        }
    }

    private static DefaultMutableTreeNode node(BdiTreeEntry entry) {
        return new DefaultMutableTreeNode(entry);
    }
}

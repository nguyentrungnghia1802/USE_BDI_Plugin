package org.tzi.use.plugins.bdi.ui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Supplier;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTabbedPane;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeSelectionModel;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.tzi.use.gui.views.View;
import org.tzi.use.plugins.bdi.ImportBdiAction;
import org.tzi.use.plugins.bdi.ImportJaCaMoAction;
import org.tzi.use.plugins.bdi.application.BdiImportService;
import org.tzi.use.plugins.bdi.application.BdiImportSnapshot;
import org.tzi.use.plugins.bdi.application.BdiProjectConfiguration;
import org.tzi.use.plugins.bdi.application.BdiSourceTracker;
import org.tzi.use.plugins.bdi.application.CurrentAnalysisSnapshot;
import org.tzi.use.plugins.bdi.application.CurrentAnalysisSnapshotService;
import org.tzi.use.plugins.bdi.application.MasProjectAnalysisRequest;
import org.tzi.use.plugins.bdi.application.MasProjectAnalysisResult;
import org.tzi.use.plugins.bdi.application.MasProjectAnalysisService;
import org.tzi.use.plugins.bdi.diagram.BdiDiagramBuilder;
import org.tzi.use.plugins.bdi.diagram.DiagramEdge;
import org.tzi.use.plugins.bdi.diagram.DiagramModel;
import org.tzi.use.plugins.bdi.diagram.DiagramNode;
import org.tzi.use.plugins.bdi.diagram.MasOverviewDiagramBuilder;
import org.tzi.use.plugins.bdi.diagram.TraceabilityDiagramContributor;
import org.tzi.use.plugins.bdi.index.BdiIndex;
import org.tzi.use.plugins.bdi.importer.AslDiagnostic;
import org.tzi.use.plugins.bdi.importer.MasProjectDiagnostic;
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
import org.tzi.use.plugins.bdi.model.mas.MasProjectModel;
import org.tzi.use.plugins.bdi.report.CurrentAnalysisReportService;
import org.tzi.use.plugins.bdi.report.ReportFormat;
import org.tzi.use.plugins.bdi.model.mapping.MappingDocument;
import org.tzi.use.plugins.bdi.problems.BdiProblem;
import org.tzi.use.plugins.bdi.problems.BdiProblemCollector;
import org.tzi.use.plugins.bdi.problems.BdiProblemPanel;
import org.tzi.use.plugins.bdi.trace.TraceabilityGraph;
import org.tzi.use.plugins.bdi.trace.TraceabilityGraphBuilder;
import org.tzi.use.plugins.bdi.use.UseModelSnapshot;
import org.tzi.use.plugins.bdi.use.LiveUseSnapshotProvider;
import org.tzi.use.plugins.bdi.use.UseSnapshotContext;
import org.tzi.use.plugins.bdi.use.UseSnapshotProvider;
import org.tzi.use.plugins.bdi.validation.SnapshotOclEvaluator;
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
    private final BdiDiagramPanel diagram;
    private final JTabbedPane tabs;
    private final MappingSuggestionService mappingSuggestionService;
    private final CurrentAnalysisSnapshotService analysisService;
    private final MasProjectAnalysisService projectAnalysisService;
    private final CurrentAnalysisReportService reportService = new CurrentAnalysisReportService();
    private final String configurationSummary;
    private final String projectName;
    private final BdiProjectConfiguration configuration;
    private Optional<UseModelSnapshot> useModel;
    private Optional<SnapshotOclEvaluator> oclEvaluator;
    private final Optional<UseSnapshotProvider> useSnapshotProvider;
    private final JButton reimportButton;
    private final JButton importProjectButton;
    private final JButton refreshUseButton;
    private final JButton exportButton;
    private final BdiSourceTracker sourceTracker;
    private BdiImportSnapshot snapshot;
    private Optional<CurrentAnalysisSnapshot> currentAnalysis = Optional.empty();
    private Optional<MasProjectModel> project = Optional.empty();
    private boolean analysisInputAvailable;
    private SwingWorker<?, ?> worker;
    private long importGeneration;
    private final AtomicLong useRefreshGeneration = new AtomicLong();

    public BdiExplorerView() {
        this(new BdiImportService(), new BdiSourceTracker(), Optional.empty(), Optional.empty(),
                BdiProjectConfiguration.defaults());
    }

    public BdiExplorerView(UseModelSnapshot useModel) {
        this(new BdiImportService(), new BdiSourceTracker(), Optional.of(Objects.requireNonNull(useModel, "useModel")), Optional.empty(),
                BdiProjectConfiguration.defaults());
    }

    public BdiExplorerView(MSystem system) {
        this(() -> Objects.requireNonNull(system, "system"), BdiProjectConfiguration.defaults());
    }

    public BdiExplorerView(MSystem system, BdiProjectConfiguration configuration) {
        this(() -> Objects.requireNonNull(system, "system"), configuration);
    }

    public BdiExplorerView(Supplier<MSystem> currentSystem, BdiProjectConfiguration configuration) {
        this(new LiveUseSnapshotProvider(currentSystem), configuration);
    }

    private BdiExplorerView(UseSnapshotProvider provider, BdiProjectConfiguration configuration) {
        this(provider.capture(), provider, configuration);
    }

    private BdiExplorerView(
            UseSnapshotContext initial,
            UseSnapshotProvider provider,
            BdiProjectConfiguration configuration) {
        this(
                new BdiImportService(),
                new BdiSourceTracker(),
                Optional.of(initial.snapshot()),
                Optional.of(initial.oclEvaluator()),
                Objects.requireNonNull(configuration, "configuration"),
                Optional.of(provider));
    }

    BdiExplorerView(BdiImportService importService) {
        this(importService, new BdiSourceTracker(), Optional.empty(), Optional.empty(),
                BdiProjectConfiguration.defaults());
    }

    BdiExplorerView(BdiImportService importService, BdiSourceTracker sourceTracker) {
        this(importService, sourceTracker, Optional.empty(), Optional.empty(),
                BdiProjectConfiguration.defaults());
    }

    BdiExplorerView(
            BdiImportService importService,
            BdiSourceTracker sourceTracker,
            UseModelSnapshot useModel) {
        this(importService, sourceTracker, Optional.of(Objects.requireNonNull(useModel, "useModel")), Optional.empty(),
                BdiProjectConfiguration.defaults());
    }

    BdiExplorerView(
            BdiImportService importService,
            BdiSourceTracker sourceTracker,
            UseModelSnapshot useModel,
            BdiProjectConfiguration configuration) {
        this(importService, sourceTracker, Optional.of(Objects.requireNonNull(useModel, "useModel")), Optional.empty(),
                Objects.requireNonNull(configuration, "configuration"));
    }

    BdiExplorerView(
            BdiImportService importService,
            BdiSourceTracker sourceTracker,
            UseSnapshotProvider provider,
            BdiProjectConfiguration configuration) {
        this(importService, sourceTracker, provider.capture(), provider, configuration);
    }

    private BdiExplorerView(
            BdiImportService importService,
            BdiSourceTracker sourceTracker,
            UseSnapshotContext initial,
            UseSnapshotProvider provider,
            BdiProjectConfiguration configuration) {
        this(
                importService,
                sourceTracker,
                Optional.of(initial.snapshot()),
                Optional.of(initial.oclEvaluator()),
                configuration,
                Optional.of(provider));
    }

    private BdiExplorerView(
            BdiImportService importService,
            BdiSourceTracker sourceTracker,
            Optional<UseModelSnapshot> useModel,
            Optional<SnapshotOclEvaluator> oclEvaluator,
            BdiProjectConfiguration configuration) {
        this(importService, sourceTracker, useModel, oclEvaluator, configuration, Optional.empty());
    }

    private BdiExplorerView(
            BdiImportService importService,
            BdiSourceTracker sourceTracker,
            Optional<UseModelSnapshot> useModel,
            Optional<SnapshotOclEvaluator> oclEvaluator,
            BdiProjectConfiguration configuration,
            Optional<UseSnapshotProvider> useSnapshotProvider) {
        this(
                importService,
                sourceTracker,
                useModel,
                oclEvaluator,
                Objects.requireNonNull(configuration, "configuration").newOrchestrator(),
                configuration.summary(),
                configuration.projectRoot(),
                configuration,
                useSnapshotProvider);
    }

    private BdiExplorerView(
            BdiImportService importService,
            BdiSourceTracker sourceTracker,
            Optional<UseModelSnapshot> useModel,
            Optional<SnapshotOclEvaluator> oclEvaluator,
            ValidationOrchestrator validationOrchestrator,
            String configurationSummary,
            Optional<Path> projectRoot,
            BdiProjectConfiguration configuration,
            Optional<UseSnapshotProvider> useSnapshotProvider) {
        super(new BorderLayout(6, 6));
        this.importService = Objects.requireNonNull(importService, "importService");
        this.sourceTracker = Objects.requireNonNull(sourceTracker, "sourceTracker");
        this.useModel = Objects.requireNonNull(useModel, "useModel");
        this.oclEvaluator = Objects.requireNonNull(oclEvaluator, "oclEvaluator");
        this.useSnapshotProvider = Objects.requireNonNull(useSnapshotProvider, "useSnapshotProvider");
        this.configuration = Objects.requireNonNull(configuration, "configuration");
        this.mappingSuggestionService = new MappingSuggestionService();
        this.projectAnalysisService = new MasProjectAnalysisService();
        ValidationOrchestrator configuredOrchestrator = Objects.requireNonNull(
                validationOrchestrator, "validationOrchestrator");
        this.configurationSummary = Objects.requireNonNull(configurationSummary, "configurationSummary");
        this.projectName = projectRoot
                .map(Path::getFileName)
                .map(Path::toString)
                .filter(name -> !name.isBlank())
                .orElse("USE-BDI-Analysis");
        this.analysisService = new CurrentAnalysisSnapshotService(
                configuredOrchestrator,
                configurationSummary,
                "0.1.0",
                "USE-7.1.1");
        this.snapshot = new BdiImportSnapshot(List.of(), List.of(), BdiIndex.empty());

        JButton importButton = new JButton("Import .asl...");
        importButton.setToolTipText("Choose AgentSpeak source files");
        importButton.addActionListener(event -> ImportBdiAction.chooseAndImport(this));
        importProjectButton = new JButton("Import .jcm...");
        importProjectButton.setToolTipText("Choose a static JaCaMo project");
        importProjectButton.addActionListener(event -> ImportJaCaMoAction.chooseAndImport(this));
        reimportButton = new JButton("Re-import changed");
        reimportButton.setToolTipText("Re-import all selected files after a source change");
        reimportButton.setEnabled(false);
        reimportButton.addActionListener(event -> reimportChangedFiles());
        refreshUseButton = new JButton("Refresh USE Snapshot");
        refreshUseButton.setToolTipText("Capture the current USE model/state and re-run analysis");
        refreshUseButton.setEnabled(useSnapshotProvider.isPresent());
        refreshUseButton.addActionListener(event -> refreshUseSnapshot());
        exportButton = new JButton("Export Current Analysis...");
        exportButton.setToolTipText("Export the currently displayed Problems analysis as JSON or HTML");
        exportButton.setEnabled(false);
        exportButton.addActionListener(event -> chooseExportCurrentAnalysis());
        status = new JLabel();
        setStatus("No AgentSpeak source imported; " + configurationSummary);
        JPanel importActions = new JPanel(new FlowLayout(FlowLayout.LEADING, 6, 2));
        importActions.add(importButton);
        importActions.add(importProjectButton);
        importActions.add(reimportButton);
        importActions.setAlignmentX(LEFT_ALIGNMENT);
        JPanel analysisActions = new JPanel(new FlowLayout(FlowLayout.LEADING, 6, 2));
        analysisActions.add(refreshUseButton);
        analysisActions.add(exportButton);
        analysisActions.setAlignmentX(LEFT_ALIGNMENT);
        JPanel statusRow = new JPanel(new BorderLayout());
        statusRow.add(status, BorderLayout.CENTER);
        statusRow.setBorder(BorderFactory.createEmptyBorder(0, 6, 3, 6));
        statusRow.setAlignmentX(LEFT_ALIGNMENT);
        JPanel toolbar = new JPanel();
        toolbar.setLayout(new BoxLayout(toolbar, BoxLayout.Y_AXIS));
        toolbar.add(importActions);
        toolbar.add(analysisActions);
        toolbar.add(statusRow);
        add(toolbar, BorderLayout.NORTH);

        tree = new JTree(createTree(snapshot));
        tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        tree.addTreeSelectionListener(this::showSelection);
        tree.setRootVisible(true);

        detail = new JTextArea();
        detail.setEditable(false);
        detail.setLineWrap(true);
        detail.setWrapStyleWord(true);
        detail.setFont(new java.awt.Font(java.awt.Font.MONOSPACED, java.awt.Font.PLAIN, 12));
        detail.setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));

        JSplitPane split = new JSplitPane(
                JSplitPane.HORIZONTAL_SPLIT,
                new JScrollPane(tree),
                new JScrollPane(detail));
        split.setResizeWeight(0.42);
        split.setContinuousLayout(true);
        split.setPreferredSize(new Dimension(900, 520));
        problems = new BdiProblemPanel();
        problems.setProblemSelectionListener(this::showProblemSelection);
        mapping = new MappingEditorPanel(new org.tzi.use.plugins.bdi.persistence.MappingFileRepository(), projectRoot);
        mapping.setDocumentChangeListener(ignored -> refreshProblems());
        diagram = new BdiDiagramPanel();
        diagram.setSelectionListener(this::showDiagramSelection);
        tabs = new JTabbedPane();
        tabs.addTab("Explorer", split);
        tabs.addTab("Diagram", diagram);
        tabs.addTab("Problems", problems);
        tabs.addTab("Mapping", mapping);
        tabs.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
        add(tabs, BorderLayout.CENTER);
        detail.setText("Select a BDI node to inspect its details and source span.");
    }

    public void importFiles(List<Path> sources) {
        List<Path> selectedSources = List.copyOf(Objects.requireNonNull(sources, "sources"));
        startImport(selectedSources, "Importing AgentSpeak...");
    }

    public void importProject(Path projectFile) {
        Path normalized = Objects.requireNonNull(projectFile, "projectFile")
                .toAbsolutePath().normalize();
        try {
            alignMappingFingerprint();
            MasProjectAnalysisRequest request = MasProjectAnalysisRequest.of(
                    normalized,
                    Instant.now(),
                    useModel,
                    oclEvaluator,
                    mapping.document(),
                    configuration);
            if (worker != null && !worker.isDone()) {
                worker.cancel(true);
            }
            long generation = ++importGeneration;
            setStatus("Importing JaCaMo project " + normalized.getFileName() + "...");
            BdiProjectImportWorker projectWorker = new BdiProjectImportWorker(
                    projectAnalysisService,
                    request,
                    result -> {
                        if (generation == importGeneration) {
                            applyProjectResult(result);
                        }
                    },
                    failure -> {
                        if (generation == importGeneration) {
                            showFailure(failure);
                        }
                    });
            worker = projectWorker;
            projectWorker.execute();
        } catch (RuntimeException error) {
            showFailure(error);
        }
    }

    public boolean reimportChangedFiles() {
        List<Path> changed = sourceTracker.changedSources();
        if (changed.isEmpty()) {
            setStatus("No changed AgentSpeak source files");
            return false;
        }
        startImport(sourceTracker.sources(), "Re-importing " + changed.size() + " changed file(s)...");
        return true;
    }

    public void refreshUseSnapshot() {
        long generation = useRefreshGeneration.incrementAndGet();
        Runnable refresh = () -> refreshUseSnapshotOnEdt(generation);
        if (SwingUtilities.isEventDispatchThread()) {
            refresh.run();
        } else {
            SwingUtilities.invokeLater(refresh);
        }
    }

    private void startImport(List<Path> sources, String message) {
        if (worker != null && !worker.isDone()) {
            worker.cancel(true);
        }
        long generation = ++importGeneration;
        setStatus(message);
        worker = new BdiImportWorker(
                importService,
                sources,
                imported -> {
                    if (generation == importGeneration) {
                        applySnapshot(imported, sources);
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
        useRefreshGeneration.incrementAndGet();
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

    BdiDiagramPanel diagramForTest() {
        return diagram;
    }

    JTabbedPane tabsForTest() {
        return tabs;
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

    JButton refreshUseButtonForTest() {
        return refreshUseButton;
    }

    JButton exportButtonForTest() {
        return exportButton;
    }

    Optional<UseModelSnapshot> useModelForTest() {
        return useModel;
    }

    Optional<CurrentAnalysisSnapshot> currentAnalysisForTest() {
        return currentAnalysis;
    }

    Optional<MasProjectModel> projectForTest() {
        return project;
    }

    JButton importProjectButtonForTest() {
        return importProjectButton;
    }

    Path exportCurrentAnalysisForTest(Path output, ReportFormat format, boolean overwrite) throws IOException {
        return exportCurrentAnalysis(output, format, overwrite);
    }

    JLabel statusForTest() {
        return status;
    }

    private void applySnapshot(BdiImportSnapshot imported, List<Path> importedSources) {
        Runnable update = () -> {
            project = Optional.empty();
            snapshot = imported;
            analysisInputAvailable = true;
            sourceTracker.track(importedSources);
            sourceTracker.markImported();
            alignMappingFingerprint();
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
            message += "; " + configurationSummary;
            setStatus(message);
            detail.setText("Select a BDI node to inspect its details and source span.");
        };
        if (SwingUtilities.isEventDispatchThread()) {
            update.run();
        } else {
            SwingUtilities.invokeLater(update);
        }
    }

    private void applyProjectResult(MasProjectAnalysisResult result) {
        Runnable update = () -> {
            project = result.project();
            snapshot = result.snapshot().bdiImport();
            currentAnalysis = Optional.of(result.snapshot());
            analysisInputAvailable = true;
            sourceTracker.clear();
            reimportButton.setEnabled(false);
            mapping.setSuggestions(useModel
                    .map(model -> mappingSuggestionService.suggest(snapshot.models(), snapshot.index(), model))
                    .orElse(List.of()));
            problems.setProblems(BdiProblemCollector.collectConsistencyIssues(result.snapshot().issues()));
            exportButton.setEnabled(true);
            tree.setModel(new DefaultTreeModel(createTree(snapshot, result.projectDiagnostics())));
            refreshDiagram(result.snapshot());
            String projectName = result.project().map(MasProjectModel::name).orElse("unknown");
            setStatus("JaCaMo project " + projectName + ": "
                    + snapshot.fileCount() + " AgentSpeak file(s), "
                    + result.project().map(value -> value.agents().size()).orElse(0)
                    + " agent instance(s), " + result.projectDiagnostics().size()
                    + " project diagnostic(s); " + problems.problemCount()
                    + " problem(s); " + configurationSummary);
            detail.setText("Select a BDI node to inspect its details and source span.");
        };
        if (SwingUtilities.isEventDispatchThread()) {
            update.run();
        } else {
            SwingUtilities.invokeLater(update);
        }
    }

    private void showFailure(Throwable failure) {
        String message = failure.getMessage() == null ? failure.getClass().getSimpleName() : failure.getMessage();
        setStatus("Import failed: " + message
                + (analysisInputAvailable ? "; previous analysis retained" : ""));
    }

    private void refreshUseSnapshotOnEdt(long generation) {
        if (generation != useRefreshGeneration.get()) {
            return;
        }
        setStatus("Refreshing USE snapshot...");
        try {
            UseSnapshotProvider provider = useSnapshotProvider.orElseThrow(
                    () -> new IllegalStateException("No live USE system is available"));
            UseSnapshotContext refreshed = provider.capture();
            if (generation != useRefreshGeneration.get()) {
                return;
            }
            String beforeAnalysis = refreshed.snapshot().fingerprint();
            useModel = Optional.of(refreshed.snapshot());
            oclEvaluator = Optional.of(refreshed.oclEvaluator());
            mapping.setSuggestions(mappingSuggestionService.suggest(
                    snapshot.models(), snapshot.index(), refreshed.snapshot()));
            refreshProblems();
            String afterAnalysis = provider.capture().snapshot().fingerprint();
            if (!beforeAnalysis.equals(afterAnalysis)) {
                throw new IllegalStateException("USE state changed during snapshot analysis");
            }
            if (generation == useRefreshGeneration.get()) {
                String shortFingerprint = beforeAnalysis.substring(0, Math.min(12, beforeAnalysis.length()));
                setStatus("USE snapshot refreshed [" + shortFingerprint
                        + "]; " + problems.problemCount() + " problem(s); " + configurationSummary);
            }
        } catch (RuntimeException error) {
            if (generation == useRefreshGeneration.get()) {
                String message = error.getMessage() == null ? error.getClass().getSimpleName() : error.getMessage();
                setStatus("USE snapshot refresh failed: " + message + "; " + configurationSummary);
            }
        }
    }

    private void refreshProblems() {
        if (!analysisInputAvailable) {
            currentAnalysis = Optional.empty();
            problems.setProblems(List.of());
            exportButton.setEnabled(false);
            diagram.setUnavailable("Import AgentSpeak or a JaCaMo project to create an analysis");
            return;
        }
        currentAnalysis = Optional.of(analysisService.create(
                Instant.now(),
                snapshot,
                useModel,
                oclEvaluator,
                mapping.document()));
        problems.setProblems(BdiProblemCollector.collectConsistencyIssues(
                currentAnalysis.orElseThrow().issues()));
        exportButton.setEnabled(true);
        refreshDiagram(currentAnalysis.orElseThrow());
    }

    private void alignMappingFingerprint() {
        useModel.filter(model -> mapping.document().useFingerprint().equals("unknown"))
                .ifPresent(model -> mapping.setDocumentWithoutNotification(
                        MappingDocument.empty(model.fingerprint())));
    }

    private void refreshDiagram(CurrentAnalysisSnapshot analysis) {
        try {
            Path root = diagramProjectRoot(analysis);
            DiagramModel structure = new BdiDiagramBuilder().build(analysis, root);
            TraceabilityGraph trace = new TraceabilityGraphBuilder().build(analysis, root);
            DiagramModel evidence = new TraceabilityDiagramContributor().build(trace);
            List<org.tzi.use.plugins.bdi.diagram.DiagramNode> nodes = new ArrayList<>(structure.nodes());
            nodes.addAll(evidence.nodes());
            List<DiagramEdge> edges = new ArrayList<>(structure.edges());
            edges.addAll(evidence.edges());
            List<org.tzi.use.plugins.bdi.diagram.DiagramGroup> groups = new ArrayList<>(structure.groups());
            groups.addAll(evidence.groups());
            project.ifPresent(value -> {
                DiagramModel overview = new MasOverviewDiagramBuilder().build(value, analysis, root);
                nodes.addAll(overview.nodes());
                edges.addAll(overview.edges());
                groups.addAll(overview.groups());
            });
            diagram.setDiagram(new DiagramModel(nodes, edges, groups));
        } catch (RuntimeException error) {
            String message = error.getMessage() == null ? error.getClass().getSimpleName() : error.getMessage();
            diagram.setUnavailable("Current analysis could not be projected: " + message);
        }
    }

    private Path diagramProjectRoot(CurrentAnalysisSnapshot analysis) {
        if (configuration.projectRoot().isPresent()) {
            return configuration.projectRoot().orElseThrow();
        }
        List<Path> sources = analysis.bdiImport().models().stream()
                .map(AgentModel::source)
                .toList();
        if (sources.isEmpty()) {
            return Path.of("").toAbsolutePath().normalize();
        }
        Path root = sources.get(0).getParent();
        for (Path source : sources) {
            while (root != null && !source.startsWith(root)) {
                root = root.getParent();
            }
        }
        return root == null ? Path.of("").toAbsolutePath().normalize() : root;
    }

    private void showDiagramSelection(DiagramNode node) {
        StringBuilder selection = new StringBuilder();
        selection.append("Diagram selection\n")
                .append("Type: ").append(node.type()).append('\n')
                .append("Label: ").append(node.label()).append('\n')
                .append("Visual state: ").append(DiagramVisualStateResolver.resolve(node).badge()).append('\n');
        node.source().ifPresent(source -> selection.append("Source: ")
                .append(source.projectPath()).append(':').append(source.beginLine()).append('\n'));
        node.attributes().forEach((key, value) -> selection.append(key).append(": ").append(value).append('\n'));
        node.issueMarker().ifPresent(marker -> selection.append("Issue: ")
                .append(marker.ruleId()).append(" [").append(marker.severity()).append(", ")
                .append(marker.status()).append(", ").append(marker.certainty()).append("]\n")
                .append("Evidence:\n")
                .append(marker.evidence().stream().map(value -> "- " + value).collect(java.util.stream.Collectors.joining("\n")))
                .append('\n'));
        detail.setText(selection.toString());
    }

    private void showProblemSelection(BdiProblem problem) {
        boolean highlighted = diagram.highlightIssue(problem.code());
        if (highlighted) {
            tabs.setSelectedComponent(diagram);
        }
        StringBuilder selection = new StringBuilder();
        selection.append("Problem selection\n")
                .append("Code: ").append(problem.code()).append('\n')
                .append("Severity: ").append(problem.severity()).append('\n')
                .append("Source: ").append(problem.source()).append(':').append(problem.location()).append('\n')
                .append("Message: ").append(problem.message()).append('\n')
                .append(highlighted
                        ? "Evidence path: highlighted in Diagram tab.\n"
                        : "Evidence path: no matching diagram issue in the current projection.\n");
        detail.setText(selection.toString());
    }

    private void chooseExportCurrentAnalysis() {
        JFileChooser chooser = BdiFileChooserSupport.create();
        chooser.setDialogTitle("Export current BDI analysis");
        FileNameExtensionFilter jsonFilter = new FileNameExtensionFilter(
                ReportFormat.JSON.description(), ReportFormat.JSON.extension());
        FileNameExtensionFilter htmlFilter = new FileNameExtensionFilter(
                ReportFormat.HTML.description(), ReportFormat.HTML.extension(), "htm");
        chooser.setAcceptAllFileFilterUsed(false);
        chooser.addChoosableFileFilter(jsonFilter);
        chooser.addChoosableFileFilter(htmlFilter);
        chooser.setFileFilter(jsonFilter);
        chooser.setSelectedFile(new java.io.File("bdi-analysis.json"));
        if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) {
            setStatus("Analysis export cancelled; " + configurationSummary);
            return;
        }

        ReportFormat format = chooser.getFileFilter() == htmlFilter ? ReportFormat.HTML : ReportFormat.JSON;
        Path output = withExtension(chooser.getSelectedFile().toPath(), format);
        boolean overwrite = false;
        if (Files.exists(output)) {
            int choice = JOptionPane.showConfirmDialog(
                    this,
                    "Replace existing report?\n" + output.toAbsolutePath(),
                    "Confirm report overwrite",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE);
            if (choice != JOptionPane.YES_OPTION) {
                setStatus("Analysis export cancelled; existing file retained: " + output.toAbsolutePath());
                return;
            }
            overwrite = true;
        }
        try {
            Path exported = exportCurrentAnalysis(output, format, overwrite);
            setStatus("Exported current analysis to " + exported);
        } catch (IOException | RuntimeException error) {
            setStatus("Analysis export failed for " + output.toAbsolutePath() + ": " + error.getMessage());
        }
    }

    private void setStatus(String message) {
        status.setText(message);
        status.setToolTipText(message);
    }

    private Path exportCurrentAnalysis(Path output, ReportFormat format, boolean overwrite) throws IOException {
        CurrentAnalysisSnapshot analysis = currentAnalysis.orElseThrow(() ->
                new IOException("No current analysis is available; import AgentSpeak first"));
        return reportService.export(projectName, analysis, format, output, overwrite);
    }

    private static Path withExtension(Path output, ReportFormat format) {
        String filename = output.getFileName().toString();
        if (ReportFormat.fromFilename(filename) == format
                && filename.toLowerCase(java.util.Locale.ROOT).endsWith("." + format.extension())) {
            return output;
        }
        return output.resolveSibling(filename + "." + format.extension());
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

    private DefaultMutableTreeNode createTree(
            BdiImportSnapshot imported,
            List<MasProjectDiagnostic> projectDiagnostics) {
        DefaultMutableTreeNode root = createTree(imported);
        if (!projectDiagnostics.isEmpty()) {
            DefaultMutableTreeNode diagnostics = node(new BdiTreeEntry(
                    "JaCaMo diagnostics (" + projectDiagnostics.size() + ")",
                    "Project diagnostics retained with their source and severity.",
                    Optional.empty()));
            projectDiagnostics.forEach(diagnostic ->
                    diagnostics.add(node(projectDiagnosticEntry(diagnostic))));
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

    private static BdiTreeEntry projectDiagnosticEntry(MasProjectDiagnostic diagnostic) {
        SourceSpan span = diagnostic.line() > 0
                ? new SourceSpan(
                        diagnostic.source(),
                        diagnostic.line(),
                        diagnostic.column(),
                        diagnostic.line(),
                        diagnostic.column())
                : SourceSpan.unknown(diagnostic.source());
        return new BdiTreeEntry(
                diagnostic.code() + " [" + diagnostic.severity() + "]",
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

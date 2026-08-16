package org.tzi.use.plugins.bdi.ui;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import javax.imageio.ImageIO;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;
import org.tzi.use.config.Options;
import org.tzi.use.gui.main.MainWindow;
import org.tzi.use.gui.views.diagrams.classdiagram.ClassDiagramView;
import org.tzi.use.gui.views.diagrams.objectdiagram.NewObjectDiagramView;
import org.tzi.use.main.Session;
import org.tzi.use.parser.use.USECompiler;
import org.tzi.use.plugins.bdi.application.BdiProjectConfiguration;
import org.tzi.use.plugins.bdi.cli.HeadlessAnalysisRequest;
import org.tzi.use.plugins.bdi.cli.HeadlessAnalysisService;
import org.tzi.use.plugins.bdi.cli.HeadlessStateFixture;
import org.tzi.use.plugins.bdi.diagram.TraceabilityDiagramContributor;
import org.tzi.use.plugins.bdi.diagram.DiagramModel;
import org.tzi.use.plugins.bdi.diagram.DiagramNodeType;
import org.tzi.use.plugins.bdi.diagram.MasOverviewDiagramBuilder;
import org.tzi.use.plugins.bdi.persistence.MappingFileRepository;
import org.tzi.use.plugins.bdi.report.ReportFormat;
import org.tzi.use.plugins.bdi.trace.TraceabilityGraphBuilder;
import org.tzi.use.uml.mm.MModel;
import org.tzi.use.uml.mm.ModelFactory;
import org.tzi.use.uml.sys.MSystem;
import org.tzi.use.gui.views.diagrams.DiagramView.LayoutType;

/** Opt-in source-backed raster capture for the thesis release gate. */
class ReleaseScreenshotCaptureTest {
    private static final Dimension CAPTURE_SIZE = new Dimension(1280, 820);

    @Test
    void capturesCurrentAuctionUseAndPluginViews() throws Exception {
        String requestedOutput = System.getProperty("bdi.releaseScreenshots");
        Assumptions.assumeTrue(requestedOutput != null && !requestedOutput.isBlank(),
                "Release screenshots are generated only by the explicit capture command");

        Path root = repositoryRoot();
        Path requested = Path.of(requestedOutput);
        Path output = (requested.isAbsolute() ? requested : root.resolve(requested)).normalize();
        Files.createDirectories(output);
        Path releaseOutput = root.resolve("target/release-evidence");
        Files.createDirectories(releaseOutput);

        Path demo = root.resolve("use-bdi-plugin/demo/auction");
        Path baselineWorkspace = releaseOutput.resolve("auction-baseline-workspace");
        Files.createDirectories(baselineWorkspace);
        copy(demo.resolve("Auction_default.clt"), baselineWorkspace);
        MSystem baseline = loadSystem(copy(demo.resolve("Auction.use"), baselineWorkspace));
        populateAuctionObjects(baseline);

        AtomicReference<MainWindow> mainWindowRef = new AtomicReference<>();
        SwingUtilities.invokeAndWait(() -> {
            Options.processArgs(new String[] { "-nr", "-noplugins" });
            Session session = new Session();
            session.setSystem(baseline);
            MainWindow mainWindow = MainWindow.create(session);
            mainWindow.setVisible(false);
            mainWindowRef.set(mainWindow);
        });

        try {
            MainWindow mainWindow = mainWindowRef.get();
            capture(output.resolve("release_use_class_diagram.png"),
                    new ClassDiagramView(mainWindow, baseline, true));
            NewObjectDiagramView objectDiagram = new NewObjectDiagramView(mainWindow, baseline);
            prepareObjectDiagram(objectDiagram);
            capture(output.resolve("release_use_object_diagram.png"), objectDiagram);
            objectDiagram.detachModel();

            BdiProjectConfiguration demoConfiguration =
                    new org.tzi.use.plugins.bdi.application.BdiProjectConfigurationLoader()
                            .load(demo.resolve("Auction.use"));
            BdiExplorerView explorer = new BdiExplorerView(baseline, demoConfiguration);
            explorer.importProject(demo.resolve("auction.jcm"));
            waitForProject(explorer);
            explorer.mappingForTest().setDocument(new MappingFileRepository().load(
                    demo.resolve("Auction.bdimap.json"), demo));
            waitForDiagram(explorer);
            prepareExplorerTree(explorer);

            showTab(explorer, "Explorer");
            capture(output.resolve("release_bdi_explorer.png"), explorer);

            showTab(explorer, "Problems");
            capture(output.resolve("release_problems.png"), explorer);

            showTab(explorer, "Mapping");
            capture(output.resolve("release_mapping.png"), explorer);

            explorer.exportCurrentAnalysisForTest(
                    releaseOutput.resolve("current-analysis.json"), ReportFormat.JSON, true);
            SwingUtilities.invokeAndWait(() -> explorer.statusForTest().setText(
                    "Exported JSON: target/release-evidence/current-analysis.json"));
            showTab(explorer, "Explorer");
            capture(output.resolve("release_current_analysis_export.png"), explorer);

            showTab(explorer, "Diagram");
            setDiagramMode(explorer, DiagramViewMode.BDI_PLAN);
            focusFirst(explorer.diagramForTest(), DiagramNodeType.PLAN);
            capture(output.resolve("release_diagram_bdi_plan.png"), explorer);

            SwingUtilities.invokeAndWait(() -> explorer.diagramForTest().resetForTest().doClick());
            waitForDiagram(explorer);
            focusFirst(explorer.diagramForTest(), DiagramNodeType.AGENT);
            setDiagramMode(explorer, DiagramViewMode.MAPPING);
            capture(output.resolve("release_diagram_mapping_evidence.png"), explorer);

            DiagramModel staticMas = new MasOverviewDiagramBuilder().build(
                    explorer.projectForTest().orElseThrow(),
                    explorer.currentAnalysisForTest().orElseThrow(),
                    demo);
            BdiDiagramPanel staticPanel = new BdiDiagramPanel();
            SwingUtilities.invokeAndWait(() -> staticPanel.setDiagram(staticMas));
            waitForPanel(staticPanel);
            SwingUtilities.invokeAndWait(() -> staticPanel.stateForTest().setText(
                    "STATIC ONLY — no JaCaMo runtime; no Moise enactment; no live CArtAgO state"));
            capture(output.resolve("release_diagram_static_mas.png"), staticPanel);

            Path svg = releaseOutput.resolve("auction-current-view.svg");
            staticPanel.exportSvgForTest(svg, true);
            SwingUtilities.invokeAndWait(() -> staticPanel.stateForTest().setText(
                    "Exported SVG: target/release-evidence/auction-current-view.svg | "
                            + "Static analysis only: no JaCaMo runtime / no Moise enactment / no live CArtAgO state"));
            capture(output.resolve("release_svg_export.png"), staticPanel);

            BdiDiagramPanel mutant = signatureMutantDiagram(root, releaseOutput);
            SwingUtilities.invokeAndWait(() -> assertTrue(mutant.highlightIssue("SIG-001"),
                    "Reviewed signature finding must have a diagram evidence path"));
            capture(output.resolve("release_issue_highlight_sig001.png"), mutant);
        } finally {
            SwingUtilities.invokeAndWait(() -> {
                MainWindow mainWindow = mainWindowRef.get();
                if (mainWindow != null) {
                    mainWindow.setVisible(false);
                    mainWindow.dispose();
                }
            });
        }

        for (String filename : expectedScreenshots()) {
            Path image = output.resolve(filename);
            assertTrue(Files.isRegularFile(image), () -> "Missing release screenshot: " + image);
            assertTrue(Files.size(image) > 2_000L, () -> "Suspiciously small release screenshot: " + image);
            BufferedImage decoded = ImageIO.read(image.toFile());
            assertTrue(decoded != null && decoded.getWidth() == CAPTURE_SIZE.width
                            && decoded.getHeight() == CAPTURE_SIZE.height,
                    () -> "Unexpected screenshot dimensions: " + image);
        }
        System.out.println("RELEASE_SCREENSHOTS_OK: files=" + expectedScreenshots().size()
                + " demo=Auction baseline+SIG-001 size=" + CAPTURE_SIZE.width + "x" + CAPTURE_SIZE.height);
    }

    private static BdiDiagramPanel signatureMutantDiagram(Path root, Path releaseOutput) throws Exception {
        Path fixtureRoot = root.resolve("use-bdi-plugin/src/test/resources/fixtures/casestudy/auction");
        Path workspace = releaseOutput.resolve("signature-mutant-workspace");
        Files.createDirectories(workspace);
        Path useFile = copy(fixtureRoot.resolve("mutants/signature-open-flag.use"), workspace);
        Path auctioneer = copy(fixtureRoot.resolve("auctioneer.asl"), workspace);
        Path bidder = copy(fixtureRoot.resolve("bidder.asl"), workspace);
        Path mapping = copy(root.resolve("docs/project/evidence/auction-signature.bdimap.json"), workspace);
        var result = new HeadlessAnalysisService().analyze(new HeadlessAnalysisRequest(
                useFile,
                List.of(auctioneer, bidder),
                Optional.empty(),
                Optional.of(mapping),
                Optional.empty(),
                Optional.empty(),
                Instant.parse("2026-08-17T00:00:00Z"),
                Optional.of("SIG-001-release-capture"),
                Optional.of(new HeadlessStateFixture("auction-populated"))));
        assertTrue(result.snapshot().issues().stream().anyMatch(issue -> issue.ruleId().equals("SIG-001")));
        DiagramModel diagram = new TraceabilityDiagramContributor().build(
                new TraceabilityGraphBuilder().build(result.snapshot(), workspace));
        DiagramHighlightPath.Highlight highlight = DiagramHighlightPath.forIssue(diagram, "SIG-001");
        assertFalse(highlight.isEmpty());
        Set<String> nodeIds = highlight.nodeIds();
        diagram = new DiagramModel(
                diagram.nodes().stream().filter(node -> nodeIds.contains(node.id())).toList(),
                diagram.edges().stream().filter(edge -> highlight.edgeIds().contains(edge.id())).toList(),
                List.of());
        BdiDiagramPanel panel = new BdiDiagramPanel();
        DiagramModel reviewedPath = diagram;
        SwingUtilities.invokeAndWait(() -> panel.setDiagram(reviewedPath));
        waitForPanel(panel);
        return panel;
    }

    private static Path copy(Path source, Path workspace) throws Exception {
        Path destination = workspace.resolve(source.getFileName());
        Files.copy(source, destination, StandardCopyOption.REPLACE_EXISTING);
        return destination;
    }

    private static MSystem loadSystem(Path specification) throws Exception {
        StringWriter errors = new StringWriter();
        MModel model;
        try (var input = Files.newInputStream(specification)) {
            model = USECompiler.compileSpecification(
                    input, specification.toString(), new PrintWriter(errors), new ModelFactory());
        }
        assertTrue(model != null, errors::toString);
        model.setFilename(specification.toString());
        return new MSystem(model);
    }

    private static void populateAuctionObjects(MSystem system) throws Exception {
        var auctioneer = system.state().createObject(system.model().getClass("Auctioneer"), "auctioneer1");
        var auction = system.state().createObject(system.model().getClass("Auction"), "auction1");
        var bidder = system.state().createObject(system.model().getClass("Bidder"), "bidder1");
        system.state().createLink(
                system.model().getAssociation("AuctioneerAuctions"), List.of(auctioneer, auction), null);
        system.state().createLink(
                system.model().getAssociation("AuctionBidders"), List.of(auction, bidder), null);
    }

    private static void capture(Path output, JComponent component) throws Exception {
        AtomicReference<BufferedImage> imageRef = new AtomicReference<>();
        SwingUtilities.invokeAndWait(() -> {
            JFrame host = new JFrame("USE 7.1.1 — Auction release evidence");
            try {
                host.setContentPane(component);
                host.setSize(CAPTURE_SIZE);
                component.setSize(CAPTURE_SIZE);
                layoutRecursively(component);
                findDiagramPanel(component).ifPresent(panel -> panel.fitForTest().doClick());
                BufferedImage image = new BufferedImage(
                        CAPTURE_SIZE.width, CAPTURE_SIZE.height, BufferedImage.TYPE_INT_RGB);
                Graphics2D graphics = image.createGraphics();
                try {
                    graphics.setColor(Color.WHITE);
                    graphics.fillRect(0, 0, image.getWidth(), image.getHeight());
                    component.printAll(graphics);
                } finally {
                    graphics.dispose();
                }
                imageRef.set(image);
            } finally {
                host.setVisible(false);
                host.dispose();
            }
        });
        assertTrue(ImageIO.write(imageRef.get(), "png", output.toFile()), "PNG writer unavailable");
    }

    private static void prepareObjectDiagram(NewObjectDiagramView view) throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            view.setSize(CAPTURE_SIZE);
            layoutRecursively(view);
            view.getDiagram().setSize(CAPTURE_SIZE.width - 20, CAPTURE_SIZE.height - 20);
            view.getDiagram().startLayoutFormatThread(LayoutType.Horizontal, 180, 120, false);
        });
        Thread.sleep(750);
        flushEdt();
    }

    private static void showTab(BdiExplorerView explorer, String title) throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            for (int index = 0; index < explorer.tabsForTest().getTabCount(); index++) {
                if (title.equals(explorer.tabsForTest().getTitleAt(index))) {
                    explorer.tabsForTest().setSelectedIndex(index);
                    return;
                }
            }
            throw new IllegalArgumentException("Tab not found: " + title);
        });
        flushEdt();
    }

    private static void setDiagramMode(BdiExplorerView explorer, DiagramViewMode mode) throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            explorer.diagramForTest().setViewMode(mode);
            explorer.diagramForTest().fitForTest().doClick();
        });
        waitForDiagram(explorer);
    }

    private static void prepareExplorerTree(BdiExplorerView explorer) throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            for (int row = explorer.treeForTest().getRowCount() - 1; row > 0; row--) {
                explorer.treeForTest().collapseRow(row);
            }
            explorer.treeForTest().expandRow(0);
            explorer.treeForTest().setSelectionRow(0);
        });
        flushEdt();
    }

    private static void focusFirst(BdiDiagramPanel panel, DiagramNodeType type) throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            String nodeId = panel.sourceModelForTest().nodes().stream()
                    .filter(node -> node.type() == type)
                    .map(node -> node.id())
                    .findFirst()
                    .orElseThrow(() -> new IllegalStateException("No diagram node for focus type " + type));
            panel.canvasForTest().selectNodeForTest(nodeId);
            if (type == DiagramNodeType.AGENT) {
                panel.focusAgentForTest().doClick();
            } else {
                panel.focusGoalPlanForTest().doClick();
            }
        });
        waitForPanel(panel);
    }

    private static void waitForProject(BdiExplorerView explorer) throws Exception {
        waitUntil(() -> explorer.projectForTest().isPresent(), "Auction project import");
    }

    private static void waitForDiagram(BdiExplorerView explorer) throws Exception {
        waitUntil(() -> !explorer.diagramForTest().modelForTest().nodes().isEmpty(), "diagram layout");
        SwingUtilities.invokeAndWait(() -> explorer.diagramForTest().fitForTest().doClick());
        flushEdt();
    }

    private static void waitForPanel(BdiDiagramPanel panel) throws Exception {
        waitUntil(() -> !panel.modelForTest().nodes().isEmpty(), "mutant diagram layout");
        SwingUtilities.invokeAndWait(() -> panel.fitForTest().doClick());
        flushEdt();
    }

    private static void waitUntil(CheckedCondition condition, String description) throws Exception {
        long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(20);
        while (!condition.get() && System.nanoTime() < deadline) {
            Thread.sleep(25);
            flushEdt();
        }
        assertTrue(condition.get(), () -> "Timed out waiting for " + description);
        flushEdt();
    }

    private static void flushEdt() throws Exception {
        CountDownLatch flushed = new CountDownLatch(1);
        SwingUtilities.invokeLater(flushed::countDown);
        assertTrue(flushed.await(10, TimeUnit.SECONDS), "EDT did not flush in time");
    }

    private static void layoutRecursively(Container container) {
        container.doLayout();
        for (Component component : container.getComponents()) {
            if (component instanceof Container child) {
                layoutRecursively(child);
            }
        }
    }

    private static Optional<BdiDiagramPanel> findDiagramPanel(Container container) {
        if (container instanceof BdiDiagramPanel panel) {
            return Optional.of(panel);
        }
        for (Component component : container.getComponents()) {
            if (component instanceof Container child) {
                Optional<BdiDiagramPanel> found = findDiagramPanel(child);
                if (found.isPresent()) {
                    return found;
                }
            }
        }
        return Optional.empty();
    }

    private static List<String> expectedScreenshots() {
        return List.of(
                "release_use_class_diagram.png",
                "release_use_object_diagram.png",
                "release_bdi_explorer.png",
                "release_problems.png",
                "release_mapping.png",
                "release_current_analysis_export.png",
                "release_diagram_bdi_plan.png",
                "release_diagram_mapping_evidence.png",
                "release_diagram_static_mas.png",
                "release_svg_export.png",
                "release_issue_highlight_sig001.png");
    }

    private static Path repositoryRoot() {
        Path current = Path.of("").toAbsolutePath().normalize();
        while (current != null) {
            if (Files.isRegularFile(current.resolve("docs/project/16_PROJECT_COMPLETION_CHECKLIST.md"))) {
                return current;
            }
            current = current.getParent();
        }
        throw new IllegalStateException("Could not locate repository root");
    }

    @FunctionalInterface
    private interface CheckedCondition {
        boolean get() throws Exception;
    }
}

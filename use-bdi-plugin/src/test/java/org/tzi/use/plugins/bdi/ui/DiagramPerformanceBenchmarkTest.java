package org.tzi.use.plugins.bdi.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.GraphicsEnvironment;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.management.ManagementFactory;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HexFormat;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.tzi.use.parser.use.USECompiler;
import org.tzi.use.plugins.bdi.application.BdiImportService;
import org.tzi.use.plugins.bdi.application.BdiImportSnapshot;
import org.tzi.use.plugins.bdi.application.BdiProjectConfiguration;
import org.tzi.use.plugins.bdi.application.CurrentAnalysisSnapshot;
import org.tzi.use.plugins.bdi.application.CurrentAnalysisSnapshotService;
import org.tzi.use.plugins.bdi.application.MasProjectAnalysisRequest;
import org.tzi.use.plugins.bdi.application.MasProjectAnalysisResult;
import org.tzi.use.plugins.bdi.application.MasProjectAnalysisService;
import org.tzi.use.plugins.bdi.diagram.BdiDiagramBuilder;
import org.tzi.use.plugins.bdi.diagram.DiagramModel;
import org.tzi.use.plugins.bdi.model.mapping.MappingDocument;
import org.tzi.use.plugins.bdi.persistence.MappingFileRepository;
import org.tzi.use.plugins.bdi.use.UseModelSnapshot;
import org.tzi.use.plugins.bdi.use.UseUmlModelFacade;
import org.tzi.use.plugins.bdi.validation.ValidationOrchestrator;
import org.tzi.use.uml.mm.MModel;
import org.tzi.use.uml.mm.ModelFactory;
import org.tzi.use.uml.sys.MSystem;

/** Environment-dependent presentation timing with deterministic structural checks. */
@Tag("performance")
class DiagramPerformanceBenchmarkTest {
    private static final Instant FIXED_ANALYSIS_TIME = Instant.parse("2026-08-17T00:00:00Z");
    private static final int WARMUP_COUNT = 3;
    private static final int MEASURED_REPETITIONS = 15;

    @Test
    void recordsCanonicalDiagramPipelineTimingsAndStableStructure() throws Exception {
        List<BenchmarkCase> cases = benchmarkCases();
        List<CaseResult> results = new ArrayList<>();
        for (BenchmarkCase benchmarkCase : cases) {
            results.add(measure(benchmarkCase));
        }
        assertEquals(List.of("family-person", "smart-queue", "smart-home", "auction"),
                results.stream().map(CaseResult::name).toList());

        String structureFingerprint = sha256(results.stream()
                .map(CaseResult::structuralIdentity)
                .reduce("", (left, right) -> left + right));
        String report = json(results, structureFingerprint);
        Path output = Path.of("target", "performance", "diagram-performance.json");
        Files.createDirectories(output.getParent());
        Files.writeString(output, report, StandardCharsets.UTF_8);

        System.out.printf(Locale.ROOT,
                "DIAGRAM_PERFORMANCE_SAMPLE_OK: cases=%d warmup=%d repetitions=%d structure=%s output=%s%n",
                results.size(), WARMUP_COUNT, MEASURED_REPETITIONS, structureFingerprint,
                output.toAbsolutePath().normalize());
    }

    private static CaseResult measure(BenchmarkCase benchmarkCase) throws Exception {
        DiagramModel reference = benchmarkCase.builder().get();
        assertFalse(reference.nodes().isEmpty(), benchmarkCase.name());
        String focusNode = reference.nodes().get(0).id();
        DiagramModel referenceVisible = visible(reference, focusNode);
        String referenceSvg = svg(referenceVisible);

        for (int index = 0; index < WARMUP_COUNT; index++) {
            DiagramModel model = benchmarkCase.builder().get();
            BdiDiagramLayout.compute(model);
            DiagramModel visible = visible(model, focusNode);
            svg(visible);
        }

        List<Long> build = new ArrayList<>();
        List<Long> layout = new ArrayList<>();
        List<Long> visibleProjection = new ArrayList<>();
        List<Long> svg = new ArrayList<>();
        for (int index = 0; index < MEASURED_REPETITIONS; index++) {
            long started = System.nanoTime();
            DiagramModel model = benchmarkCase.builder().get();
            build.add(System.nanoTime() - started);
            assertEquals(reference, model, benchmarkCase.name() + " build structure");

            started = System.nanoTime();
            BdiDiagramLayout.Layout layoutSnapshot = BdiDiagramLayout.compute(model);
            layout.add(System.nanoTime() - started);
            assertEquals(model.nodes().size(), layoutSnapshot.boxes().size());

            started = System.nanoTime();
            DiagramModel visible = visible(model, focusNode);
            visibleProjection.add(System.nanoTime() - started);
            assertEquals(referenceVisible, visible, benchmarkCase.name() + " visible projection");

            started = System.nanoTime();
            String rendered = svg(visible);
            svg.add(System.nanoTime() - started);
            assertEquals(referenceSvg, rendered, benchmarkCase.name() + " SVG");
        }

        return new CaseResult(
                benchmarkCase.name(),
                reference.nodes().size(),
                reference.edges().size(),
                reference.groups().size(),
                referenceVisible.nodes().size(),
                referenceVisible.edges().size(),
                "ALL",
                List.of(),
                Stats.from(build),
                Stats.from(layout),
                Stats.from(visibleProjection),
                Stats.from(svg),
                sha256(referenceSvg));
    }

    private static DiagramModel visible(DiagramModel source, String focusNode) {
        DiagramModel mode = DiagramModeProjector.project(source, DiagramViewMode.ALL);
        return DiagramNavigationProjector.project(mode, Set.of(), Optional.of(focusNode));
    }

    private static String svg(DiagramModel visible) {
        return new DiagramSvgExporter().render(visible, Set.of(), Set.of(), Optional.empty());
    }

    private static List<BenchmarkCase> benchmarkCases() throws Exception {
        Path familyRoot = demo("family-person");
        CurrentAnalysisSnapshot family = directAnalysis(
                familyRoot.resolve("person.asl"), familyRoot.resolve("FamilyPerson.bdimap.json"));

        Path queueRoot = demo("smart-queue");
        CurrentAnalysisSnapshot queue = directAnalysis(
                queueRoot.resolve("smart_queue_manager.asl"), queueRoot.resolve("SmartQueue.bdimap.json"));

        Path homeRoot = demo("smart-home");
        MasProjectAnalysisResult home = projectAnalysis(
                homeRoot.resolve("smart-home.jcm"), homeRoot.resolve("SmartHome.bdimap.json"), Optional.empty());

        Path auctionRoot = demo("auction");
        MasProjectAnalysisResult auction = projectAnalysis(
                auctionRoot.resolve("auction.jcm"), auctionRoot.resolve("Auction.bdimap.json"),
                Optional.of(useSnapshot(auctionRoot.resolve("Auction.use"))));

        return List.of(
                new BenchmarkCase("family-person", () -> new BdiDiagramBuilder().build(family, familyRoot)),
                new BenchmarkCase("smart-queue", () -> new BdiDiagramBuilder().build(queue, queueRoot)),
                new BenchmarkCase("smart-home", () -> new BdiDiagramBuilder().build(home.snapshot(), homeRoot)),
                new BenchmarkCase("auction", () -> new BdiDiagramBuilder().build(auction.snapshot(), auctionRoot)));
    }

    private static CurrentAnalysisSnapshot directAnalysis(Path source, Path mappingFile) throws Exception {
        BdiImportSnapshot imported = new BdiImportService().importFiles(List.of(source));
        assertTrue(imported.diagnostics().isEmpty(), () -> "Unexpected import diagnostics: " + imported.diagnostics());
        MappingDocument mapping = new MappingFileRepository().load(mappingFile, source.getParent());
        return new CurrentAnalysisSnapshotService(
                new ValidationOrchestrator(), "diagram benchmark", "0.1.0", "USE-7.1.1")
                .create(FIXED_ANALYSIS_TIME, imported, Optional.empty(), Optional.empty(), mapping);
    }

    private static MasProjectAnalysisResult projectAnalysis(
            Path projectFile,
            Path mappingFile,
            Optional<UseModelSnapshot> useModel) throws Exception {
        Path root = projectFile.getParent();
        MappingDocument mapping = new MappingFileRepository().load(mappingFile, root);
        MasProjectAnalysisResult result = new MasProjectAnalysisService().analyze(MasProjectAnalysisRequest.of(
                projectFile,
                FIXED_ANALYSIS_TIME,
                useModel,
                Optional.empty(),
                mapping,
                BdiProjectConfiguration.defaults()));
        assertTrue(result.project().isPresent(), () -> "Project did not normalize: " + result.projectDiagnostics());
        return result;
    }

    private static UseModelSnapshot useSnapshot(Path specification) throws Exception {
        StringWriter errors = new StringWriter();
        MModel model;
        try (var input = Files.newInputStream(specification)) {
            model = USECompiler.compileSpecification(
                    input, specification.toString(), new PrintWriter(errors), new ModelFactory());
        }
        assertNotNull(model, errors::toString);
        model.setFilename(specification.toString());
        return new UseUmlModelFacade().snapshot(new MSystem(model));
    }

    private static String json(List<CaseResult> results, String structureFingerprint) {
        StringBuilder value = new StringBuilder(4096);
        value.append("{\n")
                .append("  \"schemaVersion\":\"1.0.0\",\n")
                .append("  \"benchmarkTimestamp\":\"").append(Instant.now()).append("\",\n")
                .append("  \"analysisTimestampPolicy\":\"fixed:2026-08-17T00:00:00Z\",\n")
                .append("  \"releaseIdentity\":\"")
                .append(escape(System.getProperty("bdi.benchmark.identity", "unrecorded"))).append("\",\n")
                .append("  \"javaVersion\":\"").append(escape(System.getProperty("java.version"))).append("\",\n")
                .append("  \"jvm\":\"").append(escape(System.getProperty("java.vm.name"))).append("\",\n")
                .append("  \"os\":\"").append(escape(System.getProperty("os.name") + " "
                        + System.getProperty("os.version") + " " + System.getProperty("os.arch"))).append("\",\n")
                .append("  \"processor\":\"").append(escape(processor())).append("\",\n")
                .append("  \"availableProcessors\":").append(Runtime.getRuntime().availableProcessors()).append(",\n")
                .append("  \"physicalMemoryMiB\":").append(physicalMemoryMiB()).append(",\n")
                .append("  \"headless\":").append(GraphicsEnvironment.isHeadless()).append(",\n")
                .append("  \"warmupCount\":").append(WARMUP_COUNT).append(",\n")
                .append("  \"measuredRepetitions\":").append(MEASURED_REPETITIONS).append(",\n")
                .append("  \"timingUnit\":\"milliseconds\",\n")
                .append("  \"structureFingerprint\":\"").append(structureFingerprint).append("\",\n")
                .append("  \"cases\":[\n");
        for (int index = 0; index < results.size(); index++) {
            if (index > 0) {
                value.append(",\n");
            }
            value.append(results.get(index).json());
        }
        return value.append("\n  ]\n}\n").toString();
    }

    private static long physicalMemoryMiB() {
        var os = ManagementFactory.getOperatingSystemMXBean();
        if (os instanceof com.sun.management.OperatingSystemMXBean extended) {
            return extended.getTotalMemorySize() / (1024L * 1024L);
        }
        return -1L;
    }

    private static String processor() {
        String value = System.getenv("PROCESSOR_IDENTIFIER");
        return value == null || value.isBlank() ? System.getProperty("os.arch") : value;
    }

    private static String sha256(String value) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
                    .digest(value.getBytes(StandardCharsets.UTF_8)));
        } catch (java.security.NoSuchAlgorithmException error) {
            throw new IllegalStateException(error);
        }
    }

    private static String escape(String value) {
        return value.replace("\\", "\\\\").replace("\"", "\\\"")
                .replace("\r", "\\r").replace("\n", "\\n");
    }

    private static Path demo(String name) {
        return repositoryRoot().resolve("use-bdi-plugin").resolve("demo").resolve(name);
    }

    private static Path repositoryRoot() {
        Path current = Path.of("").toAbsolutePath().normalize();
        while (current != null) {
            if (Files.isDirectory(current.resolve("use-bdi-plugin/demo"))) {
                return current;
            }
            current = current.getParent();
        }
        throw new IllegalStateException("Could not locate repository root from the test working directory");
    }

    private record BenchmarkCase(String name, ThrowingSupplier<DiagramModel> builder) {
    }

    @FunctionalInterface
    private interface ThrowingSupplier<T> {
        T get() throws Exception;
    }

    private record CaseResult(
            String name,
            int nodes,
            int edges,
            int groups,
            int visibleNodes,
            int visibleEdges,
            String mode,
            List<String> hiddenLayers,
            Stats build,
            Stats layout,
            Stats visibleProjection,
            Stats svg,
            String svgSha256) {

        private String structuralIdentity() {
            return String.join("|", name, Integer.toString(nodes), Integer.toString(edges),
                    Integer.toString(groups), Integer.toString(visibleNodes), Integer.toString(visibleEdges),
                    mode, String.join(",", hiddenLayers), svgSha256) + "\n";
        }

        private String json() {
            return "    {\"name\":\"" + escape(name) + "\",\"nodes\":" + nodes
                    + ",\"edges\":" + edges + ",\"groups\":" + groups
                    + ",\"visibleNodes\":" + visibleNodes + ",\"visibleEdges\":" + visibleEdges
                    + ",\"mode\":\"" + mode + "\",\"hiddenLayers\":[],\"build\":" + build.json()
                    + ",\"layout\":" + layout.json() + ",\"visibleProjection\":" + visibleProjection.json()
                    + ",\"svgRender\":" + svg.json() + ",\"svgSha256\":\"" + svgSha256 + "\"}";
        }
    }

    private record Stats(double minimum, double median, double p95, double maximum) {
        private static Stats from(List<Long> values) {
            List<Long> sorted = values.stream().sorted(Comparator.naturalOrder()).toList();
            int p95Index = Math.max(0, (int) Math.ceil(sorted.size() * 0.95) - 1);
            return new Stats(milliseconds(sorted.get(0)), milliseconds(sorted.get(sorted.size() / 2)),
                    milliseconds(sorted.get(p95Index)), milliseconds(sorted.get(sorted.size() - 1)));
        }

        private String json() {
            return String.format(Locale.ROOT,
                    "{\"minimum\":%.6f,\"median\":%.6f,\"p95\":%.6f,\"maximum\":%.6f}",
                    minimum, median, p95, maximum);
        }

        private static double milliseconds(long nanos) {
            return nanos / 1_000_000.0;
        }
    }
}

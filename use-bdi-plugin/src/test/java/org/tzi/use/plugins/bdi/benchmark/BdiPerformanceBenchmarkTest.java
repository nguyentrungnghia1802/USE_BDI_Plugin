package org.tzi.use.plugins.bdi.benchmark;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.tzi.use.plugins.bdi.application.BdiImportService;
import org.tzi.use.plugins.bdi.application.BdiImportSnapshot;
import org.tzi.use.plugins.bdi.index.BdiIndex;
import org.tzi.use.plugins.bdi.model.ir.AgentModel;

/**
 * Small reproducible baseline for the import -> IR -> BDI index pipeline.
 *
 * <p>This is intentionally a measurement test rather than a timing gate. The
 * result is useful for comparing environments, while a hard duration limit
 * would make the build flaky on shared or slower machines.</p>
 */
@Tag("performance")
class BdiPerformanceBenchmarkTest {
    private static final String FIXTURE = "fixtures/smartqueue/Smart_manager_agent.asl";
    private static final int WARMUP_ITERATIONS = 2;
    private static final int MEASURED_ITERATIONS = 7;

    @Test
    void measuresSmartQueueImportAndIndexBaseline() throws Exception {
        Path source = fixture(FIXTURE);
        List<Path> sources = List.of(source);
        BdiImportService service = new BdiImportService();

        for (int iteration = 0; iteration < WARMUP_ITERATIONS; iteration++) {
            verifyWorkload(service.importFiles(sources));
        }

        List<Long> durations = new ArrayList<>(MEASURED_ITERATIONS);
        BdiImportSnapshot lastSnapshot = null;
        for (int iteration = 0; iteration < MEASURED_ITERATIONS; iteration++) {
            long start = System.nanoTime();
            lastSnapshot = service.importFiles(sources);
            long elapsed = System.nanoTime() - start;
            assertTrue(elapsed > 0, "benchmark clock did not advance");
            verifyWorkload(lastSnapshot);
            durations.add(elapsed);
        }

        BenchmarkResult result = BenchmarkResult.from(source, durations, lastSnapshot);
        Path output = Path.of("target", "performance", "bdi-import-index.json");
        Files.createDirectories(output.getParent());
        Files.writeString(output, result.toJson(), StandardCharsets.UTF_8);

        assertEquals(MEASURED_ITERATIONS, result.durationsNanos().size());
        assertTrue(result.medianNanos() >= result.minNanos());
        assertTrue(result.p95Nanos() >= result.medianNanos());
        assertTrue(Files.size(output) > 0, "benchmark report is empty");
        System.out.printf(
                "PERFORMANCE_BENCHMARK_OK: fixture=%s median=%.3f ms p95=%.3f ms output=%s%n",
                source.getFileName(),
                result.medianNanos() / 1_000_000.0,
                result.p95Nanos() / 1_000_000.0,
                output.toAbsolutePath());
    }

    private static void verifyWorkload(BdiImportSnapshot snapshot) {
        assertFalse(snapshot.hasErrors(), () -> "unexpected import diagnostics: " + snapshot.diagnostics());
        assertEquals(1, snapshot.fileCount());
        assertEquals(1, snapshot.models().size());

        AgentModel model = snapshot.models().get(0);
        assertEquals(9, model.beliefCount());
        assertEquals(1, model.goalCount());
        assertEquals(5, model.planCount());
        assertTrue(model.isMaterialized());
        assertTrue(model.unsupportedFeatures().isEmpty());

        BdiIndex index = snapshot.index();
        assertEquals(1, index.models().size());
        assertFalse(index.allActionCallSites().isEmpty());
        assertFalse(index.allPredicateReferences().isEmpty());
        assertFalse(index.agentReferencesByName().isEmpty());
        assertFalse(index.objectReferencesByName().isEmpty());
        assertTrue(index.duplicatePlanLabels().isEmpty());
    }

    private static Path fixture(String name) throws URISyntaxException {
        URL resource = BdiPerformanceBenchmarkTest.class.getClassLoader().getResource(name);
        if (resource == null) {
            throw new IllegalStateException("Missing benchmark fixture: " + name);
        }
        return Path.of(resource.toURI());
    }

    private record BenchmarkResult(
            Path source,
            String parserVersion,
            String metamodelVersion,
            int beliefCount,
            int goalCount,
            int planCount,
            List<Long> durationsNanos,
            long minNanos,
            long medianNanos,
            long p95Nanos,
            int warmupIterations,
            int measuredIterations) {
        private static BenchmarkResult from(Path source, List<Long> durations, BdiImportSnapshot snapshot) {
            List<Long> ordered = durations.stream().sorted().toList();
            AgentModel model = snapshot.models().get(0);
            return new BenchmarkResult(
                    source,
                    model.parserVersion(),
                    snapshot.index().metamodelVersion(),
                    model.beliefCount(),
                    model.goalCount(),
                    model.planCount(),
                    List.copyOf(durations),
                    ordered.get(0),
                    percentile(ordered, 0.50),
                    percentile(ordered, 0.95),
                    WARMUP_ITERATIONS,
                    MEASURED_ITERATIONS);
        }

        private static long percentile(List<Long> ordered, double percentile) {
            int index = (int) Math.ceil(percentile * ordered.size()) - 1;
            return ordered.get(Math.max(0, Math.min(index, ordered.size() - 1)));
        }

        private String toJson() {
            String relativeSource = source.getFileName().toString();
            String durations = durationsNanos.stream()
                    .map(String::valueOf)
                    .collect(Collectors.joining(","));
            return "{\n"
                    + "  \"benchmark\": \"bdi-import-index\",\n"
                    + "  \"source\": \"" + escape(relativeSource) + "\",\n"
                    + "  \"parserVersion\": \"" + escape(parserVersion) + "\",\n"
                    + "  \"bdiMetamodelVersion\": \"" + escape(metamodelVersion) + "\",\n"
                    + "  \"workload\": {\"beliefs\": " + beliefCount
                    + ", \"goals\": " + goalCount
                    + ", \"plans\": " + planCount + "},\n"
                    + "  \"warmupIterations\": " + warmupIterations + ",\n"
                    + "  \"measuredIterations\": " + measuredIterations + ",\n"
                    + "  \"durationsNanos\": [" + durations + "],\n"
                    + "  \"minNanos\": " + minNanos + ",\n"
                    + "  \"medianNanos\": " + medianNanos + ",\n"
                    + "  \"p95Nanos\": " + p95Nanos + "\n"
                    + "}\n";
        }

        private static String escape(String value) {
            return value.replace("\\", "\\\\").replace("\"", "\\\"");
        }
    }
}

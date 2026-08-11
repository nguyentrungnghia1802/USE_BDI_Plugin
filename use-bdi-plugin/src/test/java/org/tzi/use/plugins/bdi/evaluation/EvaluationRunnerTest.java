package org.tzi.use.plugins.bdi.evaluation;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.tzi.use.plugins.bdi.validation.IssueCertainty;

class EvaluationRunnerTest {
    @TempDir
    Path tempDir;

    @Test
    void runsRealHeadlessServiceInTemporaryWorkspaceAndProducesStableReports() throws Exception {
        Path fixtureRoot = fixture("fixtures/casestudy/auction/Auction.use").getParent();
        Path use = fixtureRoot.resolve("Auction.use");
        Path auctioneer = fixtureRoot.resolve("auctioneer.asl");
        Path bidder = fixtureRoot.resolve("bidder.asl");
        byte[] useBefore = Files.readAllBytes(use);
        byte[] auctioneerBefore = Files.readAllBytes(auctioneer);
        EvaluationManifest manifest = new EvaluationManifest(
                EvaluationManifest.CURRENT_SCHEMA_VERSION,
                "Auction",
                "0.1.0",
                "USE-7.1.1",
                "standard",
                List.of("MOISE_ORGANIZATION_IR"),
                List.of(new EvaluationManifest.EvaluationCase(
                        "baseline",
                        "BASELINE",
                        "BDI_UML_OCL",
                        "Auction.use",
                        List.of("auctioneer.asl", "bidder.asl"),
                        Optional.empty(),
                        Optional.empty(),
                        List.of("MAP-001"),
                        List.of(),
                        Map.of("MAP-001", IssueCertainty.CONFIRMED),
                        List.of("Auction.use"),
                        List.of(),
                        Duration.ofSeconds(30))));

        EvaluationRunResult first = new EvaluationRunner().run(
                manifest, fixtureRoot, Instant.parse("2026-08-11T00:00:00Z"));
        EvaluationRunResult second = new EvaluationRunner().run(
                manifest, fixtureRoot, Instant.parse("2026-08-11T00:00:00Z"));

        assertEquals(EvaluationStatus.DETECTED, first.cases().get(0).status());
        assertEquals(1, first.metrics().detected());
        assertEquals(EvaluationReportWriter.json(first), EvaluationReportWriter.json(second));
        assertArrayEquals(useBefore, Files.readAllBytes(use));
        assertArrayEquals(auctioneerBefore, Files.readAllBytes(auctioneer));
        EvaluationReportWriter.write(tempDir, first);
        assertTrue(Files.isRegularFile(tempDir.resolve("evaluation-results.json")));
        assertTrue(Files.readString(tempDir.resolve("evaluation-results.csv")).contains("baseline"));
        assertFalse(Files.readString(tempDir.resolve("evaluation-results.json")).contains("use-bdi-evaluation-"));
    }

    @Test
    void separatesTimeoutAndExecutionErrorFromSemanticOutcomes() throws Exception {
        EvaluationManifest manifest = simpleManifest(Duration.ofSeconds(1));
        EvaluationRunner timeoutRunner = new EvaluationRunner(request -> {
            TimeUnit.SECONDS.sleep(2);
            return null;
        });
        EvaluationRunResult timeout = timeoutRunner.run(manifest, fixtureRoot(), Instant.EPOCH);
        assertEquals(EvaluationStatus.TIMEOUT, timeout.cases().get(0).status());

        EvaluationRunner errorRunner = new EvaluationRunner(request -> {
            throw new IllegalStateException("synthetic tool failure");
        });
        EvaluationRunResult error = errorRunner.run(manifest, fixtureRoot(), Instant.EPOCH);
        assertEquals(EvaluationStatus.EXECUTION_ERROR, error.cases().get(0).status());
        assertTrue(error.cases().get(0).diagnostic().contains("synthetic tool failure"));
    }

    @Test
    void rejectsMissingManifestInputsBeforeRunningAnyCase() {
        EvaluationManifest manifest = new EvaluationManifest(
                EvaluationManifest.CURRENT_SCHEMA_VERSION, "Auction", "0.1.0", "USE-7.1.1", "standard",
                List.of(), List.of(new EvaluationManifest.EvaluationCase(
                        "missing", "BASELINE", "BDI_UML_OCL", "missing.use", List.of("agent.asl"),
                        Optional.empty(), Optional.empty(), List.of("MAP-001"), List.of(),
                        Map.of("MAP-001", IssueCertainty.CONFIRMED), List.of(), List.of(), Duration.ofSeconds(5))));
        assertThrows(IllegalArgumentException.class, () -> new EvaluationRunner().run(manifest, fixtureRoot(), Instant.EPOCH));
    }

    @Test
    void runsTheReviewedAuctionManifestAndKeepsMoiseAndLiveCartagoOutOfScope() throws Exception {
        Path root = Path.of("").toAbsolutePath().normalize().getParent();
        Path manifestFile = root.resolve("docs/project/evidence/auction-evaluation-manifest.json");
        EvaluationManifest manifest = EvaluationManifestCodec.load(manifestFile);

        EvaluationRunResult result = new EvaluationRunner().run(
                manifest, root, Instant.parse("2026-08-11T00:00:00Z"));

        assertEquals(1, result.metrics().passed(), result.cases()::toString);
        assertEquals(4, result.metrics().detected(), result.cases()::toString);
        assertEquals(0, result.metrics().missed());
        assertEquals(0, result.metrics().unknown());
        assertEquals(List.of("LIVE_CARTAGO", "MOISE_ORGANIZATION_IR"), manifest.excludedLayers());
        EvaluationReportWriter.write(tempDir, result);
        assertTrue(Files.readString(tempDir.resolve("evaluation-results.json")).contains("\"detected\":4"));
    }

    private Path fixtureRoot() {
        try {
            return fixture("fixtures/casestudy/auction/Auction.use").getParent();
        } catch (Exception error) {
            throw new IllegalStateException(error);
        }
    }

    private static EvaluationManifest simpleManifest(Duration timeout) {
        return new EvaluationManifest(
                EvaluationManifest.CURRENT_SCHEMA_VERSION, "Auction", "0.1.0", "USE-7.1.1", "standard",
                List.of(), List.of(new EvaluationManifest.EvaluationCase(
                        "synthetic", "BASELINE", "BDI_UML_OCL", "Auction.use", List.of("auctioneer.asl"),
                        Optional.empty(), Optional.empty(), List.of("MAP-001"), List.of(),
                        Map.of("MAP-001", IssueCertainty.CONFIRMED), List.of(), List.of(), timeout)));
    }

    private static Path fixture(String name) throws Exception {
        URL resource = EvaluationRunnerTest.class.getClassLoader().getResource(name);
        if (resource == null) {
            throw new IllegalStateException("Missing fixture: " + name);
        }
        return Path.of(resource.toURI());
    }
}

package org.tzi.use.plugins.bdi.cli;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.tzi.use.plugins.bdi.persistence.RuleConfigurationRepository;
import org.tzi.use.plugins.bdi.validation.RuleConfiguration;

class BdiQualityGateMainTest {
    @TempDir
    Path tempDir;

    @Test
    void auctionBaselineProducesDeterministicRealReportsWithoutMutatingInputs() throws Exception {
        Path use = fixture("fixtures/casestudy/auction/Auction.use");
        Path auctioneer = fixture("fixtures/casestudy/auction/auctioneer.asl");
        Path bidder = fixture("fixtures/casestudy/auction/bidder.asl");
        byte[] useBefore = Files.readAllBytes(use);
        byte[] auctioneerBefore = Files.readAllBytes(auctioneer);
        Path json = tempDir.resolve("auction.json");
        Path html = tempDir.resolve("auction.html");
        Invocation first = invoke(
                "--use", use.toString(),
                "--asl", auctioneer.toString(),
                "--asl", bidder.toString(),
                "--json", json.toString(),
                "--html", html.toString(),
                "--timestamp", "2026-08-10T00:00:00Z");

        assertEquals(HeadlessExitCode.CONFIRMED_FINDINGS.code(), first.exitCode());
        assertTrue(first.stdout().contains("BDI_QUALITY_GATE_RESULT=CONFIRMED_FINDINGS"));
        assertTrue(Files.readString(json).contains("\"issues\":[{"));
        assertTrue(Files.readString(json).contains("MAP-001"));
        assertTrue(Files.readString(html).contains("Consistency Issues"));
        assertArrayEquals(useBefore, Files.readAllBytes(use));
        assertArrayEquals(auctioneerBefore, Files.readAllBytes(auctioneer));

        Path jsonRepeat = tempDir.resolve("auction-repeat.json");
        Invocation second = invoke(
                "--use", use.toString(),
                "--asl", auctioneer.toString(),
                "--asl", bidder.toString(),
                "--json", jsonRepeat.toString(),
                "--timestamp", "2026-08-10T00:00:00Z");
        assertEquals(first.exitCode(), second.exitCode());
        assertEquals(Files.readString(json), Files.readString(jsonRepeat));
    }

    @Test
    void distinguishesInvalidAslMissingInputAndInvalidConfiguration() throws Exception {
        Path use = fixture("fixtures/casestudy/auction/Auction.use");
        Path invalidAsl = fixture("fixtures/asl/invalid/missing-plan-body.asl");
        Path aslRules = rules("asl-only.json", "ASL-001");
        Path invalidReport = tempDir.resolve("invalid-asl.json");

        Invocation syntax = invoke(
                "--use", use.toString(),
                "--asl", invalidAsl.toString(),
                "--rules", aslRules.toString(),
                "--json", invalidReport.toString());
        assertEquals(HeadlessExitCode.CONFIRMED_FINDINGS.code(), syntax.exitCode());
        assertTrue(Files.readString(invalidReport).contains("ASL-001"));
        assertTrue(syntax.stdout().contains("parser"));

        Path missingReport = tempDir.resolve("missing.json");
        Path missing = tempDir.resolve("missing.asl");
        Invocation missingInput = invoke(
                "--use", use.toString(),
                "--asl", missing.toString(),
                "--json", missingReport.toString());
        assertEquals(HeadlessExitCode.INVALID_INPUT.code(), missingInput.exitCode());
        assertTrue(missingInput.stderr().contains("Missing AgentSpeak source file"));
        assertTrue(missingInput.stderr().contains(missing.toString()));
        assertFalse(Files.exists(missingReport));

        Path badRules = Files.writeString(tempDir.resolve("bad-rules.json"), "{not-json}");
        Path configReport = tempDir.resolve("bad-config.json");
        Invocation invalidConfig = invoke(
                "--use", use.toString(),
                "--asl", invalidAsl.toString(),
                "--rules", badRules.toString(),
                "--json", configReport.toString());
        assertEquals(HeadlessExitCode.INVALID_INPUT.code(), invalidConfig.exitCode());
        assertTrue(invalidConfig.stderr().contains("Invalid rule configuration"));
        assertTrue(invalidConfig.stderr().contains(badRules.toString()));
        assertFalse(Files.exists(configReport));
    }

    @Test
    void reportsPotentialOrUnknownFindingsAsReviewInsteadOfPassOrCrash() throws Exception {
        Path use = fixture("fixtures/casestudy/auction/Auction.use");
        Path reviewSource = fixture("fixtures/asl/valid/minimal.asl");
        Path rules = rules("potential-only.json", "BEL-001");
        Path report = tempDir.resolve("review.json");

        Invocation result = invoke(
                "--use", use.toString(),
                "--asl", reviewSource.toString(),
                "--rules", rules.toString(),
                "--json", report.toString());

        assertEquals(HeadlessExitCode.REVIEW_FINDINGS.code(), result.exitCode());
        assertTrue(result.stdout().contains("BDI_QUALITY_GATE_RESULT=REVIEW_FINDINGS"));
        assertTrue(result.stdout().contains("BEL-001"));
        assertTrue(Files.readString(report).contains("\"certainty\":\"POTENTIAL\""));
        assertFalse(result.stderr().contains("INFRASTRUCTURE"));
    }

    @Test
    void helpAndOutputFailureUseTheirDocumentedExitCodes() throws Exception {
        Invocation help = invoke("--help");
        assertEquals(HeadlessExitCode.CLEAN.code(), help.exitCode());
        assertTrue(help.stdout().contains("Exit: 0 clean"));

        Path use = fixture("fixtures/casestudy/auction/Auction.use");
        Path asl = fixture("fixtures/casestudy/auction/auctioneer.asl");
        Path parentFile = Files.writeString(tempDir.resolve("not-a-directory"), "keep");
        Invocation failure = invoke(
                "--use", use.toString(),
                "--asl", asl.toString(),
                "--json", parentFile.resolve("report.json").toString());
        assertEquals(HeadlessExitCode.INFRASTRUCTURE_FAILURE.code(), failure.exitCode());
        assertTrue(failure.stderr().contains("BDI_QUALITY_GATE_INFRASTRUCTURE_ERROR"));

        Path jcm = Files.writeString(tempDir.resolve("unsupported.jcm"), "mas unsupported {}");
        Invocation unsupported = invoke(
                "--use", use.toString(),
                "--asl", jcm.toString(),
                "--json", tempDir.resolve("jcm.json").toString());
        assertEquals(HeadlessExitCode.INVALID_INPUT.code(), unsupported.exitCode());
        assertTrue(unsupported.stderr().contains("must use .asl"));
    }

    private Path rules(String filename, String... ids) throws Exception {
        Path file = tempDir.resolve(filename);
        new RuleConfigurationRepository().save(file, RuleConfiguration.of(List.of(ids)));
        return file;
    }

    private static Invocation invoke(String... args) {
        ByteArrayOutputStream stdout = new ByteArrayOutputStream();
        ByteArrayOutputStream stderr = new ByteArrayOutputStream();
        int exit = BdiQualityGateMain.run(
                args,
                new PrintStream(stdout, true, StandardCharsets.UTF_8),
                new PrintStream(stderr, true, StandardCharsets.UTF_8));
        return new Invocation(
                exit,
                stdout.toString(StandardCharsets.UTF_8),
                stderr.toString(StandardCharsets.UTF_8));
    }

    private static Path fixture(String name) throws Exception {
        URL resource = BdiQualityGateMainTest.class.getClassLoader().getResource(name);
        if (resource == null) {
            throw new IllegalStateException("Missing fixture: " + name);
        }
        return Path.of(resource.toURI());
    }

    private record Invocation(int exitCode, String stdout, String stderr) {
    }
}

package org.tzi.use.plugins.bdi.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.tzi.use.plugins.bdi.importer.MasProjectDiagnostic;
import org.tzi.use.plugins.bdi.model.mapping.MappingDocument;
import org.tzi.use.plugins.bdi.model.mas.MasProjectModelJsonSerializer;
import org.tzi.use.plugins.bdi.model.source.ProjectSourceId;

class MasProjectAnalysisServiceTest {
    private static final Instant FIXED_TIME = Instant.parse("2026-08-11T09:00:00Z");

    @Test
    void composesAuctionProjectIntoTheSharedImmutableSnapshot() throws Exception {
        Path projectFile = fixture("fixtures/casestudy/auction/auction.jcm");
        MasProjectAnalysisResult result = new MasProjectAnalysisService().analyze(
                MasProjectAnalysisRequest.of(
                        projectFile,
                        FIXED_TIME,
                        Optional.empty(),
                        Optional.empty(),
                        MappingDocument.empty("unknown"),
                        BdiProjectConfiguration.defaults()));

        assertTrue(result.project().isPresent());
        assertEquals(List.of("auctioneer", "bidder1", "bidder2"),
                result.project().orElseThrow().agents().stream().map(agent -> agent.name()).toList());
        assertEquals(2, result.snapshot().bdiImport().models().size());
        assertEquals(2, result.snapshot().importedFileCount());
        assertEquals(FIXED_TIME, result.snapshot().timestamp());
        assertEquals(2, result.projectDiagnostics().size());
        assertTrue(result.projectDiagnostics().stream()
                .allMatch(item -> item.code().equals(MasProjectDiagnostic.UNSUPPORTED_RESOURCE)));
    }

    @Test
    void preservesValidProjectAnalysisWhenOneAgentSourceIsInvalidOrMissing(@TempDir Path root)
            throws IOException, URISyntaxException {
        Files.copy(fixture("fixtures/asl/valid/minimal.asl"), root.resolve("valid.asl"));
        Files.copy(fixture("fixtures/asl/invalid/missing-plan-body.asl"), root.resolve("invalid.asl"));
        Path projectFile = root.resolve("partial.jcm");
        Files.writeString(projectFile, """
                mas partial {
                    agent valid : valid.asl
                    agent invalid : invalid.asl
                    agent missing : missing.asl
                }
                """);

        MasProjectAnalysisResult result = new MasProjectAnalysisService().analyze(
                MasProjectAnalysisRequest.of(
                        projectFile,
                        FIXED_TIME,
                        Optional.empty(),
                        Optional.empty(),
                        MappingDocument.empty("unknown"),
                        BdiProjectConfiguration.defaults()));

        assertTrue(result.project().isPresent());
        assertEquals(1, result.snapshot().bdiImport().models().size());
        assertTrue(result.hasErrors());
        assertEquals(List.of(
                MasProjectDiagnostic.MISSING_AGENT_SOURCE,
                MasProjectDiagnostic.INVALID_AGENT_SOURCE),
                result.projectDiagnostics().stream()
                        .map(MasProjectDiagnostic::code)
                        .sorted()
                        .toList());
    }

    @Test
    void projectSourceIdsRemainPortableAfterRelocation(@TempDir Path root) throws Exception {
        Path first = root.resolve("first");
        Path second = root.resolve("second");
        copyAuction(first);
        copyAuction(second);

        MasProjectAnalysisService service = new MasProjectAnalysisService();
        MasProjectAnalysisResult firstResult = service.analyze(request(first.resolve("auction.jcm")));
        MasProjectAnalysisResult secondResult = service.analyze(request(second.resolve("auction.jcm")));
        MasProjectModelJsonSerializer serializer = new MasProjectModelJsonSerializer();

        assertEquals(
                serializer.serialize(firstResult.project().orElseThrow()),
                serializer.serialize(secondResult.project().orElseThrow()));
        assertEquals(
                ProjectSourceId.fromPath(first, first.resolve("auction.jcm")),
                firstResult.project().orElseThrow().source());
        assertFalse(serializer.serialize(firstResult.project().orElseThrow()).contains(root.toString()));
    }

    private static MasProjectAnalysisRequest request(Path projectFile) {
        return MasProjectAnalysisRequest.of(
                projectFile,
                FIXED_TIME,
                Optional.empty(),
                Optional.empty(),
                MappingDocument.empty("unknown"),
                BdiProjectConfiguration.defaults());
    }

    private static void copyAuction(Path target) throws IOException, URISyntaxException {
        Files.createDirectories(target);
        for (String file : List.of("auction.jcm", "auctioneer.asl", "bidder.asl")) {
            Files.copy(fixture("fixtures/casestudy/auction/" + file), target.resolve(file));
        }
    }

    private static Path fixture(String name) throws URISyntaxException {
        URL resource = MasProjectAnalysisServiceTest.class.getClassLoader().getResource(name);
        if (resource == null) {
            throw new IllegalStateException("Missing test fixture: " + name);
        }
        return Path.of(resource.toURI());
    }
}

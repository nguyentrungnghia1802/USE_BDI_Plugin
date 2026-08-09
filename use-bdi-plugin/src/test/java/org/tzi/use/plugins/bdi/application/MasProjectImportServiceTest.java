package org.tzi.use.plugins.bdi.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.tzi.use.plugins.bdi.importer.MasProjectDiagnostic;
import org.tzi.use.plugins.bdi.model.mas.MasAgentImportStatus;
import org.tzi.use.plugins.bdi.model.mas.MasProjectModel;
import org.tzi.use.plugins.bdi.model.mas.MasProjectModelJsonSerializer;

class MasProjectImportServiceTest {
    private final MasProjectImportService service = new MasProjectImportService();
    private final MasProjectModelJsonSerializer serializer = new MasProjectModelJsonSerializer();

    @Test
    void importsAuctionInstancesResourcesAndGoldenPortableIr() throws Exception {
        Path projectFile = fixture("fixtures/casestudy/auction/auction.jcm");

        MasProjectImportResult result = service.importProject(projectFile);
        MasProjectModel project = result.project().orElseThrow();

        assertEquals(List.of("auctioneer", "bidder1", "bidder2"),
                project.agents().stream().map(agent -> agent.name()).toList());
        assertTrue(project.agents().stream()
                .allMatch(agent -> agent.status() == MasAgentImportStatus.IMPORTED));
        assertEquals(2, result.bdiSnapshot().models().size());
        assertEquals(3, project.resources().size());
        assertEquals(List.of(
                MasProjectDiagnostic.UNSUPPORTED_RESOURCE,
                MasProjectDiagnostic.UNSUPPORTED_RESOURCE,
                MasProjectDiagnostic.UNSUPPORTED_RESOURCE),
                result.diagnostics().stream().map(MasProjectDiagnostic::code).toList());
        assertEquals(
                Files.readString(fixture("fixtures/expected/auction-mas-project.json"), StandardCharsets.UTF_8)
                        .replace("\r\n", "\n"),
                serializer.serialize(project));
    }

    @Test
    void invalidProjectReturnsPositionedParserDiagnostic() throws Exception {
        MasProjectImportResult result = service.importProject(
                fixture("fixtures/jcm/invalid/invalid-syntax.jcm"));

        assertTrue(result.project().isEmpty());
        assertEquals(MasProjectDiagnostic.PARSE_ERROR, result.diagnostics().get(0).code());
        assertTrue(result.diagnostics().get(0).line() > 0);
        assertTrue(result.hasErrors());
    }

    @Test
    void preservesValidAgentWhenOtherSourcesAreInvalidMissingAndDuplicate(@TempDir Path root)
            throws Exception {
        Files.copy(fixture("fixtures/asl/valid/minimal.asl"), root.resolve("valid.asl"));
        Files.copy(fixture("fixtures/asl/invalid/missing-plan-body.asl"), root.resolve("invalid.asl"));
        Path projectFile = root.resolve("partial.jcm");
        Files.writeString(projectFile, """
                mas partial {
                    agent valid : valid.asl
                    agent invalid : invalid.asl
                    agent missing : missing.asl
                    agent valid : valid.asl
                }
                """, StandardCharsets.UTF_8);

        MasProjectImportResult result = service.importProject(root, projectFile);
        MasProjectModel project = result.project().orElseThrow();

        assertEquals(List.of("valid", "invalid", "missing"),
                project.agents().stream().map(agent -> agent.name()).toList());
        assertEquals(List.of(
                MasAgentImportStatus.IMPORTED,
                MasAgentImportStatus.INVALID,
                MasAgentImportStatus.MISSING),
                project.agents().stream().map(agent -> agent.status()).toList());
        assertEquals(1, result.bdiSnapshot().models().size());
        assertEquals(1, result.bdiSnapshot().diagnostics().size());
        assertEquals(List.of(
                MasProjectDiagnostic.DUPLICATE_AGENT,
                MasProjectDiagnostic.MISSING_AGENT_SOURCE,
                MasProjectDiagnostic.INVALID_AGENT_SOURCE),
                result.diagnostics().stream().map(MasProjectDiagnostic::code).toList());
    }

    @Test
    void copiedRelativeProjectProducesSamePortableIr(@TempDir Path temporaryRoot) throws Exception {
        Path checkoutA = temporaryRoot.resolve("checkout-a");
        Path checkoutB = temporaryRoot.resolve("checkout-b");
        copyAuction(checkoutA);
        copyAuction(checkoutB);

        MasProjectImportResult first = service.importProject(checkoutA.resolve("auction.jcm"));
        MasProjectImportResult second = service.importProject(checkoutB.resolve("auction.jcm"));

        assertEquals(serializer.serialize(first.project().orElseThrow()),
                serializer.serialize(second.project().orElseThrow()));
        assertEquals(2, first.bdiSnapshot().models().size());
        assertEquals(2, second.bdiSnapshot().models().size());
        assertFalse(serializer.serialize(first.project().orElseThrow()).contains(temporaryRoot.toString()));
    }

    @Test
    void directMultiAslImportStillPreservesPartialSuccess() throws Exception {
        BdiImportSnapshot snapshot = new BdiImportService().importFiles(List.of(
                fixture("fixtures/asl/valid/minimal.asl"),
                fixture("fixtures/asl/invalid/missing-plan-body.asl"),
                fixture("fixtures/asl/valid/review-agent.asl")));

        assertEquals(2, snapshot.models().size());
        assertEquals(1, snapshot.diagnostics().size());
    }

    private static void copyAuction(Path target) throws IOException, URISyntaxException {
        Files.createDirectories(target);
        for (String file : List.of("auction.jcm", "auctioneer.asl", "bidder.asl")) {
            Files.copy(fixture("fixtures/casestudy/auction/" + file), target.resolve(file));
        }
    }

    private static Path fixture(String name) throws URISyntaxException {
        URL resource = MasProjectImportServiceTest.class.getClassLoader().getResource(name);
        if (resource == null) {
            throw new IllegalStateException("Missing test fixture: " + name);
        }
        return Path.of(resource.toURI());
    }
}

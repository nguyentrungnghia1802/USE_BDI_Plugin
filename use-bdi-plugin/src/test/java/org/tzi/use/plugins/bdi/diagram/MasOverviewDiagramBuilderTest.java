package org.tzi.use.plugins.bdi.diagram;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.tzi.use.plugins.bdi.application.BdiProjectConfiguration;
import org.tzi.use.plugins.bdi.application.MasProjectAnalysisRequest;
import org.tzi.use.plugins.bdi.application.MasProjectAnalysisResult;
import org.tzi.use.plugins.bdi.application.MasProjectAnalysisService;
import org.tzi.use.plugins.bdi.model.environment.ArtifactModel;
import org.tzi.use.plugins.bdi.model.environment.EnvironmentMapping;
import org.tzi.use.plugins.bdi.model.environment.EnvironmentModel;
import org.tzi.use.plugins.bdi.model.environment.EnvironmentOperation;
import org.tzi.use.plugins.bdi.model.environment.EnvironmentOperationMapping;
import org.tzi.use.plugins.bdi.model.mapping.MappingDocument;
import org.tzi.use.plugins.bdi.model.organization.OrganizationMapping;
import org.tzi.use.plugins.bdi.model.organization.OrganizationMappingConfirmation;
import org.tzi.use.plugins.bdi.model.organization.OrganizationMissionMapping;
import org.tzi.use.plugins.bdi.model.organization.OrganizationModel;
import org.tzi.use.plugins.bdi.model.organization.OrganizationRoleMapping;

class MasOverviewDiagramBuilderTest {
    private static final Instant FIXED_TIME = Instant.parse("2026-08-13T00:00:00Z");

    @Test
    void projectsAuctionInstancesOrganizationAndStaticOnlyResourcesDeterministically() throws Exception {
        MasProjectAnalysisResult analysis = auctionAnalysis();
        MasOverviewDiagramBuilder builder = new MasOverviewDiagramBuilder();

        DiagramModel first = builder.build(
                analysis.project().orElseThrow(), analysis.snapshot(), fixtureRoot());
        DiagramModel second = builder.build(
                analysis.project().orElseThrow(), analysis.snapshot(), fixtureRoot());

        assertEquals(first, second);
        assertEquals(3, first.nodes().stream().filter(node -> node.type() == DiagramNodeType.AGENT).count());
        assertEquals(1, first.nodes().stream().filter(node -> node.type() == DiagramNodeType.ORGANIZATION
                && node.label().startsWith("Organization:")).count());
        assertTrue(first.nodes().stream().anyMatch(node -> node.type() == DiagramNodeType.ROLE));
        assertTrue(first.nodes().stream().anyMatch(node -> node.type() == DiagramNodeType.MISSION));
        assertTrue(first.nodes().stream().anyMatch(node -> node.label().contains("WORKSPACE")
                && node.attributes().get("resourceStatus").equals("UNSUPPORTED")));
        assertTrue(first.nodes().stream().anyMatch(node -> node.label().startsWith("Static analysis only")));
        assertTrue(first.groups().get(0).attributes().get("staticOnly").equals("true"));
    }

    @Test
    void connectsConfirmedOrganizationAndEnvironmentMappingsToUmlTargets() throws Exception {
        MasProjectAnalysisResult analysis = auctionAnalysis();
        OrganizationModel organization = analysis.project().orElseThrow().organizations().get(0);
        OrganizationModel.Role role = organization.roles().get(0);
        OrganizationModel.Mission mission = organization.schemes().get(0).missions().get(0);
        List<OrganizationMapping> organizationMappings = List.of(
                new OrganizationRoleMapping(role.qualifiedId(), "Auctioneer", OrganizationMappingConfirmation.CONFIRMED,
                        List.of("reviewed role mapping")),
                new OrganizationMissionMapping(mission.qualifiedId(), "Auctioneer::openAuction()",
                        OrganizationMappingConfirmation.CONFIRMED, List.of("reviewed mission mapping")));
        ArtifactModel artifact = new ArtifactModel(
                "auction_environment", "auctionArtifact", "AuctionArtifact",
                List.of(new EnvironmentOperation("open", 0, List.of(), "")), List.of());
        EnvironmentModel environment = new EnvironmentModel(List.of(artifact));
        EnvironmentMapping environmentMapping = new EnvironmentOperationMapping(
                "openAuction", 0, "auction_environment", "auctionArtifact", "open", "Auctioneer::openAuction()");
        EnvironmentMapping missingEnvironmentMapping = new EnvironmentOperationMapping(
                "missingAction", 0, "auction_environment", "auctionArtifact", "missing", "Auctioneer::missing()");

        DiagramModel overview = new MasOverviewDiagramBuilder().build(
                analysis.project().orElseThrow(), analysis.snapshot(), fixtureRoot(), Optional.of(environment),
                organizationMappings, List.of(environmentMapping, missingEnvironmentMapping));

        assertTrue(overview.nodes().stream().anyMatch(node -> node.type() == DiagramNodeType.UML_CLASS
                && node.label().equals("Auctioneer")));
        assertTrue(overview.nodes().stream().anyMatch(node -> node.type() == DiagramNodeType.UML_OPERATION
                && node.label().equals("Auctioneer::openAuction()")));
        assertTrue(overview.nodes().stream().anyMatch(node -> node.type() == DiagramNodeType.ARTIFACT_OPERATION));
        assertTrue(overview.edges().stream().filter(edge -> edge.type() == DiagramEdgeType.MAPS_TO).count() >= 3);
        assertTrue(overview.nodes().stream().anyMatch(node -> node.type() == DiagramNodeType.GAP
                && node.label().contains("Missing environment operation")));
    }

    private static MasProjectAnalysisResult auctionAnalysis() throws Exception {
        Path projectFile = fixture("fixtures/casestudy/auction/auction.jcm");
        return new MasProjectAnalysisService().analyze(MasProjectAnalysisRequest.of(
                projectFile,
                FIXED_TIME,
                Optional.empty(),
                Optional.empty(),
                MappingDocument.empty("unknown"),
                BdiProjectConfiguration.defaults()));
    }

    private static Path fixtureRoot() throws URISyntaxException {
        return fixture("fixtures/casestudy/auction/auction.jcm").getParent();
    }

    private static Path fixture(String name) throws URISyntaxException {
        URL resource = MasOverviewDiagramBuilderTest.class.getClassLoader().getResource(name);
        if (resource == null) {
            throw new IllegalStateException("Missing fixture: " + name);
        }
        return Path.of(resource.toURI());
    }
}

package org.tzi.use.plugins.bdi.casestudy;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.tzi.use.plugins.bdi.importer.CArtAgOArtifactAdapter;
import org.tzi.use.plugins.bdi.model.environment.ArtifactModel;
import org.tzi.use.plugins.bdi.model.environment.EnvironmentMappingConfirmation;
import org.tzi.use.plugins.bdi.model.environment.EnvironmentMappingDocument;
import org.tzi.use.plugins.bdi.model.environment.EnvironmentMappingStaleness;
import org.tzi.use.plugins.bdi.model.environment.EnvironmentMappingStalenessStatus;
import org.tzi.use.plugins.bdi.model.environment.EnvironmentModel;
import org.tzi.use.plugins.bdi.model.environment.EnvironmentSourceProvenance;
import org.tzi.use.plugins.bdi.model.environment.ObservablePropertyModel;
import org.tzi.use.plugins.bdi.model.environment.PersistedEnvironmentOperationMapping;
import org.tzi.use.plugins.bdi.model.environment.PersistedEnvironmentPropertyMapping;
import org.tzi.use.plugins.bdi.model.source.ProjectSourceId;
import org.tzi.use.plugins.bdi.use.UseModelSnapshot;
import org.tzi.use.plugins.bdi.use.UseUmlModelFacade;
import org.tzi.use.plugins.bdi.validation.IssueCertainty;
import org.tzi.use.plugins.bdi.validation.IssueSeverity;
import org.tzi.use.plugins.bdi.validation.environment.EnvironmentMappingValidationService;
import org.tzi.use.plugins.bdi.validation.environment.EnvironmentValidationResult;

class AuctionEnvironmentMappingPersistenceTest {
    @TempDir
    Path tempDir;

    @Test
    void loadsConfirmedMappingsIntoExistingEnvironmentRules() throws Exception {
        Path root = Files.createDirectories(tempDir.resolve("project")).toAbsolutePath();
        Path source = Files.writeString(Files.createDirectories(root.resolve("agents"))
                .resolve("auctioneer.asl"), "+ready.");
        UseModelSnapshot uml = new UseUmlModelFacade().snapshot(AuctionMappingFixtureTest.loadAuctionSystem());
        EnvironmentModel environment = environment();
        PersistedEnvironmentOperationMapping mapping = operation(root, source,
                "Auction::open()", EnvironmentMappingStaleness.current());
        PersistedEnvironmentPropertyMapping property = property(root, source);
        EnvironmentValidationResult result = new EnvironmentMappingValidationService().evaluate(
                new EnvironmentMappingDocument(EnvironmentMappingDocument.CURRENT_SCHEMA_VERSION,
                        List.of(mapping, property)),
                environment, uml);

        assertEquals(EnvironmentMappingStaleness.current(), result.document().mappings().get(0).staleness());
        assertEquals(List.of("ENV-003"), result.findings().stream()
                .map(finding -> finding.issue().ruleId()).toList());
        assertEquals(IssueCertainty.UNKNOWN, result.findings().get(0).issue().certainty());
        assertTrue(result.findings().stream().noneMatch(finding ->
                finding.issue().severity() == IssueSeverity.ERROR));
    }

    @Test
    void reportsStaleConfirmedTargetAndDoesNotSilentlyPassItToRules() throws Exception {
        Path root = Files.createDirectories(tempDir.resolve("stale")).toAbsolutePath();
        Path source = Files.writeString(Files.createDirectories(root.resolve("agents"))
                .resolve("auctioneer.asl"), "+ready.");
        UseModelSnapshot uml = new UseUmlModelFacade().snapshot(AuctionMappingFixtureTest.loadAuctionSystem());
        PersistedEnvironmentOperationMapping mapping = operation(root, source,
                "Auction::removed()", EnvironmentMappingStaleness.current());

        EnvironmentValidationResult result = new EnvironmentMappingValidationService().evaluate(
                new EnvironmentMappingDocument(EnvironmentMappingDocument.CURRENT_SCHEMA_VERSION, List.of(mapping)),
                environment(), uml);

        assertEquals(EnvironmentMappingStalenessStatus.STALE,
                result.document().mappings().get(0).staleness().status());
        assertEquals(List.of("ENV-004"), result.findings().stream()
                .map(finding -> finding.issue().ruleId()).toList());
        assertEquals(IssueSeverity.ERROR, result.findings().get(0).issue().severity());
        assertEquals(IssueCertainty.CONFIRMED, result.findings().get(0).issue().certainty());
        assertTrue(result.findings().get(0).issue().evidence().stream()
                .anyMatch(value -> value.contains("UML operation is missing")));
    }

    private static EnvironmentModel environment() {
        ArtifactModel artifact = new CArtAgOArtifactAdapter().normalize(
                "main", "auction", AuctionEnvironmentConsistencyTest.AuctionArtifact.class,
                List.of(new ObservablePropertyModel("status", 1, java.util.Optional.empty(),
                        List.of("Static descriptor declaration"))));
        return new EnvironmentModel(List.of(artifact));
    }

    private static PersistedEnvironmentOperationMapping operation(
            Path root, Path source, String umlTarget, EnvironmentMappingStaleness staleness) {
        return new PersistedEnvironmentOperationMapping(
                "open", 0, "main", "auction", AuctionEnvironmentConsistencyTest.AuctionArtifact.class.getName(),
                "open", 0, List.of(), umlTarget,
                EnvironmentMappingConfirmation.CONFIRMED,
                new EnvironmentSourceProvenance(ProjectSourceId.fromPath(root, source), "fixture"),
                staleness, List.of("fixture signature"));
    }

    private static PersistedEnvironmentPropertyMapping property(Path root, Path source) {
        return new PersistedEnvironmentPropertyMapping(
                "auction_status", 1, "main", "auction",
                AuctionEnvironmentConsistencyTest.AuctionArtifact.class.getName(),
                "status", 1, "String", "Auction::status",
                EnvironmentMappingConfirmation.CONFIRMED,
                new EnvironmentSourceProvenance(ProjectSourceId.fromPath(root, source), "fixture"),
                EnvironmentMappingStaleness.current(), List.of("fixture property signature"));
    }
}

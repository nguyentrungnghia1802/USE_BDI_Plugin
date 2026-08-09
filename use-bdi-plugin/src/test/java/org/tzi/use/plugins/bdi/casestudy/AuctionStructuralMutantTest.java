package org.tzi.use.plugins.bdi.casestudy;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;
import org.tzi.use.plugins.bdi.application.BdiImportService;
import org.tzi.use.plugins.bdi.application.BdiImportSnapshot;
import org.tzi.use.plugins.bdi.model.mapping.MappingBinding;
import org.tzi.use.plugins.bdi.model.mapping.MappingDocument;
import org.tzi.use.plugins.bdi.model.mapping.MappingKind;
import org.tzi.use.plugins.bdi.model.mapping.MappingStaleness;
import org.tzi.use.plugins.bdi.model.mapping.MappingStalenessDetector;
import org.tzi.use.plugins.bdi.model.mapping.MappingStalenessReason;
import org.tzi.use.plugins.bdi.use.UseModelSnapshot;
import org.tzi.use.plugins.bdi.use.UseUmlModelFacade;
import org.tzi.use.plugins.bdi.validation.ConsistencyIssue;
import org.tzi.use.plugins.bdi.validation.IssueStatus;
import org.tzi.use.plugins.bdi.validation.ValidationContext;
import org.tzi.use.plugins.bdi.validation.ValidationOrchestrator;
import org.tzi.use.parser.use.USECompiler;
import org.tzi.use.uml.mm.MModel;
import org.tzi.use.uml.mm.ModelFactory;
import org.tzi.use.uml.sys.MSystem;

class AuctionStructuralMutantTest {
    @Test
    void removesBidderStructureAndReportsMissingMappedTargets() throws Exception {
        BdiImportSnapshot imported = new BdiImportService().importFiles(List.of(
                AuctionMappingFixtureTest.fixture("fixtures/casestudy/auction/auctioneer.asl"),
                AuctionMappingFixtureTest.fixture("fixtures/casestudy/auction/bidder.asl")));
        assertTrue(imported.diagnostics().isEmpty(), () -> "Unexpected diagnostics: " + imported.diagnostics());

        UseUmlModelFacade facade = new UseUmlModelFacade();
        UseModelSnapshot baseline = facade.snapshot(AuctionMappingFixtureTest.loadAuctionSystem());
        MappingDocument mapping = AuctionMappingFixtureTest.confirmedMapping(imported, baseline);
        UseModelSnapshot mutant = facade.snapshot(loadMutant());

        assertFalse(mutant.classes().stream().anyMatch(value -> value.name().equals("Bidder")));
        assertFalse(mutant.operations().stream().anyMatch(value -> value.ownerName().equals("Bidder")));

        List<MappingStaleness> stale = new MappingStalenessDetector()
                .detect(imported.models(), imported.index(), mapping, mutant);
        assertTrue(stale.stream().anyMatch(value -> value.reason() == MappingStalenessReason.USE_FINGERPRINT_CHANGED));

        List<MappingStaleness> missingTargets = stale.stream()
                .filter(value -> value.reason() == MappingStalenessReason.TARGET_MISSING)
                .toList();
        assertEquals(9, missingTargets.size());
        assertEquals(Map.of(
                MappingKind.AGENT_CLASS, 1L,
                MappingKind.AGENT_OBJECT, 1L,
                MappingKind.ACTION_OPERATION, 2L,
                MappingKind.PARAMETER, 4L,
                MappingKind.BELIEF_ATTRIBUTE, 1L), missingTargets.stream()
                .map(MappingStaleness::binding)
                .flatMap(Optional::stream)
                .collect(Collectors.groupingBy(
                        MappingBinding::kind,
                        TreeMap::new,
                        Collectors.counting())));
        assertTrue(missingTargets.stream()
                .map(MappingStaleness::binding)
                .flatMap(Optional::stream)
                .map(MappingBinding::target)
                .allMatch(target -> target.contains("Bidder") || target.equals("bidder1")));

        List<ConsistencyIssue> issues = new ValidationOrchestrator()
                .evaluate(ValidationContext.from(imported, mapping, Optional.of(mutant)));
        List<ConsistencyIssue> mapIssues = issues.stream()
                .filter(issue -> issue.ruleId().equals("MAP-003"))
                .toList();
        assertEquals(9, mapIssues.size());
        assertTrue(mapIssues.stream().allMatch(issue -> issue.status() == IssueStatus.OPEN));
        assertFalse(issues.stream().anyMatch(issue -> issue.ruleId().equals("ASL-001")));
    }

    private static MSystem loadMutant() throws Exception {
        Path fixture = AuctionMappingFixtureTest.fixture(
                "fixtures/casestudy/auction/mutants/structural-remove-bidder.use");
        StringWriter errors = new StringWriter();
        MModel model = USECompiler.compileSpecification(
                Files.newInputStream(fixture),
                fixture.toString(),
                new PrintWriter(errors),
                new ModelFactory());
        assertTrue(model != null, errors::toString);
        model.setFilename(fixture.toString());
        MSystem system = new MSystem(model);
        var auctioneer = system.state().createObject(system.model().getClass("Auctioneer"), "auctioneer1");
        var auction = system.state().createObject(system.model().getClass("Auction"), "auction1");
        system.state().createLink(system.model().getAssociation("AuctioneerAuctions"),
                List.of(auctioneer, auction), null);
        return system;
    }
}

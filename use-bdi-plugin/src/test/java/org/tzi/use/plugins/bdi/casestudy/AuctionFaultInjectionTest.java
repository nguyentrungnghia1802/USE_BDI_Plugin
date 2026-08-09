package org.tzi.use.plugins.bdi.casestudy;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.tzi.use.parser.use.USECompiler;
import org.tzi.use.plugins.bdi.application.BdiImportService;
import org.tzi.use.plugins.bdi.application.BdiImportSnapshot;
import org.tzi.use.plugins.bdi.index.ActionCallSite;
import org.tzi.use.plugins.bdi.model.ir.AgentModel;
import org.tzi.use.plugins.bdi.model.mapping.MappingBinding;
import org.tzi.use.plugins.bdi.model.mapping.MappingDocument;
import org.tzi.use.plugins.bdi.model.mapping.MappingKind;
import org.tzi.use.plugins.bdi.model.mapping.MappingSourceId;
import org.tzi.use.plugins.bdi.use.UseModelSnapshot;
import org.tzi.use.plugins.bdi.use.UseSnapshotOclEvaluator;
import org.tzi.use.plugins.bdi.use.UseUmlModelFacade;
import org.tzi.use.plugins.bdi.use.UmlOperationRef;
import org.tzi.use.plugins.bdi.validation.ConsistencyIssue;
import org.tzi.use.plugins.bdi.validation.IssueStatus;
import org.tzi.use.plugins.bdi.validation.SnapshotOclEvaluator;
import org.tzi.use.plugins.bdi.validation.ValidationContext;
import org.tzi.use.plugins.bdi.validation.ValidationOrchestrator;
import org.tzi.use.uml.mm.MAttribute;
import org.tzi.use.uml.mm.MModel;
import org.tzi.use.uml.mm.ModelFactory;
import org.tzi.use.uml.ocl.value.EnumValue;
import org.tzi.use.uml.sys.MSystem;

class AuctionFaultInjectionTest {
    @Test
    void detectsSignatureMutationAsArityMismatch() throws Exception {
        BdiImportSnapshot imported = importSources(
                "fixtures/casestudy/auction/auctioneer.asl",
                "fixtures/casestudy/auction/bidder.asl");
        UseModelSnapshot baseline = snapshot("fixtures/casestudy/auction/Auction.use");
        UseModelSnapshot mutant = snapshot("fixtures/casestudy/auction/mutants/signature-open-flag.use");
        MappingDocument baselineMapping = AuctionMappingFixtureTest.confirmedMapping(imported, baseline);
        ActionCallSite open = imported.index().allActionCallSites().stream()
                .filter(action -> action.signature().map(signature -> signature.functor().equals("open")).orElse(false))
                .findFirst()
                .orElseThrow();
        UmlOperationRef mutatedOpen = mutant.operations().stream()
                .filter(operation -> operation.ownerName().equals("Auction") && operation.name().equals("open"))
                .findFirst()
                .orElseThrow();
        MappingBinding originalBinding = baselineMapping
                .find(MappingKind.ACTION_OPERATION, MappingSourceId.action(open))
                .orElseThrow();
        MappingDocument mapping = new MappingDocument(
                baselineMapping.schemaVersion(),
                baselineMapping.bdiMetamodelVersion(),
                mutant.fingerprint(),
                baselineMapping.bindings()).upsert(new MappingBinding(
                        MappingKind.ACTION_OPERATION,
                        originalBinding.source(),
                        mutatedOpen.reference()));

        List<ConsistencyIssue> issues = validate(imported, mapping, mutant, Optional.empty());
        List<ConsistencyIssue> openArityMismatches = issues.stream()
                .filter(issue -> issue.ruleId().equals("SIG-001"))
                .filter(issue -> issue.umlElementRef()
                        .filter(reference -> reference.equals("Auction::open(flag:String)"))
                        .isPresent())
                .toList();

        assertEquals(1, openArityMismatches.size());
        assertTrue(openArityMismatches.get(0).message().contains("Action arity"));
        assertTrue(issues.stream().noneMatch(issue -> issue.ruleId().equals("MAP-003")));
    }

    @Test
    void detectsReferenceMutationAsAbsentUseObject() throws Exception {
        BdiImportSnapshot imported = importSources(
                "fixtures/casestudy/auction/mutants/reference/auctioneer.asl",
                "fixtures/casestudy/auction/bidder.asl");
        UseModelSnapshot uml = snapshot("fixtures/casestudy/auction/Auction.use");
        MappingDocument mapping = AuctionMappingFixtureTest.confirmedMapping(
                imported, uml, "auctioneer.asl", "bidder.asl");

        List<ConsistencyIssue> issues = validate(imported, mapping, uml, Optional.empty());
        List<ConsistencyIssue> absentBidderReferences = issues.stream()
                .filter(issue -> issue.ruleId().equals("REF-001"))
                .filter(issue -> issue.message().contains("bidder2"))
                .toList();

        assertEquals(4, absentBidderReferences.size());
        assertTrue(absentBidderReferences.stream().allMatch(issue -> issue.status() == IssueStatus.OPEN));
        assertTrue(absentBidderReferences.stream().allMatch(issue -> issue.certainty().name().equals("POTENTIAL")));
    }

    @Test
    void detectsOclMutationAsFailedOpenPreconditionAndRestoresState() throws Exception {
        BdiImportSnapshot imported = importSources(
                "fixtures/casestudy/auction/auctioneer.asl",
                "fixtures/casestudy/auction/bidder.asl");
        MSystem baselineSystem = loadSystem("fixtures/casestudy/auction/Auction.use");
        MSystem mutantSystem = loadSystem("fixtures/casestudy/auction/mutants/ocl-open-closed.use");
        setAuctionStatus(baselineSystem, "draft");
        setAuctionStatus(mutantSystem, "draft");
        UseUmlModelFacade facade = new UseUmlModelFacade();
        UseModelSnapshot baseline = facade.snapshot(baselineSystem);
        UseModelSnapshot mutant = facade.snapshot(mutantSystem);

        MappingDocument baselineMapping = auctionObjectMapping(
                imported, AuctionMappingFixtureTest.confirmedMapping(imported, baseline));
        MappingDocument mutantMapping = auctionObjectMapping(
                imported, AuctionMappingFixtureTest.confirmedMapping(imported, mutant));
        String before = mutant.fingerprint();
        List<ConsistencyIssue> baselineIssues = validate(
                imported,
                baselineMapping,
                baseline,
                Optional.of(new UseSnapshotOclEvaluator(baselineSystem)));
        List<ConsistencyIssue> mutantIssues = validate(
                imported,
                mutantMapping,
                mutant,
                Optional.of(new UseSnapshotOclEvaluator(mutantSystem)));

        assertTrue(baselineIssues.stream()
                .noneMatch(issue -> isOpenPreconditionIssue(issue)));
        assertEquals(1, mutantIssues.stream()
                .filter(AuctionFaultInjectionTest::isOpenPreconditionIssue)
                .count());
        assertEquals(before, facade.snapshot(mutantSystem).fingerprint());
    }

    private static boolean isOpenPreconditionIssue(ConsistencyIssue issue) {
        return issue.ruleId().equals("OCL-001")
                && issue.umlElementRef().filter("Auction::open()"::equals).isPresent();
    }

    private static MappingDocument auctionObjectMapping(
            BdiImportSnapshot imported,
            MappingDocument mapping) {
        AgentModel auctioneer = imported.models().stream()
                .filter(model -> model.source().getFileName().toString().equals("auctioneer.asl"))
                .findFirst()
                .orElseThrow();
        return mapping.upsert(new MappingBinding(
                MappingKind.AGENT_OBJECT,
                MappingSourceId.agent(auctioneer),
                "auction1"));
    }

    private static List<ConsistencyIssue> validate(
            BdiImportSnapshot imported,
            MappingDocument mapping,
            UseModelSnapshot uml,
            Optional<SnapshotOclEvaluator> evaluator) {
        return new ValidationOrchestrator().evaluate(ValidationContext.from(imported, mapping, Optional.of(uml), evaluator));
    }

    private static BdiImportSnapshot importSources(String first, String second) throws Exception {
        return new BdiImportService().importFiles(List.of(
                AuctionMappingFixtureTest.fixture(first),
                AuctionMappingFixtureTest.fixture(second)));
    }

    private static UseModelSnapshot snapshot(String fixtureName) throws Exception {
        return new UseUmlModelFacade().snapshot(loadSystem(fixtureName));
    }

    private static MSystem loadSystem(String fixtureName) throws Exception {
        Path fixture = AuctionMappingFixtureTest.fixture(fixtureName);
        StringWriter errors = new StringWriter();
        MModel model = USECompiler.compileSpecification(
                Files.newInputStream(fixture),
                fixture.toString(),
                new PrintWriter(errors),
                new ModelFactory());
        assertNotNull(model, errors::toString);
        model.setFilename(fixture.toString());
        MSystem system = new MSystem(model);
        var auctioneer = system.state().createObject(system.model().getClass("Auctioneer"), "auctioneer1");
        var auction = system.state().createObject(system.model().getClass("Auction"), "auction1");
        var bidder = system.state().createObject(system.model().getClass("Bidder"), "bidder1");
        system.state().createLink(system.model().getAssociation("AuctioneerAuctions"),
                List.of(auctioneer, auction), null);
        system.state().createLink(system.model().getAssociation("AuctionBidders"),
                List.of(auction, bidder), null);
        return system;
    }

    private static void setAuctionStatus(MSystem system, String literal) {
        MAttribute status = system.model().getClass("Auction").attribute("status", false);
        system.state().objectByName("auction1").state(system.state())
                .setAttributeValue(status, new EnumValue(system.model().enumType("AuctionStatus"), literal));
    }
}

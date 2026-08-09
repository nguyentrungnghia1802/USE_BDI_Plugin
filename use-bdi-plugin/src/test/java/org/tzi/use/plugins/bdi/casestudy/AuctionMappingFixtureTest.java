package org.tzi.use.plugins.bdi.casestudy;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.tzi.use.parser.use.USECompiler;
import org.tzi.use.plugins.bdi.application.BdiImportService;
import org.tzi.use.plugins.bdi.application.BdiImportSnapshot;
import org.tzi.use.plugins.bdi.index.ActionCallSite;
import org.tzi.use.plugins.bdi.index.BdiIndex;
import org.tzi.use.plugins.bdi.model.ir.AgentModel;
import org.tzi.use.plugins.bdi.model.mapping.MappingDocument;
import org.tzi.use.plugins.bdi.model.mapping.MappingFingerprint;
import org.tzi.use.plugins.bdi.model.mapping.MappingKind;
import org.tzi.use.plugins.bdi.model.mapping.MappingSourceId;
import org.tzi.use.plugins.bdi.model.mapping.MappingStalenessDetector;
import org.tzi.use.plugins.bdi.model.mapping.MappingSuggestion;
import org.tzi.use.plugins.bdi.model.mapping.MappingSuggestionService;
import org.tzi.use.plugins.bdi.persistence.MappingFileRepository;
import org.tzi.use.plugins.bdi.use.UmlOperationRef;
import org.tzi.use.plugins.bdi.use.UseModelSnapshot;
import org.tzi.use.plugins.bdi.use.UseUmlModelFacade;
import org.tzi.use.plugins.bdi.validation.ConsistencyIssue;
import org.tzi.use.plugins.bdi.validation.ValidationContext;
import org.tzi.use.plugins.bdi.validation.ValidationOrchestrator;
import org.tzi.use.uml.mm.MModel;
import org.tzi.use.uml.mm.ModelFactory;
import org.tzi.use.uml.sys.MSystem;

class AuctionMappingFixtureTest {
    @TempDir
    Path tempDir;

    @Test
    void buildsPersistsAndValidatesConfirmedAuctionMapping() throws Exception {
        BdiImportSnapshot imported = new BdiImportService().importFiles(List.of(
                fixture("fixtures/casestudy/auction/auctioneer.asl"),
                fixture("fixtures/casestudy/auction/bidder.asl")));
        assertTrue(imported.diagnostics().isEmpty(), () -> "Unexpected diagnostics: " + imported.diagnostics());

        MSystem system = loadAuctionSystem();
        UseModelSnapshot uml = new UseUmlModelFacade().snapshot(system);
        assertEquals(3, uml.objects().size());

        MappingSuggestionService suggestions = new MappingSuggestionService();
        List<MappingSuggestion> candidates = suggestions.suggest(imported.models(), imported.index(), uml);
        AgentModel auctioneer = model(imported, "auctioneer.asl");
        AgentModel bidder = model(imported, "bidder.asl");
        List<MappingSuggestion> confirmed = new ArrayList<>();

        confirmed.add(require(candidates, MappingKind.AGENT_CLASS,
                MappingSourceId.agent(auctioneer), "Auctioneer"));
        confirmed.add(require(candidates, MappingKind.AGENT_OBJECT,
                MappingSourceId.agent(auctioneer), "auctioneer1"));
        confirmed.add(require(candidates, MappingKind.AGENT_CLASS,
                MappingSourceId.agent(bidder), "Bidder"));
        confirmed.add(require(candidates, MappingKind.AGENT_OBJECT,
                MappingSourceId.agent(bidder), "bidder1"));

        addActionMapping(confirmed, candidates, imported.index(), auctioneer.source(), "open",
                operation(uml, "Auction", "open"));
        addActionMapping(confirmed, candidates, imported.index(), auctioneer.source(), "placeBid",
                operation(uml, "Auction", "placeBid"));
        addActionMapping(confirmed, candidates, imported.index(), auctioneer.source(), "close",
                operation(uml, "Auction", "close"));
        addActionMapping(confirmed, candidates, imported.index(), bidder.source(), "submitBid",
                operation(uml, "Bidder", "submitBid"));

        confirmed.add(require(candidates, MappingKind.BELIEF_ATTRIBUTE, "auction_status/1", "Auction::status"));
        confirmed.add(require(candidates, MappingKind.BELIEF_ATTRIBUTE, "budget/1", "Bidder::budget"));

        MappingDocument document = MappingDocument.empty(uml.fingerprint());
        for (MappingSuggestion suggestion : confirmed) {
            document = document.upsert(suggestion.toBinding());
        }
        assertEquals(14, document.bindings().size());
        assertTrue(confirmed.stream().allMatch(suggestion -> !suggestion.reasons().isEmpty()));

        Path mappingFile = tempDir.resolve("Auction.bdimap.json");
        MappingFileRepository repository = new MappingFileRepository();
        repository.save(mappingFile, document);
        MappingDocument loaded = repository.load(mappingFile);
        assertEquals(document, loaded);
        assertEquals(64, MappingFingerprint.compute(loaded).length());

        assertTrue(new MappingStalenessDetector()
                .detect(imported.models(), imported.index(), loaded, uml)
                .isEmpty());

        List<ConsistencyIssue> mappingIssues = new ValidationOrchestrator()
                .evaluate(ValidationContext.from(imported, loaded, Optional.of(uml))).stream()
                .filter(issue -> issue.ruleId().startsWith("MAP-"))
                .toList();
        assertTrue(mappingIssues.isEmpty(), () -> "Unexpected mapping issues: " + mappingIssues);
    }

    private static void addActionMapping(
            List<MappingSuggestion> confirmed,
            List<MappingSuggestion> candidates,
            BdiIndex index,
            Path source,
            String functor,
            UmlOperationRef operation) {
        ActionCallSite action = action(index, source, functor);
        confirmed.add(require(candidates, MappingKind.ACTION_OPERATION,
                MappingSourceId.action(action), operation.reference()));
        for (int indexInAction = 0; indexInAction < operation.parameters().size(); indexInAction++) {
            confirmed.add(require(candidates, MappingKind.PARAMETER,
                    MappingSourceId.argument(action, indexInAction),
                    operation.reference() + "#parameter:" + operation.parameters().get(indexInAction).name()));
        }
    }

    private static MappingSuggestion require(
            List<MappingSuggestion> candidates,
            MappingKind kind,
            String source,
            String target) {
        return candidates.stream()
                .filter(candidate -> candidate.kind() == kind)
                .filter(candidate -> candidate.source().equals(source))
                .filter(candidate -> candidate.target().equals(target))
                .findFirst()
                .orElseThrow(() -> new AssertionError(
                        "Missing suggestion: " + kind + " " + source + " -> " + target));
    }

    private static ActionCallSite action(BdiIndex index, Path source, String functor) {
        Path normalized = source.toAbsolutePath().normalize();
        return index.allActionCallSites().stream()
                .filter(value -> value.sourceSpan().source().equals(normalized))
                .filter(value -> value.signature()
                        .map(signature -> signature.functor().equals(functor))
                        .orElse(false))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Missing action: " + source + " " + functor));
    }

    private static UmlOperationRef operation(UseModelSnapshot snapshot, String owner, String name) {
        return snapshot.operations().stream()
                .filter(value -> value.ownerName().equals(owner) && value.name().equals(name))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Missing operation: " + owner + "::" + name));
    }

    private static AgentModel model(BdiImportSnapshot snapshot, String filename) {
        return snapshot.models().stream()
                .filter(value -> value.source().getFileName().toString().equals(filename))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Missing model: " + filename));
    }

    private static MSystem loadAuctionSystem() throws Exception {
        Path fixture = fixture("fixtures/casestudy/auction/Auction.use");
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
        system.state().createLink(system.model().getAssociation("AuctioneerAuctions"), List.of(auctioneer, auction), null);
        system.state().createLink(system.model().getAssociation("AuctionBidders"), List.of(auction, bidder), null);
        return system;
    }

    private static Path fixture(String name) throws URISyntaxException {
        URL resource = AuctionMappingFixtureTest.class.getClassLoader().getResource(name);
        if (resource == null) {
            throw new IllegalStateException("Missing fixture: " + name);
        }
        return Path.of(resource.toURI());
    }
}

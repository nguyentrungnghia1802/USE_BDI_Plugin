package org.tzi.use.plugins.bdi.casestudy;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;
import org.tzi.use.parser.use.USECompiler;
import org.tzi.use.plugins.bdi.use.UmlOperationRef;
import org.tzi.use.plugins.bdi.use.UseModelSnapshot;
import org.tzi.use.plugins.bdi.use.UseUmlModelFacade;
import org.tzi.use.uml.mm.MAssociation;
import org.tzi.use.uml.mm.MModel;
import org.tzi.use.uml.mm.ModelFactory;
import org.tzi.use.uml.sys.MSystem;

class AuctionModelFixtureTest {
    @Test
    void compilesAuctionUmlOclFixtureAndProjectsItsCaseStudySurface() throws Exception {
        MSystem system = loadFixture();
        UseModelSnapshot empty = new UseUmlModelFacade().snapshot(system);

        assertEquals("AuctionModel", empty.modelName());
        assertEquals(
                Set.of("Auction", "Auctioneer", "Bid", "Bidder"),
                empty.classes().stream().map(classRef -> classRef.name()).collect(Collectors.toSet()));
        assertEquals(
                Set.of("AuctioneerAuctions", "AuctionBidders", "AuctionBids", "BidderBids"),
                empty.associations().stream().map(association -> association.name()).collect(Collectors.toSet()));
        assertEquals(7, empty.attributes().size());
        assertEquals(5, empty.classInvariants().size());
        assertEquals(4, empty.operations().size());
        assertEquals(7, empty.operations().stream()
                .mapToInt(operation -> operation.preconditions().size()).sum());
        assertEquals(2, empty.operations().stream()
                .mapToInt(operation -> operation.postconditions().size()).sum());

        UmlOperationRef placeBid = findOperation(empty, "Auction", "placeBid");
        assertEquals(List.of("Bidder", "Real"), placeBid.parameters().stream()
                .map(parameter -> parameter.type()).toList());
        assertEquals(3, placeBid.preconditions().size());
        assertTrue(placeBid.preconditions().stream()
                .anyMatch(condition -> condition.name().equals("BidderParticipates")));

        var auctioneer = system.state().createObject(system.model().getClass("Auctioneer"), "auctioneer1");
        var auction = system.state().createObject(system.model().getClass("Auction"), "auction1");
        var bidder = system.state().createObject(system.model().getClass("Bidder"), "bidder1");
        MAssociation auctioneerAuctions = system.model().getAssociation("AuctioneerAuctions");
        MAssociation auctionBidders = system.model().getAssociation("AuctionBidders");
        assertNotNull(auctioneerAuctions);
        assertNotNull(auctionBidders);
        system.state().createLink(auctioneerAuctions, List.of(auctioneer, auction), null);
        system.state().createLink(auctionBidders, List.of(auction, bidder), null);

        UseModelSnapshot populated = new UseUmlModelFacade().snapshot(system);
        assertEquals(3, populated.objects().size());
        assertEquals(2, populated.links().size());
        assertNotEquals(empty.fingerprint(), populated.fingerprint());
    }

    private static UmlOperationRef findOperation(UseModelSnapshot snapshot, String owner, String name) {
        return snapshot.operations().stream()
                .filter(operation -> operation.ownerName().equals(owner) && operation.name().equals(name))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Missing operation: " + owner + "::" + name));
    }

    private static MSystem loadFixture() throws Exception {
        Path fixture = fixture("fixtures/casestudy/auction/Auction.use");
        StringWriter errors = new StringWriter();
        MModel model = USECompiler.compileSpecification(
                Files.newInputStream(fixture),
                fixture.toString(),
                new PrintWriter(errors),
                new ModelFactory());
        assertNotNull(model, errors::toString);
        model.setFilename(fixture.toString());
        return new MSystem(model);
    }

    private static Path fixture(String name) throws URISyntaxException {
        URL resource = AuctionModelFixtureTest.class.getClassLoader().getResource(name);
        if (resource == null) {
            throw new IllegalStateException("Missing fixture: " + name);
        }
        return Path.of(resource.toURI());
    }
}

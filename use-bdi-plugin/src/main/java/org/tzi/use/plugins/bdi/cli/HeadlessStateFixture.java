package org.tzi.use.plugins.bdi.cli;

import java.util.List;

import org.tzi.use.uml.mm.MAssociation;
import org.tzi.use.uml.mm.MAttribute;
import org.tzi.use.uml.mm.MClass;
import org.tzi.use.uml.ocl.value.EnumValue;
import org.tzi.use.uml.sys.MObject;
import org.tzi.use.uml.sys.MSystem;

/** Named, static state setup for reproducible analysis; it never starts a runtime. */
public record HeadlessStateFixture(String id) {
    public static final String AUCTION_POPULATED = "auction-populated";

    public HeadlessStateFixture {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("state fixture id must not be blank");
        }
    }

    public void apply(MSystem system) {
        if (!AUCTION_POPULATED.equals(id)) {
            throw new IllegalArgumentException("Unsupported headless state fixture: " + id);
        }
        try {
            applyAuctionState(system);
        } catch (org.tzi.use.uml.sys.MSystemException error) {
            throw new IllegalArgumentException("Could not apply state fixture " + id + ": " + error.getMessage(), error);
        }
    }

    private static void applyAuctionState(MSystem system) throws org.tzi.use.uml.sys.MSystemException {
        MClass auctioneerClass = requireClass(system, "Auctioneer");
        MClass auctionClass = requireClass(system, "Auction");
        MObject auctioneer = system.state().createObject(auctioneerClass, "auctioneer1");
        MObject auction = system.state().createObject(auctionClass, "auction1");
        MClass bidderClass = system.model().getClass("Bidder");
        MObject bidder = bidderClass == null ? null : system.state().createObject(bidderClass, "bidder1");

        link(system, "AuctioneerAuctions", auctioneer, auction);
        if (bidder != null) {
            link(system, "AuctionBidders", auction, bidder);
        }
        setDraftStatus(system, auctionClass, auction);
    }

    private static MClass requireClass(MSystem system, String name) {
        MClass type = system.model().getClass(name);
        if (type == null) {
            throw new IllegalArgumentException("State fixture requires USE class: " + name);
        }
        return type;
    }

    private static void link(MSystem system, String associationName, MObject left, MObject right)
            throws org.tzi.use.uml.sys.MSystemException {
        MAssociation association = system.model().getAssociation(associationName);
        if (association != null) {
            system.state().createLink(association, List.of(left, right), null);
        }
    }

    private static void setDraftStatus(MSystem system, MClass auctionClass, MObject auction)
            throws org.tzi.use.uml.sys.MSystemException {
        MAttribute status = auctionClass.attribute("status", false);
        if (status != null && system.model().enumType("AuctionStatus") != null) {
            auction.state(system.state()).setAttributeValue(
                    status, new EnumValue(system.model().enumType("AuctionStatus"), "draft"));
        }
    }
}

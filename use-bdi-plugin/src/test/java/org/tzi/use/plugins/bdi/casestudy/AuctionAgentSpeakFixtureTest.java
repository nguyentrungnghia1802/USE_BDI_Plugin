package org.tzi.use.plugins.bdi.casestudy;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.tzi.use.plugins.bdi.application.BdiImportService;
import org.tzi.use.plugins.bdi.application.BdiImportSnapshot;
import org.tzi.use.plugins.bdi.index.ActionCallSite;
import org.tzi.use.plugins.bdi.index.PredicateSignature;

class AuctionAgentSpeakFixtureTest {
    private final BdiImportService service = new BdiImportService();

    @Test
    void importsAuctionAgentsAndIndexesLifecycleActions() throws Exception {
        BdiImportSnapshot snapshot = service.importFiles(List.of(
                fixture("fixtures/casestudy/auction/auctioneer.asl"),
                fixture("fixtures/casestudy/auction/bidder.asl")));

        assertEquals(2, snapshot.fileCount());
        assertTrue(snapshot.diagnostics().isEmpty(), () -> "Unexpected diagnostics: " + snapshot.diagnostics());
        assertEquals(2, snapshot.index().models().size());

        assertEquals("auctioneer.asl", snapshot.models().get(0).source().getFileName().toString());
        assertEquals(2, snapshot.models().get(0).beliefCount());
        assertEquals(1, snapshot.models().get(0).goalCount());
        assertEquals(3, snapshot.models().get(0).planCount());
        assertTrue(snapshot.models().get(0).isMaterialized());

        assertEquals("bidder.asl", snapshot.models().get(1).source().getFileName().toString());
        assertEquals(2, snapshot.models().get(1).beliefCount());
        assertEquals(1, snapshot.models().get(1).goalCount());
        assertEquals(1, snapshot.models().get(1).planCount());
        assertTrue(snapshot.models().get(1).isMaterialized());

        assertEquals(1, snapshot.index().supportingPlans(new PredicateSignature("run_auction", 0)).size());
        assertEquals(1, snapshot.index().supportingPlans(new PredicateSignature("submit_bid", 0)).size());
        assertExternalAction(snapshot, "open", 0);
        assertExternalAction(snapshot, "placeBid", 2);
        assertExternalAction(snapshot, "close", 0);
        assertExternalAction(snapshot, "submitBid", 2);
        assertInternalAction(snapshot, ".print", 1);
    }

    private static void assertExternalAction(
            BdiImportSnapshot snapshot,
            String functor,
            int arity) {
        List<ActionCallSite> calls = snapshot.index().actionCalls(new PredicateSignature(functor, arity));
        assertEquals(1, calls.size(), () -> "Unexpected calls for " + functor + "/" + arity + ": " + calls);
        assertEquals(ActionCallSite.ActionKind.EXTERNAL_ACTION, calls.get(0).kind());
    }

    private static void assertInternalAction(
            BdiImportSnapshot snapshot,
            String functor,
            int arity) {
        List<ActionCallSite> calls = snapshot.index().actionCalls(new PredicateSignature(functor, arity));
        assertEquals(1, calls.size(), () -> "Unexpected calls for " + functor + "/" + arity + ": " + calls);
        assertEquals(ActionCallSite.ActionKind.INTERNAL_ACTION, calls.get(0).kind());
    }

    private static Path fixture(String name) throws URISyntaxException {
        URL resource = AuctionAgentSpeakFixtureTest.class.getClassLoader().getResource(name);
        if (resource == null) {
            throw new IllegalStateException("Missing test fixture: " + name);
        }
        return Path.of(resource.toURI());
    }
}

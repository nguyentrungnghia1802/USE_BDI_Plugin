package org.tzi.use.plugins.bdi.importer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.tzi.use.plugins.bdi.model.environment.ArtifactModel;
import org.tzi.use.plugins.bdi.model.environment.ObservablePropertyModel;

import cartago.Artifact;
import cartago.OPERATION;
import cartago.OpFeedbackParam;

class CArtAgOArtifactAdapterTest {
    @Test
    void normalizesOfficialOperationAnnotationsAndDeclaredProperties() {
        ArtifactModel artifact = auctionArtifact();

        assertEquals("main/auction", artifact.reference());
        assertEquals(List.of("close/0", "open/0", "placeBid/3"), artifact.operations().stream()
                .map(operation -> operation.signature()).toList());
        assertEquals(List.of("status/1"), artifact.observableProperties().stream()
                .map(property -> property.signature()).toList());
        assertEquals(Optional.empty(), artifact.observableProperties().get(0).runtimeValues());
    }

    @Test
    void rejectsClassesOutsideTheOfficialArtifactModel() {
        assertThrows(IllegalArgumentException.class, () -> new CArtAgOArtifactAdapter()
                .normalize("main", "invalid", String.class, List.of()));
    }

    static ArtifactModel auctionArtifact() {
        return new CArtAgOArtifactAdapter().normalize(
                "main",
                "auction",
                AuctionArtifact.class,
                List.of(new ObservablePropertyModel(
                        "status", 1, Optional.empty(),
                        List.of("Declared by the Auction artifact descriptor; dynamic value not captured"))));
    }

    static final class AuctionArtifact extends Artifact {
        @OPERATION(guard = "")
        public void open() {
        }

        @OPERATION(guard = "")
        public void placeBid(String bidder, double amount, OpFeedbackParam<Boolean> accepted) {
        }

        @OPERATION(guard = "")
        public void close() {
        }
    }
}

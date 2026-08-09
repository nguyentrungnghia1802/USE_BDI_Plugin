package org.tzi.use.plugins.bdi.model.source;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.tzi.use.plugins.bdi.model.ir.SourceSpan;
import org.tzi.use.plugins.bdi.model.mapping.MappingSourceId;

class ProjectSourceIdTest {
    @Test
    void remainsStableAcrossCheckoutRoots(@TempDir Path tempDir) {
        Path firstRoot = tempDir.resolve("checkout-one");
        Path secondRoot = tempDir.resolve("checkout-two");
        SourceSpan first = new SourceSpan(firstRoot.resolve("agents/bidder.asl"), 4, 2, 4, 12);
        SourceSpan second = new SourceSpan(secondRoot.resolve("agents/bidder.asl"), 4, 2, 4, 12);

        assertEquals(ProjectSourceId.from(firstRoot, first), ProjectSourceId.from(secondRoot, second));
        assertEquals(ProjectSourceId.from(firstRoot, first).canonical(),
                ProjectSourceId.from(secondRoot, second).canonical());
    }

    @Test
    void distinguishesPathAndCoordinates(@TempDir Path tempDir) {
        ProjectSourceId baseline = ProjectSourceId.from(
                tempDir,
                new SourceSpan(tempDir.resolve("agents/bidder.asl"), 4, 2, 4, 12));

        assertNotEquals(baseline, ProjectSourceId.from(
                tempDir,
                new SourceSpan(tempDir.resolve("agents/auctioneer.asl"), 4, 2, 4, 12)));
        assertNotEquals(baseline, ProjectSourceId.from(
                tempDir,
                new SourceSpan(tempDir.resolve("agents/bidder.asl"), 5, 2, 5, 12)));
    }

    @Test
    void normalizesTraversalAndPreservesCase(@TempDir Path tempDir) {
        Path source = tempDir.resolve("agents/unused/../Bidder.asl");

        ProjectSourceId identity = ProjectSourceId.fromPath(tempDir.resolve("."), source);

        assertEquals("agents/Bidder.asl", identity.projectPath());
        assertTrue(identity.canonical().contains("agents/Bidder.asl"));
        assertEquals(tempDir.resolve("agents/Bidder.asl").toAbsolutePath().normalize(),
                identity.resolve(tempDir));
    }

    @Test
    void roundTripsCanonicalIdentity(@TempDir Path tempDir) {
        ProjectSourceId identity = ProjectSourceId.from(
                tempDir,
                new SourceSpan(tempDir.resolve("agents/with%#name.asl"), 10, 3, 11, 7));

        ProjectSourceId decoded = ProjectSourceId.parse(identity.canonical());

        assertEquals(identity, decoded);
        assertEquals(identity.toSourceSpan(tempDir), decoded.toSourceSpan(tempDir));
        assertTrue(identity.canonical().contains("%25%23"));
    }

    @Test
    void rejectsSourcesOutsideProjectRoot(@TempDir Path tempDir) {
        Path root = tempDir.resolve("project");
        Path outside = tempDir.resolve("other/agent.asl");

        IllegalArgumentException error = assertThrows(
                IllegalArgumentException.class,
                () -> ProjectSourceId.fromPath(root, outside));

        assertTrue(error.getMessage().contains("outside project root"));
    }

    @Test
    void leavesLegacyAbsoluteMappingIdentityAvailable(@TempDir Path tempDir) {
        Path source = tempDir.resolve("agent.asl").toAbsolutePath().normalize();

        assertEquals(source, MappingSourceId.sourcePath(source.toString()).orElseThrow());
        assertTrue(MappingSourceId.sourcePath(ProjectSourceId.fromPath(tempDir, source).canonical()).isEmpty());
    }
}

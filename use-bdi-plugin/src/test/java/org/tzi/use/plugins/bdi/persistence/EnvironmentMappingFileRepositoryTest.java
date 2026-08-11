package org.tzi.use.plugins.bdi.persistence;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.tzi.use.plugins.bdi.model.environment.EnvironmentMappingConfirmation;
import org.tzi.use.plugins.bdi.model.environment.EnvironmentMappingDocument;
import org.tzi.use.plugins.bdi.model.environment.EnvironmentMappingStaleness;
import org.tzi.use.plugins.bdi.model.environment.EnvironmentSourceProvenance;
import org.tzi.use.plugins.bdi.model.environment.PersistedEnvironmentOperationMapping;
import org.tzi.use.plugins.bdi.model.environment.PersistedEnvironmentPropertyMapping;
import org.tzi.use.plugins.bdi.model.mapping.MappingBinding;
import org.tzi.use.plugins.bdi.model.mapping.MappingDocument;
import org.tzi.use.plugins.bdi.model.mapping.MappingKind;
import org.tzi.use.plugins.bdi.model.source.ProjectSourceId;

class EnvironmentMappingFileRepositoryTest {
    @TempDir
    Path tempDir;

    @Test
    void roundTripsTypedMappingsWithStableBytesAndPortableSourceIdentity() throws Exception {
        Path firstRoot = projectRoot("first");
        Path firstSource = Files.writeString(firstRoot.resolve("agents/auctioneer.asl"), "+ready.");
        EnvironmentMappingDocument document = document(firstRoot, firstSource);
        EnvironmentMappingFileRepository repository = new EnvironmentMappingFileRepository();

        Path firstFile = tempDir.resolve("first.cartago-map.json");
        Path secondFile = tempDir.resolve("second.cartago-map.json");
        repository.save(firstFile, document, firstRoot);
        EnvironmentMappingDocument loaded = repository.load(firstFile, firstRoot);
        repository.save(secondFile, loaded, firstRoot);

        assertEquals(document, loaded);
        assertEquals(Files.readString(firstFile), Files.readString(secondFile));
        assertEquals(2, loaded.confirmedCurrentMappings().size());
        assertTrue(!Files.readString(firstFile).contains("cartago-source-v1"));
        assertTrue(Files.readString(firstFile).contains(ProjectSourceId.VERSION));

        Path secondRoot = projectRoot("second");
        Path secondSource = Files.writeString(secondRoot.resolve("agents/auctioneer.asl"), "+ready.");
        EnvironmentMappingDocument relocated = document(secondRoot, secondSource);
        Path relocatedFile = tempDir.resolve("relocated.cartago-map.json");
        repository.save(relocatedFile, relocated, secondRoot);
        assertEquals(Files.readString(firstFile), Files.readString(relocatedFile));
        assertEquals(document.mappings().get(0).provenance().source(),
                relocated.mappings().get(0).provenance().source());
    }

    @Test
    void keepsCandidatesOutOfRuntimeValidationAndRetainsExplicitUnknown() throws Exception {
        Path root = projectRoot("candidate");
        Path source = Files.writeString(root.resolve("agents/auctioneer.asl"), "+ready.");
        PersistedEnvironmentOperationMapping confirmed = operation(root, source,
                EnvironmentMappingConfirmation.CONFIRMED, EnvironmentMappingStaleness.current());
        PersistedEnvironmentPropertyMapping candidate = property(root, source,
                EnvironmentMappingConfirmation.CANDIDATE, EnvironmentMappingStaleness.current());
        PersistedEnvironmentOperationMapping unknown = operation(root, source, "Auction::unknown()",
                EnvironmentMappingConfirmation.CONFIRMED,
                EnvironmentMappingStaleness.unknown("source content was not available"));

        EnvironmentMappingDocument document = new EnvironmentMappingDocument(
                EnvironmentMappingDocument.CURRENT_SCHEMA_VERSION, List.of(confirmed, candidate, unknown));

        assertEquals(1, document.confirmedCurrentMappings().size());
        assertEquals(EnvironmentMappingConfirmation.CANDIDATE, document.mappings().stream()
                .filter(value -> value.kind().equals("PROPERTY"))
                .findFirst().orElseThrow().confirmation());
        assertEquals(EnvironmentMappingStaleness.unknown("source content was not available"), document.mappings()
                .stream().filter(value -> value.staleness().status().name().equals("UNKNOWN"))
                .findFirst().orElseThrow().staleness());
    }

    @Test
    void rejectsUnknownVersionFieldsDuplicateKeysAndInvalidRootWithoutRewrite() throws Exception {
        Path root = projectRoot("invalid");
        Path source = Files.writeString(root.resolve("agents/auctioneer.asl"), "+ready.");
        EnvironmentMappingFileRepository repository = new EnvironmentMappingFileRepository();
        Path valid = tempDir.resolve("valid.cartago-map.json");
        repository.save(valid, new EnvironmentMappingDocument(
                EnvironmentMappingDocument.CURRENT_SCHEMA_VERSION,
                List.of(operation(root, source, EnvironmentMappingConfirmation.CONFIRMED,
                        EnvironmentMappingStaleness.current()))), root);
        String json = Files.readString(valid);

        Path unknownVersion = tempDir.resolve("unknown-version.cartago-map.json");
        Files.writeString(unknownVersion, json.replace("0.1.0", "9.9.9"));
        assertThrows(IOException.class, () -> repository.load(unknownVersion, root));

        Path unknownField = tempDir.resolve("unknown-field.cartago-map.json");
        Files.writeString(unknownField, json.replace("{\"kind\"", "{\"extra\":true,\"kind\""));
        assertThrows(IOException.class, () -> repository.load(unknownField, root));

        String mappingLine = java.util.Arrays.stream(json.split("\\R"))
                .filter(line -> line.startsWith("    {"))
                .findFirst().orElseThrow();
        Path duplicate = tempDir.resolve("duplicate.cartago-map.json");
        Files.writeString(duplicate, json.replace(mappingLine + "\n  ]",
                mappingLine + ",\n" + mappingLine + "\n  ]"));
        assertThrows(IOException.class, () -> repository.load(duplicate, root));

        Path protectedFile = Files.writeString(tempDir.resolve("protected.cartago-map.json"), "original");
        IOException invalidRoot = assertThrows(IOException.class, () -> repository.save(
                protectedFile,
                EnvironmentMappingDocument.empty(),
                tempDir.resolve("missing-root")));
        assertTrue(invalidRoot.getMessage().contains("existing directory"));
        assertEquals("original", Files.readString(protectedFile));
    }

    @Test
    void doesNotChangeExistingBdiMappingRepositoryContract() throws Exception {
        Path root = projectRoot("bdi");
        MappingDocument document = MappingDocument.empty("use-fingerprint").upsert(new MappingBinding(
                MappingKind.BELIEF_ATTRIBUTE,
                "ready/0",
                "Auction::status",
                Optional.empty(),
                List.of("fixture evidence")));
        Path file = tempDir.resolve("existing.bdimap.json");
        MappingFileRepository repository = new MappingFileRepository();
        repository.save(file, document, root);
        assertEquals(document, repository.load(file, root));
    }

    private EnvironmentMappingDocument document(Path root, Path source) {
        return new EnvironmentMappingDocument(EnvironmentMappingDocument.CURRENT_SCHEMA_VERSION, List.of(
                operation(root, source, EnvironmentMappingConfirmation.CONFIRMED,
                        EnvironmentMappingStaleness.current()),
                property(root, source, EnvironmentMappingConfirmation.CONFIRMED,
                        EnvironmentMappingStaleness.current())));
    }

    private PersistedEnvironmentOperationMapping operation(
            Path root,
            Path source,
            EnvironmentMappingConfirmation confirmation,
            EnvironmentMappingStaleness staleness) {
        return operation(root, source, "Auction::open()", confirmation, staleness);
    }

    private PersistedEnvironmentOperationMapping operation(
            Path root,
            Path source,
            String umlTarget,
            EnvironmentMappingConfirmation confirmation,
            EnvironmentMappingStaleness staleness) {
        return new PersistedEnvironmentOperationMapping(
                "open", 0, "main", "auction", "AuctionArtifact", "open", 0, List.of(),
                umlTarget, confirmation, provenance(root, source), staleness,
                List.of("CArtAgO operation signature: open/0", "UML target exists"));
    }

    private PersistedEnvironmentPropertyMapping property(
            Path root,
            Path source,
            EnvironmentMappingConfirmation confirmation,
            EnvironmentMappingStaleness staleness) {
        return new PersistedEnvironmentPropertyMapping(
                "auction_status", 1, "main", "auction", "AuctionArtifact", "status", 1, "String",
                "Auction::status", confirmation, provenance(root, source), staleness,
                List.of("CArtAgO property signature: status/1", "UML target exists"));
    }

    private EnvironmentSourceProvenance provenance(Path root, Path source) {
        return new EnvironmentSourceProvenance(
                ProjectSourceId.fromPath(root, source), "Jason/CArtAgO static mapping fixture");
    }

    private Path projectRoot(String name) throws IOException {
        return Files.createDirectories(tempDir.resolve(name).resolve("agents")).getParent();
    }
}

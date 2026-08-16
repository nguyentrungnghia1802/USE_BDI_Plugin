package org.tzi.use.plugins.bdi.release;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;
import org.tzi.use.plugins.bdi.model.environment.EnvironmentMappingConfirmation;
import org.tzi.use.plugins.bdi.model.environment.EnvironmentMappingDocument;
import org.tzi.use.plugins.bdi.model.environment.EnvironmentMappingStalenessStatus;
import org.tzi.use.plugins.bdi.model.mapping.MappingDocument;
import org.tzi.use.plugins.bdi.model.mapping.MappingKind;
import org.tzi.use.plugins.bdi.model.organization.OrganizationMappingConfirmation;

class CrossModelCorrespondenceContractTest {
    private static final Set<String> CORE_KINDS = Set.of(
            "AGENT_CLASS",
            "AGENT_OBJECT",
            "ACTION_OPERATION",
            "PARAMETER",
            "RECEIVER_OBJECT",
            "BELIEF_ATTRIBUTE");

    @Test
    void documentsEveryImplementedFamilyWithoutMergingPersistenceOwners() throws Exception {
        Path metamodel = repositoryRoot().resolve("docs/project/metamodel");
        String specification = Files.readString(metamodel.resolve("CROSS_MODEL_CORRESPONDENCE.md"));
        String matrix = Files.readString(metamodel.resolve("CORRESPONDENCE_RULE_MATRIX.md"));
        String diagram = Files.readString(metamodel.resolve("correspondence-diagram.mmd"));

        for (String kind : CORE_KINDS) {
            assertTrue(specification.contains("`" + kind + "`"), () -> "Missing core kind: " + kind);
            assertTrue(matrix.contains("MappingKind." + kind), () -> "Missing matrix kind: " + kind);
        }
        for (String marker : Set.of(
                "CANDIDATE", "CONFIRMED", "CURRENT", "STALE", "UNKNOWN",
                ".bdimap.json", ".cartago-map.json", "ProjectSourceId",
                "OrganizationRoleMapping", "OrganizationMissionMapping",
                "OrganizationCardinalityMapping", "does not mean bidirectional")) {
            assertTrue(specification.contains(marker), () -> "Missing correspondence contract marker: " + marker);
        }
        assertTrue(specification.contains("additional unknown root/binding fields"));
        assertTrue(specification.contains("No organization mapping repository/schema exists"));
        assertTrue(matrix.contains("only confirmed-current reaches semantic validator"));

        assertTrue(diagram.startsWith("classDiagram"));
        assertTrue(diagram.contains("Agent ..> UmlClass : AGENT_CLASS"));
        assertTrue(diagram.contains("ArtifactOperation ..> UmlOperation : environment operation"));
        assertTrue(diagram.contains("RoleCardinality ..> OclInvariant : reviewed bounds"));
        assertTrue(diagram.contains("Candidate"));
        assertTrue(diagram.contains("Confirmed"));
        assertFalse(diagram.contains("runtime diagram"));
    }

    @Test
    void exactImplementationStatesAndSchemasRemainCompatible() {
        assertEquals(CORE_KINDS, Arrays.stream(MappingKind.values())
                .map(Enum::name).collect(Collectors.toSet()));
        assertEquals("0.2.0", MappingDocument.CURRENT_SCHEMA_VERSION);
        assertEquals("0.1.0", EnvironmentMappingDocument.CURRENT_SCHEMA_VERSION);
        assertEquals(Set.of("CANDIDATE", "CONFIRMED"),
                Arrays.stream(EnvironmentMappingConfirmation.values())
                        .map(Enum::name).collect(Collectors.toSet()));
        assertEquals(Set.of("CURRENT", "STALE", "UNKNOWN"),
                Arrays.stream(EnvironmentMappingStalenessStatus.values())
                        .map(Enum::name).collect(Collectors.toSet()));
        assertEquals(Set.of("CANDIDATE", "CONFIRMED"),
                Arrays.stream(OrganizationMappingConfirmation.values())
                        .map(Enum::name).collect(Collectors.toSet()));
    }

    private static Path repositoryRoot() {
        Path current = Path.of("").toAbsolutePath().normalize();
        while (current != null) {
            if (Files.isRegularFile(current.resolve("docs/project/16_PROJECT_COMPLETION_CHECKLIST.md"))) {
                return current;
            }
            current = current.getParent();
        }
        throw new IllegalStateException("Could not locate repository root from the test working directory");
    }
}

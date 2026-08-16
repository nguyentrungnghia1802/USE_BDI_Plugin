package org.tzi.use.plugins.bdi.validation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;
import org.tzi.use.plugins.bdi.validation.environment.EnvironmentRuleCatalog;
import org.tzi.use.plugins.bdi.validation.organization.OrganizationRuleCatalog;

class RuleCatalogCompletenessTest {
    private static final Set<String> EXPECTED_RULE_IDS = Set.of(
            "ASL-001", "ASL-002", "BDI-001", "BDI-002", "BDI-003", "BDI-004",
            "REF-001", "REF-002", "MAP-001", "MAP-002", "MAP-003", "SIG-001",
            "SIG-002", "SIG-003", "OWN-001", "BEL-001", "MSG-001", "OCL-001",
            "OCL-002", "CTX-001", "OCL-003", "OCL-004");
    private static final Set<String> EXPECTED_EXTENSION_IDS = Set.of(
            "ENV-001", "ENV-002", "ENV-003", "ENV-004",
            "ORG-001", "ORG-002", "ORG-003");

    @Test
    void implementationAndThesisCatalogContainTheSameMvpRules() throws IOException {
        Map<String, RulePhase> phases = StandardConsistencyRules.create().stream()
                .collect(Collectors.toMap(ConsistencyRule::id, ConsistencyRule::phase,
                        (first, second) -> first, LinkedHashMap::new));

        assertEquals(EXPECTED_RULE_IDS, phases.keySet());
        assertEquals(22, phases.size());
        assertEquals(Set.of("ENV-001", "ENV-002", "ENV-003", "ENV-004"),
                Set.copyOf(EnvironmentRuleCatalog.ids()));
        assertEquals(Set.of("ORG-001", "ORG-002", "ORG-003"),
                Set.copyOf(OrganizationRuleCatalog.ids()));
        assertTrue(OrganizationRuleCatalog.definitions().stream()
                .allMatch(definition -> definition.failureSeverity() == IssueSeverity.ERROR));
        assertEquals(RulePhase.PARSE, phases.get("ASL-001"));
        assertEquals(RulePhase.IR_WELL_FORMEDNESS, phases.get("BDI-001"));
        assertEquals(RulePhase.REFERENCE, phases.get("REF-001"));
        assertEquals(RulePhase.MAPPING, phases.get("MAP-003"));
        assertEquals(RulePhase.SIGNATURE, phases.get("SIG-001"));
        assertEquals(RulePhase.SNAPSHOT_OCL, phases.get("OCL-001"));
        assertEquals(RulePhase.BOUNDED_SIMULATION, phases.get("OCL-003"));

        String catalog = Files.readString(repositoryRoot().resolve(
                "docs/project/08_CONSISTENCY_RULE_CATALOG.md"));
        assertTrue(catalog.contains("Implementation matrix"));
        assertTrue(catalog.contains("StandardConsistencyRules"));
        EXPECTED_RULE_IDS.forEach(ruleId -> assertTrue(
                catalog.contains("| " + ruleId + " |"),
                () -> "Rule is missing from the documented catalog: " + ruleId));

        Path metamodel = repositoryRoot().resolve("docs/project/metamodel");
        String semantics = Files.readString(metamodel.resolve("STATIC_SEMANTICS.md"));
        String matrix = Files.readString(metamodel.resolve("RULE_TO_METAMODEL_MATRIX.md"));
        Set<String> documentedIds = new java.util.HashSet<>(EXPECTED_RULE_IDS);
        documentedIds.addAll(EXPECTED_EXTENSION_IDS);
        documentedIds.forEach(ruleId -> {
            String rowMarker = "| `" + ruleId + "` |";
            assertTrue(semantics.contains(rowMarker),
                    () -> "Rule is missing from static-semantics audit: " + ruleId);
            assertTrue(matrix.contains(rowMarker),
                    () -> "Rule is missing from rule/metamodel matrix: " + ruleId);
        });
        for (String formalization : Set.of(
                "PARSER_DIAGNOSTIC",
                "METAMODEL_WELL_FORMEDNESS",
                "REFERENCE_RESOLUTION",
                "CROSS_MODEL_CONSISTENCY",
                "SNAPSHOT_SEMANTIC",
                "BOUNDED_SIMULATION",
                "STATIC_ENVIRONMENT",
                "STATIC_ORGANIZATION")) {
            assertTrue(matrix.contains("`" + formalization + "`"),
                    () -> "Missing formalization type: " + formalization);
        }
        assertTrue(semantics.contains("NOT_EVALUATED"));
        assertTrue(semantics.contains("trigger.operator == ADD"));
        assertTrue(semantics.contains("There is no separate namespace field or per-agent namespace filter"));
        assertTrue(semantics.contains("No rule is moved to"));
        assertTrue(semantics.contains("OCL or EVL"));
        assertTrue(catalog.contains("confirmed/current persisted environment"));
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

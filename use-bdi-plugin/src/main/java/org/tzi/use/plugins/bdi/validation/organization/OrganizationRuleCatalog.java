package org.tzi.use.plugins.bdi.validation.organization;

import java.util.List;

import org.tzi.use.plugins.bdi.validation.IssueSeverity;

/** Separate static organization pilot; the 22 standard rule IDs remain unchanged. */
public final class OrganizationRuleCatalog {
    public static final String ROLE_CLASS = "ORG-001";
    public static final String MISSION_OPERATION = "ORG-002";
    public static final String CARDINALITY_CONSTRAINT = "ORG-003";

    private OrganizationRuleCatalog() {
    }

    public static List<RuleDefinition> definitions() {
        return List.of(
                new RuleDefinition(ROLE_CLASS, IssueSeverity.ERROR),
                new RuleDefinition(MISSION_OPERATION, IssueSeverity.ERROR),
                new RuleDefinition(CARDINALITY_CONSTRAINT, IssueSeverity.ERROR));
    }

    public static List<String> ids() {
        return definitions().stream().map(RuleDefinition::id).toList();
    }

    public record RuleDefinition(String id, IssueSeverity failureSeverity) {
    }
}

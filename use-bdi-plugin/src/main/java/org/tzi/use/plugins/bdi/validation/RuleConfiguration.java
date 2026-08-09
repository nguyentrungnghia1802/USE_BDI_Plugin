package org.tzi.use.plugins.bdi.validation;

import java.util.Collection;
import java.util.Collections;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Pattern;

/** Immutable, versioned selection of consistency rules to execute. */
public record RuleConfiguration(String schemaVersion, Set<String> enabledRuleIds) {
    public static final String CURRENT_SCHEMA_VERSION = "0.1.0";
    private static final Pattern RULE_ID = Pattern.compile("[A-Z][A-Z0-9-]*");

    public RuleConfiguration {
        if (!CURRENT_SCHEMA_VERSION.equals(schemaVersion)) {
            throw new IllegalArgumentException("Unsupported rule configuration schema version: " + schemaVersion);
        }
        Objects.requireNonNull(enabledRuleIds, "enabledRuleIds");
        TreeSet<String> sorted = new TreeSet<>();
        for (String ruleId : enabledRuleIds) {
            requireRuleId(ruleId);
            sorted.add(ruleId);
        }
        enabledRuleIds = Collections.unmodifiableSet(sorted);
    }

    public static RuleConfiguration of(Collection<String> ruleIds) {
        return from(CURRENT_SCHEMA_VERSION, ruleIds);
    }

    public static RuleConfiguration from(String schemaVersion, Collection<String> ruleIds) {
        Objects.requireNonNull(ruleIds, "ruleIds");
        TreeSet<String> normalized = new TreeSet<>();
        for (String ruleId : ruleIds) {
            requireRuleId(ruleId);
            if (!normalized.add(ruleId)) {
                throw new IllegalArgumentException("Duplicate enabled rule ID: " + ruleId);
            }
        }
        return new RuleConfiguration(schemaVersion, normalized);
    }

    public static RuleConfiguration standard() {
        return of(StandardConsistencyRules.ids());
    }

    public boolean isEnabled(String ruleId) {
        return enabledRuleIds.contains(Objects.requireNonNull(ruleId, "ruleId"));
    }

    private static void requireRuleId(String ruleId) {
        if (ruleId == null || !RULE_ID.matcher(ruleId).matches()) {
            throw new IllegalArgumentException("Invalid rule ID: " + ruleId);
        }
    }
}

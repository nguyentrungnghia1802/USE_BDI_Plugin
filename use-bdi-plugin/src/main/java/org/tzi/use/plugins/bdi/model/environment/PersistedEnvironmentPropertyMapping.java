package org.tzi.use.plugins.bdi.model.environment;

import java.util.List;
import java.util.Objects;

/** Persisted CArtAgO observable-property binding with type/arity evidence. */
public record PersistedEnvironmentPropertyMapping(
        String belief,
        int beliefArity,
        String workspace,
        String artifact,
        String artifactType,
        String property,
        int propertyArity,
        String propertyType,
        String umlTarget,
        EnvironmentMappingConfirmation confirmation,
        EnvironmentSourceProvenance provenance,
        EnvironmentMappingStaleness staleness,
        List<String> evidence) implements PersistedEnvironmentMapping {
    public PersistedEnvironmentPropertyMapping {
        requireText(belief, "belief");
        requireNonNegative(beliefArity, "beliefArity");
        requireText(workspace, "workspace");
        requireText(artifact, "artifact");
        requireText(artifactType, "artifactType");
        requireText(property, "property");
        requireNonNegative(propertyArity, "propertyArity");
        requireText(propertyType, "propertyType");
        requireText(umlTarget, "umlTarget");
        confirmation = Objects.requireNonNull(confirmation, "confirmation");
        provenance = Objects.requireNonNull(provenance, "provenance");
        staleness = Objects.requireNonNull(staleness, "staleness");
        evidence = copyTextList(evidence, "evidence");
        if (evidence.isEmpty()) {
            throw new IllegalArgumentException("evidence must not be empty");
        }
    }

    @Override
    public String kind() {
        return "PROPERTY";
    }

    @Override
    public String key() {
        return kind() + ":" + provenance.source().canonical() + ":"
                + workspace + "/" + artifact + "#" + property + "/" + propertyArity
                + "->" + umlTarget;
    }

    @Override
    public EnvironmentMapping toRuntimeMapping() {
        return new EnvironmentPropertyMapping(
                belief + "/" + beliefArity,
                workspace,
                artifact,
                property,
                propertyArity,
                umlTarget);
    }

    @Override
    public PersistedEnvironmentPropertyMapping withStaleness(EnvironmentMappingStaleness value) {
        return new PersistedEnvironmentPropertyMapping(
                belief, beliefArity, workspace, artifact, artifactType, property,
                propertyArity, propertyType, umlTarget, confirmation, provenance, value, evidence);
    }

    private static List<String> copyTextList(List<String> values, String field) {
        List<String> copy = List.copyOf(Objects.requireNonNull(values, field));
        for (String value : copy) {
            requireText(value, field + " item");
        }
        return copy;
    }

    private static void requireNonNegative(int value, String field) {
        if (value < 0) {
            throw new IllegalArgumentException(field + " must not be negative");
        }
    }

    private static void requireText(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(field + " must not be blank");
        }
    }
}

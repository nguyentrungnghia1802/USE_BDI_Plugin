package org.tzi.use.plugins.bdi.model.environment;

import java.util.List;
import java.util.Objects;

/** Persisted CArtAgO operation binding with explicit arity/type evidence. */
public record PersistedEnvironmentOperationMapping(
        String bdiAction,
        int actionArity,
        String workspace,
        String artifact,
        String artifactType,
        String operation,
        int operationArity,
        List<String> parameterTypes,
        String umlTarget,
        EnvironmentMappingConfirmation confirmation,
        EnvironmentSourceProvenance provenance,
        EnvironmentMappingStaleness staleness,
        List<String> evidence) implements PersistedEnvironmentMapping {
    public PersistedEnvironmentOperationMapping {
        requireText(bdiAction, "bdiAction");
        requireNonNegative(actionArity, "actionArity");
        requireText(workspace, "workspace");
        requireText(artifact, "artifact");
        requireText(artifactType, "artifactType");
        requireText(operation, "operation");
        requireNonNegative(operationArity, "operationArity");
        parameterTypes = copyTextList(parameterTypes, "parameterTypes");
        if (parameterTypes.size() != operationArity) {
            throw new IllegalArgumentException("parameterTypes must match operationArity");
        }
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
        return "OPERATION";
    }

    @Override
    public String key() {
        return kind() + ":" + provenance.source().canonical() + ":"
                + workspace + "/" + artifact + "#" + operation + "/" + operationArity
                + "->" + umlTarget;
    }

    @Override
    public EnvironmentMapping toRuntimeMapping() {
        return new EnvironmentOperationMapping(
                bdiAction, actionArity, workspace, artifact, operation, umlTarget);
    }

    @Override
    public PersistedEnvironmentOperationMapping withStaleness(EnvironmentMappingStaleness value) {
        return new PersistedEnvironmentOperationMapping(
                bdiAction, actionArity, workspace, artifact, artifactType, operation,
                operationArity, parameterTypes, umlTarget, confirmation, provenance, value, evidence);
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
